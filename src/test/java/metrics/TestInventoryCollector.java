package metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Disabled;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;

public final class TestInventoryCollector {
    private static final Path CLASSES = Path.of("target/test-classes");
    private static final Path FEATURES = Path.of("src/test/resources/metrics/features.csv");
    private static final Path OUTPUT = Path.of("target/metrics/test-inventory.json");

    private TestInventoryCollector() {}

    public static void main(String[] args) throws Exception {
        if (!Files.isDirectory(CLASSES)) {
            throw new IllegalStateException("Compile tests first: mvn test-compile");
        }

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClasspathRoots(Set.of(CLASSES.toAbsolutePath())))
                .build();
        TestPlan plan = LauncherFactory.create().discover(request);

        Map<String, TestInfo> unique = new TreeMap<>();
        for (TestIdentifier root : plan.getRoots()) {
            for (TestIdentifier node : plan.getDescendants(root)) {
                TestSource source = node.getSource().orElse(null);
                if (!(source instanceof MethodSource methodSource)) continue;

                Method method = methodSource.getJavaMethod();
                Class<?> clazz = methodSource.getJavaClass();
                String id = stableId(clazz, method);
                boolean disabled = disabledReason(clazz, method) != null;
                String reason = disabledReason(clazz, method);
                Severity severity = method.getAnnotation(Severity.class);
                if (severity == null) severity = clazz.getAnnotation(Severity.class);
                String severityName = severity == null ? SeverityLevel.NORMAL.name() : severity.value().name();
                boolean explicit = severity != null;
                LinkedHashSet<String> issues = new LinkedHashSet<>();
                Issue methodIssue = method.getAnnotation(Issue.class);
                Issue classIssue = clazz.getAnnotation(Issue.class);
                if (methodIssue != null) issues.add(methodIssue.value().trim());
                if (classIssue != null) issues.add(classIssue.value().trim());

                TestInfo info = new TestInfo(id, clazz.getName(), method.getName(),
                        TestClassifier.classify(clazz), disabled, reason,
                        severityName, explicit, List.copyOf(issues));
                unique.putIfAbsent(id, info);
            }
        }

        List<TestInfo> tests = List.copyOf(unique.values());
        long disabled = tests.stream().filter(TestInfo::disabled).count();
        long api = tests.stream().filter(t -> t.type().equals("API")).count();
        long ui = tests.stream().filter(t -> t.type().equals("UI")).count();
        long critical = tests.stream().filter(t -> t.severity().equals("CRITICAL")).count();
        TestInventory.Summary summary = new TestInventory.Summary(
                tests.size(), tests.size() - disabled, disabled, api, ui,
                tests.size() - api - ui, critical);

        Map<String, String> features = loadFeatures(FEATURES);
        Set<String> criticalFeatures = features.entrySet().stream()
                .filter(e -> e.getValue().equals("Critical"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> covered = tests.stream().filter(t -> !t.disabled())
                .flatMap(t -> t.issues().stream()).collect(Collectors.toSet());
        List<String> uncovered = criticalFeatures.stream().filter(id -> !covered.contains(id)).sorted().toList();
        long total = criticalFeatures.size();
        long coveredCount = total - uncovered.size();
        Double percent = total == 0 ? null : 100.0 * coveredCount / total;
        TestInventory.CriticalFeatureCoverage coverage = new TestInventory.CriticalFeatureCoverage(
                total, coveredCount, uncovered.size(), percent, uncovered);

        TestInventory inventory = new TestInventory(1, Instant.now(), summary, coverage, tests);
        Files.createDirectories(OUTPUT.getParent());
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.writeValue(OUTPUT.toFile(), inventory);
        System.out.printf("Inventory: total=%d, api=%d, ui=%d, disabled=%d, critical=%d%n",
                summary.total(), api, ui, disabled, critical);
        System.out.printf("Critical feature coverage: %d/%d%n", coveredCount, total);
        System.out.println("Written: " + OUTPUT);
    }

    private static String stableId(Class<?> clazz, Method method) {
        String params = Arrays.stream(method.getParameterTypes()).map(Class::getTypeName)
                .collect(Collectors.joining(","));
        return clazz.getName() + "#" + method.getName() + "(" + params + ")";
    }

    private static String disabledReason(Class<?> clazz, Method method) {
        Disabled onMethod = method.getAnnotation(Disabled.class);
        if (onMethod != null) return onMethod.value().isBlank() ? "@Disabled on method" : onMethod.value();
        // @Disabled on a superclass does not disable subclasses in JUnit Jupiter.
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getEnclosingClass()) {
            Disabled onClass = c.getDeclaredAnnotation(Disabled.class);
            if (onClass != null) return onClass.value().isBlank() ? "@Disabled on class" : onClass.value();
        }
        return null;
    }

    private static Map<String, String> loadFeatures(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Missing features registry: " + path);
        }
        Map<String, String> features = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isBlank()) continue;
            String[] parts = line.split(";", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new IllegalArgumentException("Invalid features.csv line " + (i + 1));
            }
            String id = parts[0].trim();
            String priority = parts[1].trim();
            if (!Set.of("Critical", "High", "Medium", "Low").contains(priority)) {
                throw new IllegalArgumentException("Unknown priority at line " + (i + 1) + ": " + priority);
            }
            if (features.putIfAbsent(id, priority) != null) {
                throw new IllegalArgumentException("Duplicate feature ID at line " + (i + 1) + ": " + id);
            }
        }
        return features;
    }
}
