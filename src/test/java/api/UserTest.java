package api;

import api.models.user.UserSearchResponse;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserTest extends BaseApiTest {

    @Test
    @Issue("TRACK-012")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Поиск админа возвращает не пустой результат")
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
