package metrics;

import java.util.List;

public record TestInfo(
        String id,
        String className,
        String methodName,
        String type,
        boolean disabled,
        String disabledReason,
        String severity,
        boolean severityExplicit,
        List<String> issues
) {}
