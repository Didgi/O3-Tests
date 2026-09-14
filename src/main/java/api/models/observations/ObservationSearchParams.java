package api.models.observations;

import api.requests.skeleton.query.QueryParams;

import java.util.Map;

public record ObservationSearchParams() implements QueryParams {

    @Override
    public Map<String, ?> asMap() {
        return Map.of();
    }
}
