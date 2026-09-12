package api.requests.steps;

import api.models.user.UserSearchParams;
import api.models.user.UserSearchResponse;
import api.requests.skelethon.endpoints.UserEndpoints;
import api.requests.skelethon.interfaces.SearchEndpoint;
import api.requests.skelethon.requesters.SearchRequester;
import api.requests.skelethon.requesters.SuccessfulSearchRequester;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserSteps {
    private final SuccessfulSearchRequester<
                UserSearchParams,
                UserSearchResponse
                > userSearchRequester;

    public UserSteps(RequestSpecification requestSpecification) {
        SearchEndpoint<UserSearchParams> rawRequester =
                new SearchRequester<>(
                        requestSpecification,
                        UserEndpoints.SEARCH
                );

        this.userSearchRequester =
                new SuccessfulSearchRequester<>(
                        rawRequester,
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
