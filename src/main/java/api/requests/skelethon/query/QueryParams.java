package api.requests.skelethon.query;

import java.util.Map;
import java.util.Objects;

@FunctionalInterface
public interface QueryParams {

    Map<String, ?> asMap();

    static QueryParams empty() {
        return Map::of;
    }

    static QueryParams of(Map<String, ?> parameters) {
        Objects.requireNonNull(parameters, "parameters must not be null");
        Map<String, ?> copy = Map.copyOf(parameters);
        return () -> copy;
    }
}
