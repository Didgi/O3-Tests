package api;

import api.config.Config;
import api.config.ResponseMessages;
import api.models.auth.request.ChangePasswordCredentials;
import api.models.auth.request.Credentials;
import api.models.auth.response.SessionResponse;
import api.models.user.UserSearchErrorResponse;
import api.models.user.UserSearchResponse;
import api.requests.skeleton.interfaces.AuthEndpoint;
import api.requests.skeleton.requesters.AuthRequester;
import api.requests.steps.ApiClient;
import api.specs.RequestSpecs;
import common.annotations.Bug;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static api.requests.endpoints.AuthEndpoints.SESSION;
import static api.requests.skeleton.requesters.AuthRequester.getReadyAuthCookie;
import static api.requests.skeleton.requesters.AuthRequester.getReadyResponseChangePassword;
import static org.apache.http.HttpStatus.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

//@EnabledIfSystemProperty(named = "openmrs.integration.enabled", matches = "true")
public class AuthTest extends BaseApiTest {

    static final String SESSION_COOKIE_NAME = Config.getProperty("cookie_session_name");

    static final Credentials ADMIN_CREDENTIALS = new Credentials(Config.getProperty("admin_username"),
            Config.getProperty("admin_password"));

    final int minPasswordSizeForTest = 8;
    final int maxPasswordSizeForTest = 16;

    @Test
    @DisplayName("Позитивный тест: успешная авторизация с получением сессии")
    public void getAuthSessionWithValidDataAdmin() {

        Allure.step("Выполняем запрос на авторизацию");
        AuthEndpoint rawRequester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);
        final Response rawResponse = rawRequester.getSession(ADMIN_CREDENTIALS);

        softly.assertThat(rawResponse.statusCode()).isEqualTo(SC_OK);

        Allure.step("Проверяем успешность авторизации");
        final SessionResponse sessionResponse = rawResponse.body().as(SessionResponse.class);

