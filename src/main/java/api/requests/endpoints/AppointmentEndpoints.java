package api.requests.endpoints;

import api.models.appointment.AppointmentResponse;
import io.restassured.common.mapper.TypeRef;

import java.util.List;

public final class AppointmentEndpoints {

    public static final EndpointSpec<AppointmentResponse> SEARCH =
            new EndpointSpec<>(
                    "/appointment",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<List<AppointmentResponse>> SEARCH_BY_DATE =
            new EndpointSpec<>(
                    "/appointments",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<List<AppointmentResponse>> SEARCH_BY_PATIENT =
            new EndpointSpec<>(
                    "/appointments/search",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<AppointmentResponse> CREATE =
            new EndpointSpec<>(
                    "/appointment",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<AppointmentResponse> READ =
            new EndpointSpec<>(
                    "/appointment/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<AppointmentResponse> UPDATE =
            new EndpointSpec<>(
                    "/appointment/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>(
                    "/appointment/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final CrudOperations<AppointmentResponse> CRUD =
            new CrudOperations<>(CREATE, READ, UPDATE, DELETE);

    public static final EndpointSpec<AppointmentResponse> STATUS_CHANGE =
            new EndpointSpec<>(
                    "/appointments/{id}/status-change",
                    new TypeRef<>() {
                    }
            );

    private AppointmentEndpoints() {
    }
}
