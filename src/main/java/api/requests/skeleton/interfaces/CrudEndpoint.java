package api.requests.skeleton.interfaces;

import io.restassured.response.Response;

public interface CrudEndpoint<CREATE, UPDATE>
        extends CreateReadDeleteEndpoint<CREATE> {

    Response update(String id, UPDATE request);

}
