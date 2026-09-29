package api.requests.endpoints;

import api.models.order.OrderResponse;
import io.restassured.common.mapper.TypeRef;

public final class OrderEndpoints {
    private OrderEndpoints() {
    }

    public static final EndpointSpec<OrderResponse> CREATE =
            new EndpointSpec<>(
                    "/order",
                    new TypeRef<>() {
                    });

    public static final EndpointSpec<OrderResponse> GET =
            new EndpointSpec<>(
                    "/order/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>(
                    "/order/{id}",
                    new TypeRef<>() {
                    }
            );

    public static final CreateReadDeleteOperations<OrderResponse> OPERATIONS =
            new CreateReadDeleteOperations<>(
                    CREATE,
                    GET,
                    DELETE
            );
}
