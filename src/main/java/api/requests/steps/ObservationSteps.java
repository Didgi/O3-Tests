package api.requests.steps;

import api.models.observations.*;
import api.requests.endpoints.ObservationEndpoints;
import api.requests.skeleton.interfaces.CrudEndpoint;
import api.requests.skeleton.options.DeleteMode;
import api.requests.skeleton.options.ReadOptions;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulSearchRequester;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class ObservationSteps extends CrudStepsSupport<
        ObservationCreateRequest,
        ObservationUpdateRequest,
        ObservationResponse
        > {
    private final SuccessfulSearchRequester<
            ObservationSearchParams,
            ObservationSearchResponse
            > successfulSearchRequester;
    private final CrudEndpoint<
            ObjectNode,
            ObservationResponse
            > rawJsonCrud;

    public ObservationSteps(RequesterFactory requester) {
        super(requester, ObservationEndpoints.CRUD);
        this.successfulSearchRequester =
                requester.successfulSearch(ObservationEndpoints.SEARCH);
        this.rawJsonCrud =
                requester.rawCrud(ObservationEndpoints.CRUD);
    }

    public ObservationResponse createObservation(
            ObservationCreateRequest request
    ) {
        return successfulCrud.create(request);
    }

    public ObservationResponse getObservation(String id) {
        return successfulCrud.get(id);
    }

    public ObservationResponse getObservation(
            String id,
            ReadOptions options
    ) {
        return successfulCrud.get(id, options);
    }

    public ObservationResponse updateObservation(
            String id,
            ObservationUpdateRequest request
    ) {
        return successfulCrud.update(id, request);
    }

    public void deleteObservation(String id) {
        successfulCrud.delete(id);
    }

    public Response purgeObservation(String id) {
        return successfulCrud.delete(id, DeleteMode.PURGE);
    }

    public ObservationSearchResponse searchObservations(
            ObservationSearchParams params
    ) {
        return successfulSearchRequester.search(params);
    }

    public ObservationSearchResponse searchObservationByPatientAndConcept(
            String patientUuid,
            String conceptUuid
    ) {
        return successfulSearchRequester
                .search(
                        ObservationSearchParams.builder()
                                .conceptUuid(conceptUuid)
                                .patientUuid(patientUuid)
                                .build()
                );
    }

    public ObservationSearchResponse searchObservationByPatient(
            String patientUuid
    ) {
        return successfulSearchRequester
                .search(
                        ObservationSearchParams.builder()
                                .patientUuid(patientUuid)
                                .build()
                );
    }

    public ObservationSearchResponse searchObservationByEncounter(
            String encounterUuid
    ) {
        return successfulSearchRequester
                .search(
                        ObservationSearchParams.builder()
                                .encounterUuid(encounterUuid)
                                .build()
                );
    }

    public ObservationSearchResponse listObservations() {
        return searchObservations(
                new ObservationSearchParams(null, null, null)
        );
    }

    public Response createObservationRaw(
            ObservationCreateRequest request
    ) {
        return rawCrud.create(request);
    }

    public Response createObservationRawJson(
            ObjectNode request
    ) {
        return rawJsonCrud.create(request);
    }
}
