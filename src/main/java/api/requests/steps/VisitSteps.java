package api.requests.steps;

import api.models.visit.VisitCreateRequest;
import api.models.visit.VisitCreateResponse;
import api.models.visit.VisitSearchParams;
import api.models.visit.VisitSearchResponse;
import api.models.visit.VisitUpdateRequest;
import api.requests.endpoints.VisitEndpoints;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulCrudRequester;
import api.requests.skeleton.requesters.SuccessfulSearchRequester;

public class VisitSteps {
    private final SuccessfulCrudRequester<
            VisitCreateRequest,
            VisitUpdateRequest,
            VisitCreateResponse
            > successfulCrudRequester;
    private final SuccessfulSearchRequester<
            VisitSearchParams,
            VisitSearchResponse
            > searchRequester;

    public VisitSteps(RequesterFactory requester) {
        this.successfulCrudRequester =
                requester.successfulCrud(VisitEndpoints.CRUD);
        this.searchRequester =
                requester.successfulSearch(VisitEndpoints.SEARCH);
    }

    public VisitCreateResponse createVisit(VisitCreateRequest request) {
        return successfulCrudRequester.create(request);
    }

    public VisitCreateResponse getVisit(String id) {
        return successfulCrudRequester.get(id);
    }

    public VisitCreateResponse endVisit(String id, VisitUpdateRequest request) {
        return successfulCrudRequester.update(id, request);
    }

    public void deleteVisit(String id) {
        successfulCrudRequester.delete(id);
    }

    public VisitSearchResponse searchVisitsByPatient(String patientUuid) {
        return searchRequester.search(new VisitSearchParams(patientUuid, false));
    }
}
