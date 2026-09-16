package api.models.patients;

import api.requests.skeleton.query.QueryParams;

import java.util.Map;

public record PatientSearchParams(
        String q
) implements QueryParams {

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "q", q
        );
    }
}