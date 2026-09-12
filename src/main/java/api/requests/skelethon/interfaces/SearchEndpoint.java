package api.requests.skelethon.interfaces;

import api.requests.skelethon.query.QueryParams;
import io.restassured.response.Response;

public interface SearchEndpoint<PARAMS extends QueryParams> {

    Response search(PARAMS params);
}
