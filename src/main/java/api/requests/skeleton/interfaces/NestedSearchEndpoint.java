package api.requests.skeleton.interfaces;

import api.requests.skeleton.query.QueryParams;
import io.restassured.response.Response;

public interface NestedSearchEndpoint<PARAMS extends QueryParams> {

    Response search(String parentId, PARAMS params);
}
