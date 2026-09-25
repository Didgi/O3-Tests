package api.requests.steps;

import api.models.patients.PatientCreateRequest;
import api.models.patients.PatientResponse;
import api.models.user.*;
import api.requests.endpoints.PatientEndpoints;
import api.requests.endpoints.UserEndpoints;
import api.requests.skeleton.requesters.*;

public class UserSteps extends CrudStepsSupport<
        UserCreateRequest,
        UserUpdateRequest,
        UserCreateResponse> {
    private SuccessfulSearchRequester<
            UserSearchParams,
            UserSearchResponse
            > userSearchRequester;

    private final ErrorSearchRequester<
            UserSearchParams,
            UserSearchErrorResponse
            > userErrorSearchRequester;

    private SuccessfulCrudRequester<
            UserCreateRequest,
            UserUpdateRequest,
            UserCreateResponse> userRequester;

    public UserSteps(RequesterFactory requester) {
        super(requester, UserEndpoints.CRUD);

        userSearchRequester =
                requester.successfulSearch(
                        UserEndpoints.SEARCH
                );

        userErrorSearchRequester =
                requester.errorSearch(
                        UserEndpoints.SEARCH_ERROR
                );
    }

    public UserCreateResponse createUser(UserCreateRequest request) {
        return successfulCrud.create(request);
    }

    public UserSearchResponse searchUsers(String query) {
        return userSearchRequester.search(
                new UserSearchParams(query, "default")
        );
    }

    public UserSearchResponse searchUsers(
            String query,
            String representation
    ) {
        return userSearchRequester.search(
                new UserSearchParams(query, representation)
        );
    }

    public UserSearchErrorResponse searchUsersUnauthorized(
            String query
    ) {
        return userErrorSearchRequester.search(
                new UserSearchParams(
                        query,
                        "default"
                )
        );
    }
}
