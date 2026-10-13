package metrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/** Java 21, Jackson 2.x. Run `seal` in trusted CI after collecting test-run.json. */
public final class ReleaseReadinessCollector {
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final Path DIR = Path.of("target/metrics");
    private static final Path INVENTORY = DIR.resolve("test-inventory.json");
    private static final Path RUN = DIR.resolve("test-run.json");
    private static final Path CONTEXT = DIR.resolve("run-context.json");
    private static final Path OUTPUT = DIR.resolve("release-readiness.json");

    private ReleaseReadinessCollector() {}

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && "seal".equals(args[0])) { seal(); return; }
        if (args.length != 0) throw new IllegalArgumentException("Usage: ReleaseReadinessCollector [seal]");
        evaluate();
    }

    private static void seal() throws Exception {
        // Only call from trusted workflow_run job after artifacts are verified and the test workflow has completed.
        String sha = requiredEnv("TEST_SOURCE_SHA");
        String runId = requiredEnv("TEST_WORKFLOW_RUN_ID");
        String attempt = requiredEnv("TEST_WORKFLOW_RUN_ATTEMPT");
        String conclusion = requiredEnv("TEST_WORKFLOW_CONCLUSION").toLowerCase(Locale.ROOT);
        if (!Set.of("success", "failure", "timed_out", "cancelled", "action_required", "neutral", "skipped").contains(conclusion))
            throw new IllegalStateException("Unexpected workflow conclusion: " + conclusion);
        if (!"true".equalsIgnoreCase(requiredEnv("TEST_WORKFLOW_COMPLETED")))
            throw new IllegalStateException("Test workflow must be completed");
        JsonNode inventory = read(INVENTORY), run = read(RUN);
        if (!inventory.path("tests").isArray() || !run.path("tests").isArray())
            throw new IllegalStateException("Both reports must contain tests arrays");
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("schemaVersion", 1);
        context.put("sourceSha", sha);
        context.put("workflowRunId", runId);
        context.put("workflowRunAttempt", attempt);
        context.put("workflowConclusion", conclusion);
        context.put("workflowCompleted", true);
        context.put("sealedAt", Instant.now().toString());
        context.put("inventorySha256", sha256(INVENTORY));
        context.put("testRunSha256", sha256(RUN));
        JSON.writeValue(CONTEXT.toFile(), context);
        System.out.println("Sealed CI context: " + CONTEXT);
    }

    private static void evaluate() throws Exception {
        JsonNode inventory = read(INVENTORY), run = read(RUN);
        JsonNode coverage = inventory.path("criticalFeatureCoverage");
        JsonNode summary = run.path("summary");
        List<String> reasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean trustworthy = true;
        JsonNode ctx = null;
        if (!Files.isRegularFile(CONTEXT)) {
            trustworthy = false;
            warnings.add("Missing run-context.json: cannot verify CI provenance");
        } else {
            ctx = read(CONTEXT);
            if (ctx.path("schemaVersion").asInt(-1) != 1 ||
                    !ctx.path("workflowCompleted").asBoolean(false) ||
                    !ctx.path("inventorySha256").asText().equals(sha256(INVENTORY)) ||
                    !ctx.path("testRunSha256").asText().equals(sha256(RUN)) ||
                    ctx.path("sourceSha").asText().isBlank() ||
                    ctx.path("workflowRunId").asText().isBlank() ||
                    ctx.path("workflowRunAttempt").asText().isBlank()) {
                trustworthy = false;
                warnings.add("CI context incomplete or report checksums do not match");
            }
            if (!Set.of("success", "failure").contains(ctx.path("workflowConclusion").asText())) {
                trustworthy = false;
                warnings.add("Test workflow conclusion is not success/failure");
            }
        }
        if (!inventory.path("tests").isArray() || !run.path("tests").isArray()) {
            trustworthy = false;
            warnings.add("Missing tests array in inventory or run");
        }
        Map<String, JsonNode> invTests = new HashMap<>(), runTests = new HashMap<>();
        for (JsonNode t : inventory.path("tests")) {
            String id = t.path("id").asText();
            if (id.isBlank() || invTests.put(id, t) != null) {
                trustworthy = false; warnings.add("Invalid/duplicate inventory test ID: " + id);
            }
        }
        for (JsonNode t : run.path("tests")) {
            String id = t.path("id").asText();
            if (id.isBlank() || runTests.put(id, t) != null) {
                trustworthy = false; warnings.add("Invalid/duplicate execution test ID: " + id);
            }
        }
        if (!invTests.keySet().equals(runTests.keySet())) {
            trustworthy = false;
            warnings.add("Inventory and execution test ID sets differ");
        }
        int cp = 0, cf = 0, cs = 0, cn = 0, cd = 0;
        for (var entry : invTests.entrySet()) {
            JsonNode inv = entry.getValue(), actual = runTests.get(entry.getKey());
            if (actual == null) continue;
            if (!inv.path("severity").asText().equals(actual.path("severity").asText()) ||
                    !inv.path("type").asText().equals(actual.path("type").asText())) {
                trustworthy = false;
                warnings.add("Test metadata mismatch: " + entry.getKey());
            }
            if (!"CRITICAL".equals(inv.path("severity").asText())) continue;
            String status = actual.path("status").asText();
            boolean disabled = inv.path("disabled").asBoolean();
            if (disabled != "DISABLED".equals(status)) {
                trustworthy = false;
                warnings.add("Disabled status mismatch: " + entry.getKey());
            }
            switch (status) {
                case "PASSED" -> cp++;
                case "FAILED", "BROKEN" -> cf++;
                case "SKIPPED" -> cs++;
                case "NOT_RUN" -> cn++;
                case "DISABLED" -> cd++;
                default -> { trustworthy = false; warnings.add("Unknown test status: " + status); }
            }
        }
        if (summary.path("total").asInt(-1) != invTests.size() ||
                summary.path("criticalPassed").asInt(-1) != cp ||
                summary.path("criticalFailedOrBroken").asInt(-1) != cf ||
                summary.path("criticalSkipped").asInt(-1) != cs ||
                summary.path("criticalNotRun").asInt(-1) != cn ||
                summary.path("criticalDisabled").asInt(-1) != cd) {
            trustworthy = false;
            warnings.add("Execution summary inconsistent with per-test records");
        }
        int criticalTotal = cp + cf + cs + cn + cd;
        if (coverage.path("total").asInt(-1) <= 0 ||
                coverage.path("covered").asInt(-1) < 0 ||
                coverage.path("uncovered").asInt(-1) < 0 ||
                coverage.path("covered").asInt() + coverage.path("uncovered").asInt() != coverage.path("total").asInt() ||
                !coverage.path("percent").isNumber() || criticalTotal == 0) {
            trustworthy = false;
            warnings.add("Missing/inconsistent critical feature coverage or no critical tests");
        }
        if (cf > 0) reasons.add(cf + " critical tests failed or broken");
        if (cd > 0) reasons.add(cd + " critical tests disabled");
        if (cs > 0) reasons.add(cs + " critical tests skipped");
        if (cn > 0) reasons.add(cn + " critical tests not run");
        if (coverage.path("uncovered").asInt(0) > 0) reasons.add(coverage.path("uncovered").asInt() + " critical features uncovered");
        if (ctx != null && "failure".equals(ctx.path("workflowConclusion").asText()))
            reasons.add("CI workflow failed — check failed jobs in GitHub Actions");
        String status = !trustworthy ? "UNKNOWN" : !reasons.isEmpty() ? "NOT_READY" : "READY";
        Map<String, Object> critical = new LinkedHashMap<>();
        critical.put("total", criticalTotal);
        critical.put("passed", cp);
        critical.put("failedOrBroken", cf);
        critical.put("skipped", cs);
        critical.put("notRun", cn);
        critical.put("disabled", cd);
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("schemaVersion", 1);
        output.put("evaluatedAt", Instant.now().toString());
        output.put("status", status);
        output.put("sourceSha", ctx == null ? null : ctx.path("sourceSha").asText());
        output.put("workflowRunId", ctx == null ? null : ctx.path("workflowRunId").asText());
        output.put("workflowRunAttempt", ctx == null ? null : ctx.path("workflowRunAttempt").asText());
        output.put("criticalFeatureCoverage", coverage.isMissingNode() ? null : coverage);
        output.put("criticalTests", critical);
        output.put("reasons", reasons);
        output.put("warnings", warnings);
        JSON.writeValue(OUTPUT.toFile(), output);
        System.out.println("Release readiness: " + status + " | " + OUTPUT);
        if (!warnings.isEmpty()) System.out.println("Warnings: " + String.join("; ", warnings));
    }

    private static JsonNode read(Path path) throws IOException {
        if (!Files.isRegularFile(path)) throw new IllegalStateException("Missing " + path);
        return JSON.readTree(path.toFile());
    }
    private static String requiredEnv(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) throw new IllegalStateException("Missing environment variable " + key);
        return v;
    }
    private static String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        return HexFormat.of().formatHex(digest);
    }
}
