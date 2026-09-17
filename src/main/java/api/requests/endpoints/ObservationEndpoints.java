package api.requests.endpoints;

import api.models.observations.ObservationResponse;
import api.models.observations.ObservationSearchResponse;
import io.restassured.common.mapper.TypeRef;

public final class ObservationEndpoints {

    private static final String OBS_PATH = "/obs";
    private static final String OBS_BY_ID_PATH = OBS_PATH + "/{id}";

    public static final EndpointSpec<ObservationResponse> CREATE =
            new EndpointSpec<>(OBS_PATH, new TypeRef<>() {
            });

    public static final EndpointSpec<ObservationResponse> READ =
            new EndpointSpec<>(OBS_BY_ID_PATH, new TypeRef<>() {
            });

    public static final EndpointSpec<ObservationResponse> UPDATE =
            new EndpointSpec<>(OBS_BY_ID_PATH, new TypeRef<>() {
            });

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>(OBS_BY_ID_PATH, new TypeRef<>() {
            });

    public static final EndpointSpec<ObservationSearchResponse> SEARCH =
            new EndpointSpec<>(OBS_PATH, new TypeRef<>() {
            });

    public static final CrudOperations<ObservationResponse> CRUD =
            new CrudOperations<>(CREATE, READ, UPDATE, DELETE);

    private ObservationEndpoints() {
    }
}
