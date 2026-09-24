package api.requests.steps;

import api.models.user.UserSearchErrorResponse;
import api.models.user.UserSearchParams;
import api.models.user.UserSearchResponse;
import api.requests.endpoints.UserEndpoints;
import api.requests.skeleton.requesters.ErrorSearchRequester;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulSearchRequester;

public class UserSteps {
    private SuccessfulSearchRequester<
            UserSearchParams,
            UserSearchResponse
            > userSearchRequester;

    private final ErrorSearchRequester<
            UserSearchParams,
            UserSearchErrorResponse
            > userErrorSearchRequester;

    public UserSteps(RequesterFactory requester) {

        userSearchRequester =
                requester.successfulSearch(
                        UserEndpoints.SEARCH
                );

        userErrorSearchRequester =
                requester.errorSearch(
                        UserEndpoints.SEARCH_ERROR
                );
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
