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

    public static EncounterCreateRequest validEncounter(String patientUuid, String visitUuid) {
        EncounterProviderRequest provider = new EncounterProviderRequest(
                ReferenceTestData.encounterProviderUuid(),
                ReferenceTestData.clinicianEncounterRoleUuid()
        );

        return new EncounterCreateRequest(
                currentDatetime(),
                patientUuid,
                ReferenceTestData.consultationEncounterTypeUuid(),
                ReferenceTestData.locationUuid(),
                List.of(provider),
                visitUuid
        );
    }

    public static EncounterCreateRequest minimalEncounter(String patientUuid) {
        return new EncounterCreateRequest(
                null,
                patientUuid,
                ReferenceTestData.consultationEncounterTypeUuid(),
                null,
                null,
                null
        );
    }

    public static EncounterCreateRequest withEncounterType(EncounterCreateRequest request, String encounterTypeUuid) {
        return new EncounterCreateRequest(
                request.encounterDatetime(),
                request.patient(),
                encounterTypeUuid,
                request.location(),
                request.encounterProviders(),
                request.visit()
        );
    }

    public static EncounterCreateRequest withEncounterDatetime(
            EncounterCreateRequest request, String encounterDatetime
    ) {
        return new EncounterCreateRequest(
                encounterDatetime,
                request.patient(),
                request.encounterType(),
                request.location(),
                request.encounterProviders(),
                request.visit()
        );
    }

    public static EncounterCreateRequest withLocation(EncounterCreateRequest request, String locationUuid) {
        return new EncounterCreateRequest(
                request.encounterDatetime(),
                request.patient(),
                request.encounterType(),
                locationUuid,
                request.encounterProviders(),
                request.visit()
        );
    }

    public static EncounterCreateRequest withProvider(EncounterCreateRequest request, String providerUuid) {
        EncounterProviderRequest currentProvider = request.encounterProviders().get(0);
        EncounterProviderRequest provider = new EncounterProviderRequest(
                providerUuid,
                currentProvider.encounterRole()
        );

        return new EncounterCreateRequest(
                request.encounterDatetime(),
                request.patient(),
                request.encounterType(),
                request.location(),
                List.of(provider),
                request.visit()
        );
    }

    public static EncounterCreateRequest withEncounterRole(EncounterCreateRequest request, String encounterRoleUuid) {
        EncounterProviderRequest currentProvider = request.encounterProviders().get(0);
        EncounterProviderRequest provider = new EncounterProviderRequest(
                currentProvider.provider(),
                encounterRoleUuid
        );

        return new EncounterCreateRequest(
                request.encounterDatetime(),
                request.patient(),
                request.encounterType(),
                request.location(),
                List.of(provider),
                request.visit()
        );
    }

    public static String futureDatetime() {
        return OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .withNano(0)
                .format(OPEN_MRS_DATE_TIME);
    }

    public static String oneMinuteBefore(String encounterDatetime) {
        return OffsetDateTime.parse(encounterDatetime, OPEN_MRS_DATE_TIME)
                .minusMinutes(1)
                .format(OPEN_MRS_DATE_TIME);
    }

    public static String oneMinuteAfter(String encounterDatetime) {
        return OffsetDateTime.parse(encounterDatetime, OPEN_MRS_DATE_TIME)
                .plusMinutes(1)
                .format(OPEN_MRS_DATE_TIME);
    }

    private static String currentDatetime() {
        return OffsetDateTime.now(ZoneOffset.UTC)
                .withNano(0)
                .format(OPEN_MRS_DATE_TIME);
    }
}
