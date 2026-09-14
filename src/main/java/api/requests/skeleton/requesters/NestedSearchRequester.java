package api.requests.skeleton.requesters;

import api.requests.endpoints.EndpointSpec;
import api.requests.skeleton.interfaces.NestedSearchEndpoint;
import api.requests.skeleton.query.QueryParams;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.Objects;

import static io.restassured.RestAssured.given;

public class NestedSearchRequester<PARAMS extends QueryParams>
        implements NestedSearchEndpoint<PARAMS> {

    private final RequestSpecification requestSpecification;
    private final EndpointSpec<?> endpoint;

    public NestedSearchRequester(
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
    public Response search(String parentId, PARAMS params) {
        Map<String, ?> queryParams = Objects.requireNonNull(
                Objects.requireNonNull(params, "params must not be null").asMap(),
                "params.asMap() must not return null"
        );

        return given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId)
                .queryParams(queryParams)
                .get(endpoint.pathTemplate());
    }
}
