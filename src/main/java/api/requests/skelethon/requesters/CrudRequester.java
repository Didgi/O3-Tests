package api.requests.skelethon.requesters;

import api.requests.skelethon.endpoints.CrudOperations;
import api.requests.skelethon.interfaces.CrudEndpoint;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

import static io.restassured.RestAssured.given;

public class CrudRequester<CREATE, UPDATE>
        implements CrudEndpoint<CREATE, UPDATE> {

    private final RequestSpecification requestSpecification;
    private final CrudOperations<?> operations;

    public CrudRequester(
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
    public Response create(CREATE request) {
        RequestSpecification preparedRequest = given().spec(requestSpecification);
        if (request != null) {
            preparedRequest.body(request);
        }
        return preparedRequest.post(operations.create().pathTemplate());
    }

    @Override
    public Response get(String id, ReadOptions options) {
        RequestSpecification request = given()
                .spec(requestSpecification)
                .pathParam("id", id);

        if (Objects.requireNonNull(options, "options must not be null").isExplicit()) {
            request.queryParam("v", options.representation());
        }

        return request.get(operations.get().pathTemplate());
    }

    @Override
    public Response update(String id, UPDATE request) {
        RequestSpecification preparedRequest = given()
                .spec(requestSpecification)
                .pathParam("id", id);
        if (request != null) {
            preparedRequest.body(request);
        }
        return preparedRequest.post(operations.update().pathTemplate());
    }

    @Override
    public Response delete(String id, DeleteMode mode) {
        RequestSpecification request = given()
                .spec(requestSpecification)
                .pathParam("id", id);

        if (Objects.requireNonNull(mode, "mode must not be null") == DeleteMode.PURGE) {
            request.queryParam("purge", true);
        }

        return request.delete(operations.delete().pathTemplate());
    }
}
