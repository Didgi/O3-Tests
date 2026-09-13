package api.requests.skelethon.endpoints;

import api.models.observations.ObservationResponse;
import api.models.observations.ObservationSearchResponse;
import io.restassured.common.mapper.TypeRef;

public class ObservationEndpoints {
    public static final EndpointSpec<ObservationResponse> CREATE =
            new EndpointSpec<>(
                    "/obs",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<ObservationResponse> READ =
            new EndpointSpec<>(
                    "/obs/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<ObservationResponse> UPDATE =
            new EndpointSpec<>(
                    "/obs/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>(
                    "/obs/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<ObservationSearchResponse> SEARCH =
            new EndpointSpec<>(
                    "/obs",
                    new TypeRef<>() {}
            );

    public static final CrudOperations<ObservationResponse> CRUD =
            new CrudOperations<>(CREATE, READ, UPDATE, DELETE);

    private ObservationEndpoints() {
    }
}
