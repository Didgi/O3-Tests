package api.requests.skeleton.requesters;

import api.requests.endpoints.EndpointSpec;
import api.requests.skeleton.interfaces.PostSearchEndpoint;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

import static io.restassured.RestAssured.given;

public class PostSearchRequester<BODY> implements PostSearchEndpoint<BODY> {

    private final RequestSpecification requestSpecification;
    private final EndpointSpec<?> endpoint;

    public PostSearchRequester(
            RequestSpecification requestSpecification,
            EndpointSpec<?> endpoint
    ) {
        this.requestSpecification = Objects.requireNonNull(
                requestSpecification,
                "requestSpecification must not be null"
        );
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint must not be null");
    }

    @Override
    public Response search(BODY body) {
        RequestSpecification preparedRequest = given().spec(requestSpecification);
        if (body != null) {
            preparedRequest.body(body);
        }
        return preparedRequest.post(endpoint.pathTemplate());
    }
}
