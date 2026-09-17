package api.helpers;

import api.models.patients.PatientResponse;
import api.models.visit.VisitCreateResponse;
import api.requests.steps.ApiClient;
import api.testdata.PatientTestData;
import api.testdata.VisitTestData;

public class EncounterTestHelper {

    private final ApiClient admin;

    public EncounterTestHelper(ApiClient admin) {
        this.admin = admin;
    }

    public PatientResponse createPatient() {
        return admin.patients()
                .createPatient(
                        PatientTestData.validPatient()
                );
    }

    public PatientVisit createPatientWithVisit() {
        PatientResponse patient = createPatient();

        VisitCreateResponse visit =
                admin.visits()
                        .createVisit(
                                VisitTestData.activeVisitCreateRequest(
                                        patient.uuid()
                                )
                        );

        return new PatientVisit(patient, visit);
    }

    public record PatientVisit(
            PatientResponse patient,
            VisitCreateResponse visit
    ) {
    }
}
