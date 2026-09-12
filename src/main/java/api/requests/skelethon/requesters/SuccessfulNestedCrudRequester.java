package api.requests.skelethon.requesters;

import api.requests.skelethon.endpoints.CrudOperations;
import api.requests.skelethon.interfaces.NestedCrudEndpoint;
import io.restassured.response.Response;

import java.util.Objects;

import static org.apache.http.HttpStatus.*;

public class SuccessfulNestedCrudRequester<CREATE, UPDATE, RES> {

    private final NestedCrudEndpoint<CREATE, UPDATE> requester;
    private final CrudOperations<RES> operations;

    public SuccessfulNestedCrudRequester(
            NestedCrudEndpoint<CREATE, UPDATE> requester,
            CrudOperations<RES> operations
    ) {
        this.requester = Objects.requireNonNull(requester, "requester must not be null");
        this.operations = Objects.requireNonNull(operations, "operations must not be null");
    }

    public RES create(String parentId, CREATE request) {
        return requester.create(parentId, request)
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(operations.create().responseTypeRef());
    }

    public RES get(String parentId, String id, ReadOptions options) {
        return requester.get(parentId, id, options)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(operations.get().responseTypeRef());
    }

    public RES get(String parentId, String id) {
        return get(parentId, id, ReadOptions.defaults());
    }

    public RES update(String parentId, String id, UPDATE request) {
        return requester.update(parentId, id, request)
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(operations.update().responseTypeRef());
    }

    public Response delete(String parentId, String id, DeleteMode mode) {
        Response response = requester.delete(parentId, id, mode);
        response.then().statusCode(SC_NO_CONTENT);
        return response;
    }

    public Response delete(String parentId, String id) {
        return delete(parentId, id, DeleteMode.DEFAULT);
    }
}
