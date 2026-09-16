package api.requests.steps;

import api.models.auth.request.Credentials;
import api.requests.skeleton.requesters.RequesterFactory;
import api.specs.RequestSpecs;
import io.restassured.specification.RequestSpecification;

public final class ApiClient {
    private final UserSteps users;
    private final ObservationSteps observations;
    private final VisitSteps visits;
    private final AppointmentSteps appointments;
    private final PatientSteps patients;
    private final EncounterSteps encounters;

    private ApiClient(
            RequestSpecification specification
    ) {
        RequesterFactory requesters =
                new RequesterFactory(specification);

        users = new UserSteps(requesters);
        observations = new ObservationSteps(requesters);
        visits = new VisitSteps(requesters);
        appointments = new AppointmentSteps(requesters);
        patients = new PatientSteps(requesters);
        encounters = new EncounterSteps(requesters);
    }

    public static ApiClient authenticatedAs(
            Credentials credentials
    ) {
        return new ApiClient(RequestSpecs.withAuth(
                credentials.username(),
                credentials.password()
        ));
    }

    public static ApiClient admin() {
        return new ApiClient(RequestSpecs.withAdminBasicAuth());
    }

    public UserSteps users() {
        return users;
    }

    public ObservationSteps observations() {
        return observations;
    }

    public VisitSteps visits() {
        return visits;
    }

    public AppointmentSteps appointments() {
        return appointments;
    }

    public PatientSteps patients() {
        return patients;
    }

    public EncounterSteps encounters() {
        return encounters;
    }
}
