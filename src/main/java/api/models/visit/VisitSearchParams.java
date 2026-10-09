package api.models.visit;

import api.requests.skeleton.query.QueryParams;

import java.util.Map;

public record VisitSearchParams(
        String patient,
        boolean includeInactive
) implements QueryParams {

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "patient", patient,
                "includeInactive", includeInactive
        );
    }
}
