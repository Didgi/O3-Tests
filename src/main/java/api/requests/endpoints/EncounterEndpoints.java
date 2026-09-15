package api.requests.endpoints;

import api.models.encounter.EncounterResponse;
import io.restassured.common.mapper.TypeRef;

public class EncounterEndpoints {

    public static final EndpointSpec<EncounterResponse> CREATE =
            new EndpointSpec<>(
                    "/encounter",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<EncounterResponse> READ =
            new EndpointSpec<>(
                    "/encounter/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<EncounterResponse> UPDATE =
            new EndpointSpec<>(
                    "/encounter/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>(
                    "/encounter/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final CrudOperations<EncounterResponse> CRUD =
            new CrudOperations<>(CREATE, READ, UPDATE, DELETE);

    private EncounterEndpoints() {
    }
}
