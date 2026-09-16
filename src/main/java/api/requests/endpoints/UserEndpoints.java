package api.requests.endpoints;

import api.models.user.UserSearchErrorResponse;
import api.models.user.UserSearchResponse;
import io.restassured.common.mapper.TypeRef;

import static api.config.ApiPath.GET_USER_PATH;

public final class UserEndpoints {

    public static final EndpointSpec<UserSearchResponse> SEARCH =
            new EndpointSpec<>(
                    GET_USER_PATH,
                    new TypeRef<>() {
                    }
            );
    public static final EndpointSpec<UserSearchErrorResponse> SEARCH_ERROR =
            new EndpointSpec<>(
                    GET_USER_PATH,
                    new TypeRef<>() {
                    }
            );

    private UserEndpoints() {
    }
}