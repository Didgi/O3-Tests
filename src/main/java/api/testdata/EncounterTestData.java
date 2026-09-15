package api.testdata;

import api.models.encounter.EncounterCreateRequest;
import api.models.encounter.EncounterProviderRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class EncounterTestData {

    private static final DateTimeFormatter OPEN_MRS_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private EncounterTestData() {
    }

    public static EncounterCreateRequest validEncounter(
            String patientUuid,
            String visitUuid
    ) {
        return new EncounterCreateRequest(
                OffsetDateTime.now(ZoneOffset.UTC)
                        .format(OPEN_MRS_DATE_TIME),
                patientUuid,
                ReferenceTestData.consultationEncounterTypeUuid(),
                ReferenceTestData.locationUuid(),
                List.of(
                        new EncounterProviderRequest(
                                ReferenceTestData.encounterProviderUuid(),
                                ReferenceTestData.clinicianEncounterRoleUuid()
                        )
                ),
                visitUuid
        );
    }
}
