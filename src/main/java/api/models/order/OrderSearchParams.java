package api.models.order;

import api.requests.skeleton.options.ReadOptions;
import api.requests.skeleton.query.QueryParams;

import java.util.Map;

public record OrderSearchParams(
        String patientUuid,
        ReadOptions options
) implements QueryParams {
    @Override
    public Map<String, ?> asMap() {
        if (options.isExplicit()) {
            return Map.of(
                    "patient", patientUuid,
                    "v", options.representation()
            );
        }
        return Map.of("patient", patientUuid);
    }
}
