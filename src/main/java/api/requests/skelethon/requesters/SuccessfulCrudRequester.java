package api.requests.skelethon.requesters;

import api.requests.skelethon.endpoints.CrudOperations;
import api.requests.skelethon.interfaces.CrudEndpoint;
import io.restassured.response.Response;

import java.util.Objects;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

public class SuccessfulCrudRequester<CREATE, UPDATE, RES> {

    private final CrudEndpoint<CREATE, UPDATE> requester;
    private final CrudOperations<RES> operations;

    public SuccessfulCrudRequester(
            CrudEndpoint<CREATE, UPDATE> requester,
            CrudOperations<RES> operations
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

    public RES update(String id, UPDATE request) {
        return requester.update(id, request)
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(operations.update().responseTypeRef());
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
