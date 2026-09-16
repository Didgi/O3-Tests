package api.requests.skeleton.interfaces;

import io.restassured.response.Response;

public interface PostSearchEndpoint<BODY> {

    Response search(BODY body);
}
