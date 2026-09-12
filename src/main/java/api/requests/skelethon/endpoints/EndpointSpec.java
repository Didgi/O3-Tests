package api.requests.skelethon.endpoints;

import io.restassured.common.mapper.TypeRef;

import java.util.Objects;

public record EndpointSpec<RES>(
        String pathTemplate,
        TypeRef<RES> responseTypeRef
) {
    public EndpointSpec {
        Objects.requireNonNull(pathTemplate, "pathTemplate must not be null");
        Objects.requireNonNull(responseTypeRef, "responseTypeRef must not be null");

        if (pathTemplate.isBlank()) {
            throw new IllegalArgumentException("pathTemplate must not be blank");
        }
    }
}