        softly.assertThat(sessionResponse.authenticated()).isTrue();
        softly.assertThat(sessionResponse.user()).isNotNull();
        softly.assertThat(rawResponse.cookie(SESSION_COOKIE_NAME).isBlank()).isFalse();
    }


    @Test
    @DisplayName("Позитивный тест: успешная авторизация с получением сессии и " +
            "выполнение запроса только с указанием сессии")
    public void getAuthSessionWithValidDataAdminAndUseSession() {
        String searchByAdmin = "admin";

        ApiClient adminCookie = ApiClient.adminCookie();

        Allure.step("Выполняем запрос на получение списка пользователей с указанием существующей сессии");
        final UserSearchResponse response = adminCookie.users().searchUsers(searchByAdmin);

        Allure.step("Проверяем, что ответ не пустой");
        softly.assertThat(response.results()).isNotEmpty();

    }


    @Bug(true)
    @Test
    @DisplayName("Позитивный тест: logout завершает сессию")
    public void logoutUserEndsSession() {
        String searchByAdmin = "admin";

        Allure.step("Выполняем запрос на авторизацию");
        AuthEndpoint rawRequester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);

        Allure.step("Проверяем успешность авторизации");
        final Response response = rawRequester.getSession(ADMIN_CREDENTIALS);

        softly.assertThat(response.statusCode()).isEqualTo(SC_OK);

        String cookie = response.cookie(SESSION_COOKIE_NAME);

        softly.assertThat(cookie).isNotBlank();

        ApiClient adminCookie = ApiClient.userCookie(cookie);

        Allure.step("Выполняем запрос на получение списка пользователей с указанием существующей сессии");
        UserSearchResponse authorizedResponse = adminCookie.users().searchUsers(searchByAdmin);

        Allure.step("Проверяем, что ответ не пустой");
        softly.assertThat(authorizedResponse.results()).isNotEmpty();

        Allure.step("Выполняем logout");
        Response logoutResponse = rawRequester.logout(cookie);

        softly.assertThat(logoutResponse.statusCode()).isEqualTo(SC_NO_CONTENT);

        Allure.step("Выполняем запрос на получение списка пользователей с удалённой сессией");
        UserSearchErrorResponse errorResponse =
                adminCookie.users().searchUsersUnauthorized(searchByAdmin);

        Allure.step("Проверяем отображение ошибки в выполненном запросе");
        softly.assertThat(errorResponse.error()).isNotNull();

        softly.assertThat(errorResponse.error().message()).contains(String.valueOf(SC_UNAUTHORIZED));
    }

    @Disabled
    @Test
    @Execution(value = SAME_THREAD)
    @DisplayName("Позитивный тест: пользователь админ изменяет пароль самому себе и авторизуется по новому паролю")
    public void selfServicePasswordChangeAndGetSessionViaNewCredentialsSuccessfully() {

        Allure.step("Выполняем запрос на авторизацию");
        final String adminCookie = getReadyAuthCookie(ADMIN_CREDENTIALS);

        String newRandomPassword = new Faker().credentials().password(minPasswordSizeForTest, maxPasswordSizeForTest, true);

        Allure.step("Выполняем запрос на изменение пароля");
        final ChangePasswordCredentials updatePassword = new ChangePasswordCredentials(Config.getProperty("admin_password"), newRandomPassword);

        final Response readyResponseChangePassword = getReadyResponseChangePassword(updatePassword, adminCookie);

        Allure.step("Проверяем, что запрос выполнен успешно");
        softly.assertThat(readyResponseChangePassword.statusCode()).isEqualTo(SC_OK);

        AuthEndpoint rawGetSessionRequester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);

        Allure.step("Выполняем запрос на авторизацию с обновлённым паролем");
        final Response rawResponse = rawGetSessionRequester.getSession(new Credentials(Config.getProperty("admin_username"), newRandomPassword));

        Allure.step("Проверяем успешность авторизации");
        softly.assertThat(rawResponse.statusCode()).isEqualTo(SC_OK);

        final SessionResponse sessionResponse = rawResponse.body().as(SessionResponse.class);

        softly.assertThat(sessionResponse.authenticated()).isTrue();
        softly.assertThat(sessionResponse.user()).isNotNull();
        softly.assertThat(rawResponse.cookie(SESSION_COOKIE_NAME).isBlank()).isFalse();

        Allure.step("Изменяем пароль обратно на прежний");
        final ChangePasswordCredentials updatePasswordRevert = new ChangePasswordCredentials(newRandomPassword, Config.getProperty("admin_password"));

        final Response readyResponseChangePasswordRevert = getReadyResponseChangePassword(updatePasswordRevert, rawResponse.cookie(SESSION_COOKIE_NAME));

        softly.assertThat(readyResponseChangePasswordRevert.statusCode()).isEqualTo(SC_OK);
    }

    @Disabled
    @Test
    @Execution(value = SAME_THREAD)
    @DisplayName("Негативный тест: пользователь админ изменяет пароль самому себе и не авторизуется по старому паролю")
    public void selfServicePasswordChangeAndCheckGetSessionViaNewCredentialsNotSuccessfully() {

        Allure.step("Выполняем запрос на авторизацию");
        final String adminCookie = getReadyAuthCookie(ADMIN_CREDENTIALS);

        String newRandomPassword = new Faker().credentials().password(minPasswordSizeForTest, maxPasswordSizeForTest, true);

        Allure.step("Выполняем запрос на изменение пароля");
        final ChangePasswordCredentials updatePassword = new ChangePasswordCredentials(Config.getProperty("admin_password"), newRandomPassword);

        final Response readyResponseChangePassword = getReadyResponseChangePassword(updatePassword, adminCookie);

        Allure.step("Проверяем, что запрос выполнен успешно");
        softly.assertThat(readyResponseChangePassword.statusCode()).isEqualTo(SC_OK);

        Allure.step("Выполняем запрос на авторизацию с старым паролем");
        AuthEndpoint rawGetSessionRequester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);

        final Response rawResponse = rawGetSessionRequester.getSession(ADMIN_CREDENTIALS);

        Allure.step("Проверяем, что пользователь не авторизован");
        softly.assertThat(rawResponse.statusCode()).isEqualTo(SC_OK);

        final SessionResponse sessionResponse = rawResponse.body().as(SessionResponse.class);

        softly.assertThat(sessionResponse.authenticated()).isFalse();
        softly.assertThat(sessionResponse.user()).isNull();
        softly.assertThat(rawResponse.cookie(SESSION_COOKIE_NAME).isBlank()).isFalse();

        Allure.step("Изменяем пароль обратно на прежний");

        final ChangePasswordCredentials updatePasswordRevert = new ChangePasswordCredentials(newRandomPassword, Config.getProperty("admin_password"));

        final Response readyResponseChangePasswordRevert = getReadyResponseChangePassword(updatePasswordRevert, adminCookie);

        softly.assertThat(readyResponseChangePasswordRevert.statusCode()).isEqualTo(SC_OK);
    }

    @Test
    @DisplayName("Негативный тест: повторная авторизация не создаёт новую сессию")
    public void getTwiceAuthSessionWithValidDataAdminReturnSameSessionValue() {

        Allure.step("Выполняем запрос на авторизацию");
        AuthEndpoint rawRequester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);
        final Response rawResponse = rawRequester.getSession(ADMIN_CREDENTIALS);

        final String originalCookie = rawResponse.cookie(SESSION_COOKIE_NAME);

        Allure.step("Выполняем запрос на повторную авторизацию и проверяем сессию");
        final Response rawResponseAgain = rawRequester.getSession(ADMIN_CREDENTIALS, originalCookie);

        softly.assertThat(rawResponseAgain.cookie(SESSION_COOKIE_NAME)).isNull();

    }


    private static Stream<Arguments> invalidCredentials() {
        return Stream.of(
                Arguments.of(new Faker().name().firstName(), Config.getProperty("admin_password")),
                Arguments.of(Config.getProperty("admin_username"), new Faker().name().lastName()),
                Arguments.of(new Faker().name().firstName(), new Faker().name().lastName())
        );
    }

    @MethodSource("invalidCredentials")
    @ParameterizedTest
    @DisplayName("Негативный тест: отказ в авторизации с несуществующими данными и получение не авторизованной сессии")
    public void getAuthSessionWithInvalidDataAdmin(String username, String password) {

        Allure.step("Выполняем запрос на авторизацию с невалидными данными");
        AuthEndpoint requester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);

        Response rawResponse = requester.getSession(new Credentials(username, password));

        Allure.step("Проверяем, что пользователь не авторизован");
        softly.assertThat(rawResponse.statusCode()).isEqualTo(SC_OK);

        final SessionResponse sessionResponse = rawResponse.body().as(SessionResponse.class);
        softly.assertThat(sessionResponse.authenticated()).isFalse();
        softly.assertThat(sessionResponse.user()).isNull();

        softly.assertThat(rawResponse.cookie(SESSION_COOKIE_NAME).isBlank()).isFalse();
    }

    @Test
    @DisplayName("Негативный тест: отказ в выполнении запроса с не авторизованной сессией")
    public void getAuthSessionWithInvalidDataAdminAndUseSession() {

        Allure.step("Выполняем запрос на авторизацию с невалидными данными");
        String searchByAdmin = "admin";

        AuthEndpoint requester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);

        Response rawResponse = requester.
                getSession(new Credentials(new Faker().name().firstName(), new Faker().name().lastName()));

        softly.assertThat(rawResponse.statusCode()).isEqualTo(SC_OK);

        final String cookie = rawResponse.cookie(SESSION_COOKIE_NAME);

        final ApiClient invalidAuthCookie = ApiClient.userCookie(cookie);

        Allure.step("Выполнение получение списка пользователей с не авторизованной сессией");
        final UserSearchErrorResponse userSearchErrorResponse = invalidAuthCookie.users().searchUsersUnauthorized(searchByAdmin);

        Allure.step("Проверяем отображение ошибки");
        softly.assertThat(userSearchErrorResponse.error().message()).isEqualTo(ResponseMessages.GET_USER_WITH_UNAUTHORIZATED_DATA_MESSAGE);
        softly.assertThat(userSearchErrorResponse.error().rawMessage()).isEqualTo(ResponseMessages.GET_USER_WITH_UNAUTHORIZATED_DATA_RAW_MESSAGE);
        softly.assertThat(userSearchErrorResponse.error().translatedMessage()).isEqualTo(ResponseMessages.GET_USER_WITH_UNAUTHORIZATED_DATA_TRANSLATED_MESSAGE);

    }
}
