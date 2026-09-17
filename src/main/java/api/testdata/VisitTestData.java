package api.testdata;

import api.models.visit.VisitCreateRequest;

public class VisitTestData {
    public static VisitCreateRequest validVisitCreateRequest(String patientUuid) {
        return new VisitCreateRequest(
                patientUuid,
                ReferenceTestData.visitTypeUuid(),
                null,
                ReferenceTestData.locationUuid()
        );
    }
}
