package api.requests.skelethon.endpoints;

import api.models.user.UserSearchResponse;
import io.restassured.common.mapper.TypeRef;

public final class UserEndpoints {

    public static final EndpointSpec<UserSearchResponse> SEARCH =
            new EndpointSpec<>(
                    "/user",
                    new TypeRef<UserSearchResponse>() {
                    }
            );

    private UserEndpoints() {
    }
}