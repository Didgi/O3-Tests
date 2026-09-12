package api.requests.skelethon.interfaces;

import api.models.auth.request.Credentials;
import io.restassured.response.Response;

public interface AuthEndpoint {

    Response getSession(Credentials credentials);

    Response logout();
}
