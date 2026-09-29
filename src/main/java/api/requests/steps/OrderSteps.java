package api.requests.steps;

import api.models.order.OrderCreateRequest;
import api.models.order.OrderResponse;
import api.requests.endpoints.OrderEndpoints;
import api.requests.skeleton.interfaces.CreateReadDeleteEndpoint;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulCreateReadDeleteRequester;
import io.restassured.response.Response;

public class OrderSteps {
    private final SuccessfulCreateReadDeleteRequester<
            OrderCreateRequest,
            OrderResponse
            > successfulRequester;
    private final CreateReadDeleteEndpoint<
            OrderCreateRequest
            > rawRequester;

    public OrderSteps(RequesterFactory factory) {
        this.successfulRequester = factory.successfulCreateReadDelete(
                OrderEndpoints.OPERATIONS
        );
        this.rawRequester = factory.rawCreateReadDelete(
                OrderEndpoints.OPERATIONS
        );
    }

    public OrderResponse createOrder(
            OrderCreateRequest request
    ) {
        return successfulRequester.create(request);
    }

    public OrderResponse getOrder(String uuid) {
        return successfulRequester.get(uuid);
    }

    public void deleteOrder(String uuid) {
        successfulRequester.delete(uuid);
    }

    public Response createOrderRaw(
            OrderCreateRequest request
    ) {
        return rawRequester.create(request);
    }
}
