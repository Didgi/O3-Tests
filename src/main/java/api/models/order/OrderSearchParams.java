package api.models.order;

import api.requests.skeleton.query.QueryParams;

import java.util.Map;

public record OrderSearchParams(
        String patientUuid
) implements QueryParams {
    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "patient", patientUuid
        );
    }
}
