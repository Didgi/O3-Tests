package api.requests.steps;

import api.models.patients.PatientCreateRequest;
import api.models.patients.PatientResponse;
import api.models.patients.PatientSearchParams;
import api.models.patients.PatientSearchResponse;
import api.models.patients.PatientUpdateRequest;
import api.requests.endpoints.PatientEndpoints;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulSearchRequester;
import io.restassured.response.Response;

public class PatientSteps extends CrudStepsSupport<
        PatientCreateRequest,
        PatientUpdateRequest,
        PatientResponse
        > {

    private final SuccessfulSearchRequester<
            PatientSearchParams,
            PatientSearchResponse
            > successfulSearch;

    public PatientSteps(RequesterFactory requester) {
        super(requester, PatientEndpoints.CRUD);

        successfulSearch =
                requester.successfulSearch(PatientEndpoints.SEARCH);
    }

    public PatientResponse createPatient(
            PatientCreateRequest request
    ) {
        return successfulCrud.create(request);
    }

    public PatientResponse getPatient(String id) {
        return successfulCrud.get(id);
    }

    public PatientResponse updatePatient(
            String id,
            PatientUpdateRequest request
    ) {
        return successfulCrud.update(id, request);
    }

    public PatientSearchResponse searchPatients(
            PatientSearchParams params
    ) {
        return successfulSearch.search(params);
    }

    public Response createPatientRaw(
            PatientCreateRequest request
    ) {
        return rawCrud.create(request);
    }

    public Response getPatientRaw(String id) {
        return rawCrud.get(id);
    }
}