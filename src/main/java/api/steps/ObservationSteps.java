package api.steps;

import api.models.observations.*;
import api.requests.skelethon.endpoints.ObservationEndpoints;
import api.requests.skelethon.requesters.DeleteMode;
import api.requests.skelethon.requesters.RequesterFactory;
import api.requests.skelethon.requesters.SuccessfulCrudRequester;
import api.requests.skelethon.requesters.SuccessfulSearchRequester;
import io.restassured.response.Response;

public class ObservationSteps {
    private final SuccessfulSearchRequester<
            ObservationSearchParams,
            ObservationSearchResponse
            > successfulSearchRequester;
    private final SuccessfulCrudRequester<
            ObservationCreateRequest,
            ObservationUpdateRequest,
            ObservationResponse
            > successfulCrudRequester;

    public ObservationSteps(RequesterFactory requester) {
        this.successfulSearchRequester =
                requester.successfulSearch(ObservationEndpoints.SEARCH);
        this.successfulCrudRequester =
                requester.successfulCrud(ObservationEndpoints.CRUD);
    }

    public ObservationResponse createObservation(
            ObservationCreateRequest request
    ) {
        return successfulCrudRequester.create(request);
    }

    public ObservationResponse getObservation(String id) {
        return successfulCrudRequester.get(id);
    }

    public ObservationResponse updateObservation(
            String id,
            ObservationUpdateRequest request
    ) {
        return successfulCrudRequester.update(id, request);
    }

    public Response deleteObservation(String id) {
        return successfulCrudRequester.delete(id);
    }

    public Response purgeObservation(String id) {
        return successfulCrudRequester.delete(id, DeleteMode.PURGE);
    }

    public ObservationSearchResponse searchObservations(
            ObservationSearchParams params
    ) {
        return successfulSearchRequester.search(params);
    }

    public ObservationSearchResponse listObservations() {
        return searchObservations(new ObservationSearchParams());
    }
}
