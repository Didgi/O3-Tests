package api.requests.skeleton.requesters;

import api.requests.endpoints.CreateReadDeleteOperations;
import api.requests.skeleton.interfaces.CreateReadDeleteEndpoint;
import api.requests.skeleton.options.DeleteMode;
import api.requests.skeleton.options.ReadOptions;
import io.restassured.response.Response;

import java.util.Objects;

import static org.apache.http.HttpStatus.*;

public class SuccessfulCreateReadDeleteRequester<CREATE, RES> {
    private final CreateReadDeleteEndpoint<CREATE> requester;
    private final CreateReadDeleteOperations<RES> operations;

    public SuccessfulCreateReadDeleteRequester(
            CreateReadDeleteEndpoint<CREATE> requester,
            CreateReadDeleteOperations<RES> operations
    ) {
        this.requester = Objects.requireNonNull(requester, "requester must not be null");
        this.operations = Objects.requireNonNull(operations, "operations must not be null");
    }

    public RES create(CREATE request) {
        return requester.create(request)
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(operations.create().responseTypeRef());
    }

    public RES get(String id, ReadOptions options) {
        return requester.get(id, options)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(operations.get().responseTypeRef());
    }

    public RES get(String id) {
        return get(id, ReadOptions.defaults());
    }

    public Response delete(String id, DeleteMode mode) {
        Response response = requester.delete(id, mode);
        response.then().statusCode(SC_NO_CONTENT);

        return response;
    }

    public Response delete(String id) {
        return delete(id, DeleteMode.DEFAULT);
    }
}
