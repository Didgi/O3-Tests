package api.requests.skelethon.requesters;

import api.models.auth.request.Credentials;
import api.models.auth.response.SessionResponse;
import api.requests.skelethon.endpoints.EndpointSpec;
import api.requests.skelethon.interfaces.AuthEndpoint;
import io.restassured.response.Response;

import java.util.Objects;

import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

public class SuccessfulAuthRequester {

    private final AuthEndpoint requester;
    private final EndpointSpec<SessionResponse> sessionEndpoint;

    public SuccessfulAuthRequester(
            AuthEndpoint requester,
            EndpointSpec<SessionResponse> sessionEndpoint
    ) {
        this.requester = Objects.requireNonNull(requester, "requester must not be null");
        this.sessionEndpoint = Objects.requireNonNull(
                sessionEndpoint,
                "sessionEndpoint must not be null"
        );
    }

    public SessionResponse getSession(Credentials credentials) {
        SessionResponse session = requester.getSession(credentials)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(sessionEndpoint.responseTypeRef());

        if (session == null || !session.authenticated()) {
            throw new AssertionError("Expected an authenticated OpenMRS session");
        }

        return session;
    }

    public Response logout() {
        Response response = requester.logout();
        response.then().statusCode(SC_NO_CONTENT);
        return response;
    }
}
