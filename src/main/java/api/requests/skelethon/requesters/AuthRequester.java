package api.requests.skelethon.requesters;

import api.models.auth.request.Credentials;
import api.requests.skelethon.endpoints.EndpointSpec;
import api.requests.skelethon.interfaces.AuthEndpoint;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

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

    @Override
    public Response logout() {
        return given()
                .spec(requestSpecification)
                .delete(sessionEndpoint.pathTemplate());
    }
}
