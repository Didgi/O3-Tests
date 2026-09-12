package api.models.user;

import api.requests.skelethon.query.QueryParams;

import java.util.Map;

public record UserSearchParams(
        String query,
        String representation
) implements QueryParams {

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "q", query,
                "v", representation
        );
    }
}
