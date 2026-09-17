package api.requests.endpoints;

import api.models.visit.VisitCreateResponse;
import io.restassured.common.mapper.TypeRef;

public final class VisitEndpoints {

    public static final EndpointSpec<VisitCreateResponse> CREATE =
            new EndpointSpec<>(
                    "/visit",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<VisitCreateResponse> READ =
            new EndpointSpec<>(
                    "/visit/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<VisitCreateResponse> UPDATE =
            new EndpointSpec<>(
                    "/visit/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>(
                    "/visit/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final CrudOperations<VisitCreateResponse> CRUD =
            new CrudOperations<>(CREATE, READ, UPDATE, DELETE);

    private VisitEndpoints() {
    }
}
