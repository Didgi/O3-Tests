package api.requests.steps;

import api.models.encounter.EncounterCreateRequest;
import api.models.encounter.EncounterResponse;
import api.requests.endpoints.EncounterEndpoints;
import api.requests.skeleton.options.DeleteMode;
import api.requests.skeleton.options.ReadOptions;
import api.requests.skeleton.requesters.RequesterFactory;
import io.restassured.response.Response;

public class EncounterSteps extends CrudStepsSupport<
        EncounterCreateRequest,
        EncounterCreateRequest,
        EncounterResponse
        > {

    public EncounterSteps(RequesterFactory requesters) {
        super(requesters, EncounterEndpoints.CRUD);
    }

    public EncounterResponse createEncounter(
            EncounterCreateRequest request
    ) {
        return successfulCrud.create(request);
    }

    public EncounterResponse getEncounter(String id) {
        return successfulCrud.get(id);
    }

    public EncounterResponse getEncounterFull(String id) {
        return successfulCrud.get(id, ReadOptions.full());
    }

    public Response purgeEncounter(String id) {
        return successfulCrud.delete(id, DeleteMode.PURGE);
    }
}
