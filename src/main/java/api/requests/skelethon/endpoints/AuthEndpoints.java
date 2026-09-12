package api.requests.skelethon.endpoints;

import api.models.auth.response.SessionResponse;
import io.restassured.common.mapper.TypeRef;

public final class AuthEndpoints {
    private AuthEndpoints() {}

    public static final EndpointSpec<SessionResponse> SESSION =
            new EndpointSpec<>(
                    "/session",
                    new TypeRef<SessionResponse>() {
                    }
            );
}
