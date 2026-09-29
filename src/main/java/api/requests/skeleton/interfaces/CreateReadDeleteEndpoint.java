package api.requests.skeleton.interfaces;

import api.requests.skeleton.options.DeleteMode;
import api.requests.skeleton.options.ReadOptions;
import io.restassured.response.Response;

public interface CreateReadDeleteEndpoint<CREATE> {
    Response create(CREATE create);

    Response get(String id, ReadOptions readOptions);

    Response delete(String id, DeleteMode deleteMode);

    default Response get(String id) {
        return get(id, ReadOptions.defaults());
    }

    default Response delete(String id) {
        return delete(id, DeleteMode.DEFAULT);
    }
}
