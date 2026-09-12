package api.requests.skelethon.interfaces;

import api.requests.skelethon.requesters.DeleteMode;
import api.requests.skelethon.requesters.ReadOptions;
import io.restassured.response.Response;

public interface NestedCrudEndpoint<CREATE, UPDATE> {

    Response create(String parentId, CREATE request);

    Response get(String parentId, String id, ReadOptions options);

    Response update(String parentId, String id, UPDATE request);

    Response delete(String parentId, String id, DeleteMode mode);

    default Response get(String parentId, String id) {
        return get(parentId, id, ReadOptions.defaults());
    }

    default Response delete(String parentId, String id) {
        return delete(parentId, id, DeleteMode.DEFAULT);
    }
}
