package api.requests.skelethon.requesters;

import api.requests.skelethon.endpoints.CrudOperations;
import api.requests.skelethon.interfaces.NestedCrudEndpoint;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

import static io.restassured.RestAssured.given;

public class NestedCrudRequester<CREATE, UPDATE>
        implements NestedCrudEndpoint<CREATE, UPDATE> {

    private final RequestSpecification requestSpecification;
    private final CrudOperations<?> operations;

    public NestedCrudRequester(
            RequestSpecification requestSpecification,
            CrudOperations<?> operations
    ) {
        this.requestSpecification = Objects.requireNonNull(
                requestSpecification,
                "requestSpecification must not be null"
        );
        this.operations = Objects.requireNonNull(operations, "operations must not be null");
    }

    @Override
    public Response create(String parentId, CREATE request) {
        RequestSpecification preparedRequest = given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId);
        if (request != null) {
            preparedRequest.body(request);
        }
        return preparedRequest.post(operations.create().pathTemplate());
    }

    @Override
    public Response get(String parentId, String id, ReadOptions options) {
        RequestSpecification request = given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId)
                .pathParam("id", id);

        if (Objects.requireNonNull(options, "options must not be null").isExplicit()) {
            request.queryParam("v", options.representation());
        }

        return request.get(operations.get().pathTemplate());
    }

    @Override
    public Response update(String parentId, String id, UPDATE requestBody) {
        RequestSpecification preparedRequest = given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId)
                .pathParam("id", id);
        if (requestBody != null) {
            preparedRequest.body(requestBody);
        }
        return preparedRequest.post(operations.update().pathTemplate());
    }

    @Override
    public Response delete(String parentId, String id, DeleteMode mode) {
        RequestSpecification request = given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId)
                .pathParam("id", id);

        if (Objects.requireNonNull(mode, "mode must not be null") == DeleteMode.PURGE) {
            request.queryParam("purge", true);
        }

        return request.delete(operations.delete().pathTemplate());
    }
}
