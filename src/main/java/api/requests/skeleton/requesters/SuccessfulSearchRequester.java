package api.requests.skeleton.requesters;

import api.requests.endpoints.EndpointSpec;
import api.requests.skeleton.interfaces.SearchEndpoint;
import api.requests.skeleton.query.QueryParams;

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
