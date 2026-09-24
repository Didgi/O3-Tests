package api.requests.skeleton.interfaces;

import api.models.auth.request.ChangePasswordCredentials;
import api.models.auth.request.Credentials;
import io.restassured.response.Response;

public interface AuthEndpoint {

    Response getSession(Credentials credentials);

    Response getSession(Credentials credentials, String sessionId);

    Response getSession();

    Response logout();

    Response logout(String sessionId);

    Response changePassword(ChangePasswordCredentials changePasswordCredentials, String sessionId);
}
