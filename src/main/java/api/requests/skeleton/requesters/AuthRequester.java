package api.requests.skeleton.requesters;

import api.config.Config;
import api.models.auth.request.ChangePasswordCredentials;
import api.models.auth.request.Credentials;
import api.requests.endpoints.EndpointSpec;
import api.requests.skeleton.interfaces.AuthEndpoint;
import api.specs.RequestSpecs;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

import static api.requests.endpoints.AuthEndpoints.CHANGE_PASSWORD;
import static api.requests.endpoints.AuthEndpoints.SESSION;
import static io.restassured.RestAssured.given;

public class AuthRequester implements AuthEndpoint {

    private final RequestSpecification requestSpecification;
    private final EndpointSpec<?> sessionEndpoint;

    public AuthRequester(
            RequestSpecification requestSpecification,
            EndpointSpec<?> sessionEndpoint
    ) {
        this.requestSpecification = Objects.requireNonNull(
                requestSpecification,
                "requestSpecification must not be null"
        );
        this.sessionEndpoint = Objects.requireNonNull(
                sessionEndpoint,
                "sessionEndpoint must not be null"
        );
    }

    @Step("Получение сессии по логину и паролю")
    @Override
    public Response getSession(Credentials credentials) {
        Credentials requiredCredentials = Objects.requireNonNull(
                credentials,
                "credentials must not be null"
        );

        return given()
                .spec(requestSpecification)
                .auth()
                .preemptive()
                .basic(requiredCredentials.username(), requiredCredentials.password())
                .get(sessionEndpoint.pathTemplate());
    }

    @Step("Получение сессии по логину и паролю и сессии")
    @Override
    public Response getSession(
            Credentials credentials,
            String sessionId
    ) {
        return given()
                .spec(requestSpecification)
                .cookie(Config.getProperty("cookie_session_name"), sessionId)
                .auth()
                .preemptive()
                .basic(
                        credentials.username(),
                        credentials.password()
                )
                .get(sessionEndpoint.pathTemplate());
    }

    @Step("Получение сессии без передачи логина и пароля")
    public Response getSession() {
        return given()
                .spec(requestSpecification)
                .get(sessionEndpoint.pathTemplate());
    }

    @Step("Выполнение logout")
    @Override
    public Response logout() {
        return given()
                .spec(requestSpecification)
                .delete(sessionEndpoint.pathTemplate());
    }

    @Step("Выполнение logout")
    @Override
    public Response logout(String sessionId) {
        return given()
                .spec(requestSpecification)
                .cookie(Config.getProperty("cookie_session_name"), sessionId)
                .delete(sessionEndpoint.pathTemplate());
    }


    @Step("Изменение пароля")
    @Override
    public Response changePassword(ChangePasswordCredentials changePasswordCredentials, String sessionId) {
        return given()
                .spec(requestSpecification)
                .cookie(Config.getProperty("cookie_session_name"), sessionId)
                .body(changePasswordCredentials)
                .post(sessionEndpoint.pathTemplate());
    }

    @Step("Авторизация и получение готовой куки сессии")
    public static String getReadyAuthCookie(Credentials credentials) {
        AuthEndpoint rawRequester = new AuthRequester(RequestSpecs.baseRequest(), SESSION);

        final Response response = rawRequester.getSession(credentials);

        return response.cookie(Config.getProperty("cookie_session_name"));
    }

    public static Response getReadyResponseChangePassword(ChangePasswordCredentials changePasswordCredentials, String sessionId) {
        AuthEndpoint rawChangePasswordRequester = new AuthRequester(RequestSpecs.baseRequest(), CHANGE_PASSWORD);

        return rawChangePasswordRequester.changePassword(changePasswordCredentials, sessionId);

    }
}
