package api.requests.skeleton.requesters;

import api.requests.endpoints.EndpointSpec;
import api.requests.skeleton.interfaces.NestedSearchEndpoint;
import api.requests.skeleton.query.QueryParams;

import java.util.Objects;

import static org.apache.http.HttpStatus.SC_OK;

public class SuccessfulNestedSearchRequester<PARAMS extends QueryParams, RES> {

    private final NestedSearchEndpoint<PARAMS> requester;
    private final EndpointSpec<RES> endpoint;

    public SuccessfulNestedSearchRequester(
            NestedSearchEndpoint<PARAMS> requester,
            EndpointSpec<RES> endpoint
    ) {
        this.requester = Objects.requireNonNull(requester, "requester must not be null");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint must not be null");
    }

    public RES search(String parentId, PARAMS params) {
        return requester.search(parentId, params)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(endpoint.responseTypeRef());
    }
}
