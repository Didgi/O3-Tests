package api.requests.skeleton.interfaces;

import io.restassured.response.Response;

public interface IdPostEndpoint<BODY> {

    Response post(String id, BODY body);
}
