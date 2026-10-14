package api.requests.endpoints;

import api.models.order.DrugOrderResponse;
import api.models.order.OrderResponse;
import api.models.order.OrderSearchResponse;
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

    public static final EndpointSpec<DrugOrderResponse> DRUG_CREATE =
            new EndpointSpec<>(
                    "/order",
                    new TypeRef<>() {
                    }
            );

    public static final EndpointSpec<DrugOrderResponse> DRUG_GET =
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

    public static final EndpointSpec<OrderSearchResponse> SEARCH =
            new EndpointSpec<>(
                    "/order",
                    new TypeRef<>() {
                    }
            );

    public static final CreateReadDeleteOperations<OrderResponse> OPERATIONS =
            new CreateReadDeleteOperations<>(
                    CREATE,
                    GET,
                    DELETE
            );

    public static final CreateReadDeleteOperations<DrugOrderResponse>
            DRUG_OPERATIONS =
            new CreateReadDeleteOperations<>(
                    DRUG_CREATE,
                    DRUG_GET,
                    DELETE
            );
}
