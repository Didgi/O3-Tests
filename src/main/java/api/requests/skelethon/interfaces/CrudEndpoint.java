package api.requests.skelethon.interfaces;

import api.requests.skelethon.requesters.DeleteMode;
import api.requests.skelethon.requesters.ReadOptions;
import io.restassured.response.Response;

public interface CrudEndpoint<CREATE, UPDATE> {
    Response create(CREATE request);

    Response get(String id, ReadOptions options);

    Response update(String id, UPDATE request);

    Response delete(String id, DeleteMode mode);

    default Response get(String id) {
        return get(id, ReadOptions.defaults());
    }

    default Response delete(String id) {
        return delete(id, DeleteMode.DEFAULT);
    }
}
