package api;

import api.models.user.UserSearchResponse;
import org.junit.jupiter.api.Test;

public class UserTest extends BaseApiTest {

    @Test
    void searchAdminUserReturnsNonEmptyList() {
        UserSearchResponse response =
                admin.users().searchUsers("admin");

        softly.assertThat(response.results())
                .as("Users found by query")
                .isNotEmpty();

        softly.assertThat(response.results())
                .anyMatch(user -> "admin".equals(user.display()));
    }
}
