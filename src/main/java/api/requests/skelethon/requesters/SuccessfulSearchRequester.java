package api.requests.skelethon.requesters;

import api.requests.skelethon.endpoints.EndpointSpec;
import api.requests.skelethon.interfaces.SearchEndpoint;
import api.requests.skelethon.query.QueryParams;

import java.util.Objects;

import static org.apache.http.HttpStatus.SC_OK;

public class SuccessfulSearchRequester<PARAMS extends QueryParams, RES> {

    private final SearchEndpoint<PARAMS> requester;
    private final EndpointSpec<RES> endpoint;

    public SuccessfulSearchRequester(
            SearchEndpoint<PARAMS> requester,
            EndpointSpec<RES> endpoint
    ) {
        this.requester = Objects.requireNonNull(requester, "requester must not be null");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint must not be null");
    }

    public RES search(PARAMS params) {
        return requester.search(params)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(endpoint.responseTypeRef());
    }
}
