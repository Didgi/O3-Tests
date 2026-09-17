package api.requests.skeleton.requesters;

import api.requests.endpoints.EndpointSpec;
import api.requests.skeleton.interfaces.SearchEndpoint;
import api.requests.skeleton.query.QueryParams;

import java.util.Objects;

import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

public class ErrorSearchRequester<PARAMS extends QueryParams, ERROR_RESPONSE> {

    private final SearchEndpoint<PARAMS> requester;
    private final EndpointSpec<ERROR_RESPONSE> endpoint;

    public ErrorSearchRequester(
            SearchEndpoint<PARAMS> requester,
            EndpointSpec<ERROR_RESPONSE> endpoint
    ) {
        this.requester = Objects.requireNonNull(
                requester,
                "requester must not be null"
        );

        this.endpoint = Objects.requireNonNull(
                endpoint,
                "endpoint must not be null"
        );
    }

    public ERROR_RESPONSE search(PARAMS params) {
        return requester.search(params)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .extract()
                .as(endpoint.responseTypeRef());
    }
}
