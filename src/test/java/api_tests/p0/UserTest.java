package api_tests.p0;

import api.models.user.UserSearchResponse;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserTest extends BaseApiTest {
    private UserSteps userSteps;

    @BeforeEach
    void setUpUserSteps() {
        userSteps = new UserSteps(
                RequestSpecs.withAdminBasicAuth()
        );
    }

    @Test
    void searchAdminUserReturnsNonEmptyList() {
        UserSearchResponse response =
                userSteps.searchUsers("admin");

        softly.assertThat(response.results())
                .as("Users found by query")
                .isNotEmpty();

        softly.assertThat(response.results())
                .anyMatch(user -> "admin".equals(user.display()));
    }
}
