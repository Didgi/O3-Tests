package api.requests.endpoints;

import api.models.user.UserCreateResponse;
import api.models.user.UserSearchErrorResponse;
import api.models.user.UserSearchResponse;
import io.restassured.common.mapper.TypeRef;

import static api.config.ApiPath.USER_PATH;
import static api.config.ApiPath.USER_MAIN_PATH;

public final class UserEndpoints {

    public static final EndpointSpec<UserSearchResponse> SEARCH =
            new EndpointSpec<>(
                    USER_PATH,
                    new TypeRef<>() {
                    }
            );
    public static final EndpointSpec<UserSearchErrorResponse> SEARCH_ERROR =
            new EndpointSpec<>(
                    USER_PATH,
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<UserCreateResponse> CREATE =
            new EndpointSpec<>(
                    USER_PATH,
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<UserCreateResponse> READ =
            new EndpointSpec<>(
                    USER_MAIN_PATH,
                    new TypeRef<>() {
                    }
            );
    public static final EndpointSpec<UserCreateResponse> UPDATE =
            new EndpointSpec<>(
                    USER_MAIN_PATH,
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>(
                    USER_MAIN_PATH,
                    new TypeRef<>() {
                    }
            );

    public static final CrudOperations<UserCreateResponse> CRUD =
            new CrudOperations<>(
                    CREATE,
                    READ,
                    UPDATE,
                    DELETE
            );

    private UserEndpoints() {
    }
}