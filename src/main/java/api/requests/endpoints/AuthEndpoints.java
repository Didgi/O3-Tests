package api.requests.endpoints;

import api.models.auth.response.SessionResponse;
import io.restassured.common.mapper.TypeRef;

import static api.config.ApiPath.CHANGE_PASSWORD_BY_USER_PATH;
import static api.config.ApiPath.GET_SESSION_PATH;

public final class AuthEndpoints {
    private AuthEndpoints() {
    }

    public static final EndpointSpec<SessionResponse> SESSION =
            new EndpointSpec<>(
                    GET_SESSION_PATH,
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<Object> CHANGE_PASSWORD =
            new EndpointSpec<>(
                    CHANGE_PASSWORD_BY_USER_PATH,
                    new TypeRef<>() {
                    }
            );
}
