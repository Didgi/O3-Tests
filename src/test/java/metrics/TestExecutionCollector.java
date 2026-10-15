package metrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/** Reads Allure result files without executing tests. Java 21, Jackson 2.x. */
public final class TestExecutionCollector {
    private static final Path INVENTORY = Path.of("target/metrics/test-inventory.json");
    private static final Path RESULTS = Path.of("target/allure-results");
    private static final Path OUTPUT = Path.of("target/metrics/test-run.json");
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private TestExecutionCollector() {}

    public static void main(String[] args) throws IOException {
        if (!Files.isRegularFile(INVENTORY)) throw new IllegalStateException("Missing " + INVENTORY);
        if (!Files.isDirectory(RESULTS)) throw new IllegalStateException("Missing " + RESULTS);
        JsonNode inventory = JSON.readTree(INVENTORY.toFile());
        Map<String, JsonNode> byId = new LinkedHashMap<>();
        Map<String, List<String>> byClassAndMethod = new HashMap<>();
        for (JsonNode test : inventory.path("tests")) {
            String id = test.path("id").asText();
            byId.put(id, test);
            String key = test.path("className").asText() + "#" + test.path("methodName").asText();
            byClassAndMethod.computeIfAbsent(key, k -> new ArrayList<>()).add(id);
        }

        Map<String, List<Attempt>> attempts = new LinkedHashMap<>();
        List<String> unmatched = new ArrayList<>();
        int files = 0;
        try (Stream<Path> paths = Files.list(RESULTS)) {
            for (Path file : paths.filter(p -> p.getFileName().toString().endsWith("-result.json")).sorted().toList()) {
                files++;
                JsonNode result = JSON.readTree(file.toFile());
                String key = allureMethodKey(result);
                List<String> candidates = byClassAndMethod.getOrDefault(key, List.of());
                String id = candidates.size() == 1 ? candidates.get(0) : null;
                if (id == null) {
                    unmatched.add(file.getFileName() + " | " + key + (candidates.size() > 1 ? " | overloaded method ambiguous" : ""));
                    continue;
                }
                String status = result.path("status").asText("unknown").toUpperCase(Locale.ROOT);
                long start = result.path("start").asLong(0);
                long stop = result.path("stop").asLong(0);
                attempts.computeIfAbsent(id, k -> new ArrayList<>()).add(new Attempt(status, start, stop,
                        result.path("uuid").asText(""), file.getFileName().toString()));
            }
        }
        if (files == 0) throw new IllegalStateException("No *-result.json in " + RESULTS);
        if (!unmatched.isEmpty()) {
            throw new IllegalStateException("Unmatched/ambiguous Allure results (first 15):\n" +
                    String.join("\n", unmatched.stream().limit(15).toList()) +
                    "\nFix the mapping before reporting metrics; matched results must not be silently dropped.");
        }

        List<Map<String, Object>> tests = new ArrayList<>();
        int passed = 0, failed = 0, broken = 0, skipped = 0, notRun = 0, disabled = 0;
        int criticalPassed = 0, criticalFailed = 0, criticalSkipped = 0, criticalNotRun = 0, criticalDisabled = 0;
        int flakyCandidates = 0;
        long minStart = Long.MAX_VALUE, maxStop = Long.MIN_VALUE;
        for (var entry : byId.entrySet()) {
            String id = entry.getKey();
            JsonNode test = entry.getValue();
            boolean isDisabled = test.path("disabled").asBoolean(false);
            boolean isCritical = "CRITICAL".equals(test.path("severity").asText());
            List<Attempt> invocations = attempts.getOrDefault(id, List.of());
            // Allure's separate result files do not reliably distinguish parameter rows from retries.
            // Conservative aggregation: any failure/broken invocation fails the logical method.
            String status;
            if (isDisabled) status = "DISABLED";
            else if (invocations.isEmpty()) status = "NOT_RUN";
            else if (invocations.stream().anyMatch(a -> a.status.equals("FAILED"))) status = "FAILED";
            else if (invocations.stream().anyMatch(a -> a.status.equals("BROKEN"))) status = "BROKEN";
            else if (invocations.stream().anyMatch(a -> a.status.equals("SKIPPED"))) status = "SKIPPED";
            else if (invocations.stream().allMatch(a -> a.status.equals("PASSED"))) status = "PASSED";
            else status = "BROKEN";

            // A disabled test unexpectedly executed is a data inconsistency, not a successful check.
            if (isDisabled && invocations.stream().anyMatch(a -> !a.status.equals("SKIPPED"))) {
                throw new IllegalStateException("Disabled inventory test has execution results: " + id);
            }
            for (Attempt a : invocations) {
                if (a.start > 0 && a.stop >= a.start) {
                    minStart = Math.min(minStart, a.start);
                    maxStop = Math.max(maxStop, a.stop);
                }
            }
            switch (status) {
                case "PASSED" -> passed++;
                case "FAILED" -> failed++;
                case "BROKEN" -> broken++;
                case "SKIPPED" -> skipped++;
                case "NOT_RUN" -> notRun++;
                case "DISABLED" -> disabled++;
                default -> throw new IllegalStateException(status);
            }
            if (isCritical) switch (status) {
                case "PASSED" -> criticalPassed++;
                case "FAILED", "BROKEN" -> criticalFailed++;
                case "SKIPPED" -> criticalSkipped++;
                case "NOT_RUN" -> criticalNotRun++;
                case "DISABLED" -> criticalDisabled++;
                default -> throw new IllegalStateException(status);
            }
            boolean candidate = invocations.stream().anyMatch(a -> a.status.equals("PASSED")) &&
                    invocations.stream().anyMatch(a -> a.status.equals("FAILED") || a.status.equals("BROKEN"));
            if (candidate) flakyCandidates++; // Not a validated flaky rate: parameter rows can differ.
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", id);
            item.put("type", test.path("type").asText());
            item.put("severity", test.path("severity").asText());
            item.put("status", status);
            item.put("resultCount", invocations.size());
            item.put("results", invocations);
            item.put("mixedResultsCandidate", candidate);
            tests.add(item);
        }
        int executed = passed + failed + broken;
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", byId.size());
        summary.put("passed", passed);
        summary.put("failed", failed);
        summary.put("broken", broken);
        summary.put("skipped", skipped);
        summary.put("notRun", notRun);
        summary.put("disabled", disabled);
        summary.put("passRate", executed == 0 ? null : 100.0 * passed / executed);
        summary.put("criticalPassed", criticalPassed);
        summary.put("criticalFailedOrBroken", criticalFailed);
        summary.put("criticalSkipped", criticalSkipped);
        summary.put("criticalNotRun", criticalNotRun);
        summary.put("criticalDisabled", criticalDisabled);
        summary.put("mixedResultsCandidates", flakyCandidates);
        summary.put("flakyRate", null); // requires retry identity and comparable history
        summary.put("executionWindowMs", minStart == Long.MAX_VALUE ? null : maxStop - minStart);
        // Feature coverage is static, and release readiness also requires build freshness and CI status.
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schemaVersion", 1);
        report.put("collectedAt", Instant.now().toString());
        report.put("inventoryCollectedAt", inventory.path("collectedAt").asText());
        report.put("summary", summary);
        report.put("releaseReadiness", "UNKNOWN");
        report.put("tests", tests);
        Files.createDirectories(OUTPUT.getParent());
        JSON.writeValue(OUTPUT.toFile(), report);
        System.out.printf("Test run: passed=%d failed=%d broken=%d skipped=%d notRun=%d disabled=%d; passRate=%s%%%n",
                passed, failed, broken, skipped, notRun, disabled, summary.get("passRate"));
        System.out.println("Written: " + OUTPUT);
    }

    private static String allureMethodKey(JsonNode result) {
        String className = "";
        for (JsonNode label : result.path("labels")) {
            if ("testClass".equals(label.path("name").asText())) {
                className = label.path("value").asText();
                break;
            }
        }
        String method = "";
        for (JsonNode label : result.path("labels")) {
            if ("testMethod".equals(label.path("name").asText())) {
                method = label.path("value").asText();
                break;
            }
        }
        String fullName = result.path("fullName").asText();
        if (className.isBlank() && fullName.contains(".")) {
            String prefix = fullName.substring(0, fullName.indexOf('(') < 0 ? fullName.length() : fullName.indexOf('('));
            className = prefix.substring(0, prefix.lastIndexOf('.'));
        }
        if (method.isBlank() && fullName.contains(".")) {
            String suffix = fullName.substring(fullName.lastIndexOf('.') + 1);
            method = suffix.replaceFirst("\\(.*$", "");
        }
        return className + "#" + method;
    }

    public record Attempt(String status, long start, long stop, String uuid, String sourceFile) {}
}
