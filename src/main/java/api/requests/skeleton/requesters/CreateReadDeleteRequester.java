package api.requests.skeleton.requesters;

import api.requests.endpoints.CreateReadDeleteOperations;
import api.requests.skeleton.interfaces.CreateReadDeleteEndpoint;
import api.requests.skeleton.options.DeleteMode;
import api.requests.skeleton.options.ReadOptions;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

import static io.restassured.RestAssured.given;

public class CreateReadDeleteRequester<CREATE>
        implements CreateReadDeleteEndpoint<CREATE> {

    private final RequestSpecification requestSpecification;
    private final CreateReadDeleteOperations<?> operations;

    public CreateReadDeleteRequester(
            RequestSpecification requestSpecification,
            CreateReadDeleteOperations<?> operations
    ) {
        this.requestSpecification = Objects.requireNonNull(
                requestSpecification,
                "requestSpecification must not be null"
        );
        this.operations = Objects.requireNonNull(
                operations,
                "operations must not be null"
        );
    }

    @Override
    public Response create(CREATE create) {
        RequestSpecification request = given()
                .spec(requestSpecification);

        if (create != null) {
            request.body(create);
        }
        return request
                .post(operations.create().pathTemplate());
    }

    @Override
    public Response get(String id, ReadOptions options) {
        RequestSpecification request =
                given()
                        .spec(requestSpecification)
                        .pathParam("id", id);

        if (Objects.requireNonNull(options, "options must not be null").isExplicit()) {
            request.queryParam("v", options.representation());
        }

        return request
                .get(operations.get().pathTemplate());
    }

    @Override
    public Response delete(String id, DeleteMode mode) {
        RequestSpecification request =
                given()
                        .spec(requestSpecification)
                        .pathParam("id", id);

        if (Objects.requireNonNull(mode, "mode must not be null") == DeleteMode.PURGE) {
            request.queryParam("purge", true);
        }

        return request.delete(operations.delete().pathTemplate());
    }
}
