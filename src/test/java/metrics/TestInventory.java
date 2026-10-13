package metrics;

import java.time.Instant;
import java.util.List;

public record TestInventory(
        int schemaVersion,
        Instant collectedAt,
        Summary summary,
        CriticalFeatureCoverage criticalFeatureCoverage,
        List<TestInfo> tests
) {
    public record Summary(long total, long active, long disabled,
                          long api, long ui, long unclassified, long criticalTests) {}

    public record CriticalFeatureCoverage(long total, long covered,
                                          long uncovered, Double percent,
                                          List<String> uncoveredIds) {}
}
