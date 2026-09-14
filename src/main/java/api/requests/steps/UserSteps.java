package api.requests.steps;

import api.models.user.UserSearchParams;
import api.models.user.UserSearchResponse;
import api.requests.endpoints.UserEndpoints;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulSearchRequester;

public class UserSteps {
    private final SuccessfulSearchRequester<
            UserSearchParams,
            UserSearchResponse
            > userSearchRequester;

    public UserSteps(RequesterFactory requester) {
        userSearchRequester =
                requester.successfulSearch(
                        UserEndpoints.SEARCH
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
}
