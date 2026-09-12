package api.requests.skelethon.interfaces;

import api.requests.skelethon.query.QueryParams;
import io.restassured.response.Response;

public interface NestedSearchEndpoint<PARAMS extends QueryParams> {

    Response search(String parentId, PARAMS params);
}
