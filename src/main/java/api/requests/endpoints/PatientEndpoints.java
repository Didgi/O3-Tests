package api.requests.endpoints;

import api.models.patients.PatientResponse;
import api.models.patients.PatientSearchResponse;
import io.restassured.common.mapper.TypeRef;

public class PatientEndpoints {

    public static final EndpointSpec<PatientResponse> CREATE =
            new EndpointSpec<>(
                    "/patient",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<PatientResponse> READ =
            new EndpointSpec<>(
                    "/patient/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<PatientResponse> UPDATE =
            new EndpointSpec<>(
                    "/patient/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>(
                    "/patient/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<PatientSearchResponse> SEARCH =
            new EndpointSpec<>(
                    "/patient",
                    new TypeRef<>() {
                    }
            );

    public static final CrudOperations<PatientResponse> CRUD =
            new CrudOperations<>(
                    CREATE,
                    READ,
                    UPDATE,
                    DELETE
            );

    private PatientEndpoints() {
    }
}