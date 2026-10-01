package api.requests.steps;

import api.models.order.*;
import api.requests.endpoints.OrderEndpoints;
import api.requests.skeleton.interfaces.CreateReadDeleteEndpoint;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulCreateReadDeleteRequester;
import api.requests.skeleton.requesters.SuccessfulSearchRequester;
import io.restassured.response.Response;

public class OrderSteps {
    private final SuccessfulCreateReadDeleteRequester<
            OrderCreateRequest,
            OrderResponse
            > successfulRequester;
    private final SuccessfulCreateReadDeleteRequester<
            DrugOrderCreateRequest,
            DrugOrderResponse
            > drugSuccessfulRequester;
    private final CreateReadDeleteEndpoint<
            OrderCreateRequest
            > rawRequester;

    private final SuccessfulSearchRequester<
            OrderSearchParams,
                OrderSearchResponse
                > searchRequester;

    public OrderSteps(RequesterFactory factory) {
        this.successfulRequester = factory.successfulCreateReadDelete(
                OrderEndpoints.OPERATIONS
        );
        this.rawRequester = factory.rawCreateReadDelete(
                OrderEndpoints.OPERATIONS
        );
        this.searchRequester = factory.successfulSearch(
                OrderEndpoints.SEARCH
        );
        this.drugSuccessfulRequester = factory.successfulCreateReadDelete(
                OrderEndpoints.DRUG_OPERATIONS
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

    public OrderSearchResponse searchOrderByPatient(String patientUuid) {
        return searchRequester
                .search(new OrderSearchParams(patientUuid));
    }

    public DrugOrderResponse createDrugOrder(
            DrugOrderCreateRequest request
    ) {
        return drugSuccessfulRequester.create(request);
    }

    public DrugOrderResponse getDrugOrder(String uuid) {
        return drugSuccessfulRequester.get(uuid);
    }
}
