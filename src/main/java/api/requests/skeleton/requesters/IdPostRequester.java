package api.requests.skeleton.requesters;

import api.requests.endpoints.EndpointSpec;
import api.requests.skeleton.interfaces.IdPostEndpoint;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

import static io.restassured.RestAssured.given;

public class IdPostRequester<BODY> implements IdPostEndpoint<BODY> {

    private final RequestSpecification requestSpecification;
    private final EndpointSpec<?> endpoint;

    public IdPostRequester(
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
    public Response post(String id, BODY body) {
        RequestSpecification preparedRequest = given()
                .spec(requestSpecification)
                .pathParam("id", id);
        if (body != null) {
            preparedRequest.body(body);
        }
        return preparedRequest.post(endpoint.pathTemplate());
    }
}
