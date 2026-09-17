package api.requests.skeleton.requesters;

import api.requests.endpoints.EndpointSpec;
import api.requests.skeleton.interfaces.PostSearchEndpoint;

import java.util.Objects;

import static org.apache.http.HttpStatus.SC_OK;

public class SuccessfulPostSearchRequester<BODY, RES> {

    private final PostSearchEndpoint<BODY> requester;
    private final EndpointSpec<RES> endpoint;

    public SuccessfulPostSearchRequester(
            PostSearchEndpoint<BODY> requester,
            EndpointSpec<RES> endpoint
    ) {
        this.requester = Objects.requireNonNull(requester, "requester must not be null");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint must not be null");
    }

    public RES search(BODY body) {
        return requester.search(body)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(endpoint.responseTypeRef());
    }
}
