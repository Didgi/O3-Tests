package api.requests.skeleton.interfaces;

import api.requests.skeleton.query.QueryParams;
import io.restassured.response.Response;

public interface SearchEndpoint<PARAMS extends QueryParams> {

    Response search(PARAMS params);
}
