package api;

import api.models.encounter.EncounterCreateRequest;
import api.models.encounter.EncounterProviderRequest;
import api.models.encounter.EncounterResponse;
import api.requests.steps.ApiClient;
import api.testdata.EncounterTestData;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static api.helpers.EncounterErrorAssertions.assertBadRequestWithFieldError;
import static api.helpers.EncounterErrorAssertions.assertBadRequestWithRawMessage;
import static api.specs.ResponseSpecs.requestReturnsBadRequest;
import static api.testdata.EncounterValidationErrors.DATETIME_OUTSIDE_VISIT_RANGE;
import static api.testdata.EncounterValidationErrors.ENCOUNTER_DATETIME_FIELD;
import static api.testdata.EncounterValidationErrors.ENCOUNTER_TYPE_FIELD;
import static api.testdata.EncounterValidationErrors.ENCOUNTER_TYPE_REQUIRED;
import static api.testdata.EncounterValidationErrors.FUTURE_DATETIME;
import static api.testdata.EncounterValidationErrors.PATIENT_FIELD;
import static api.testdata.EncounterValidationErrors.PATIENT_REQUIRED;
import static api.testdata.EncounterValidationErrors.PATIENT_VISIT_MISMATCH;
import static api.testdata.EncounterValidationErrors.VISIT_FIELD;

public class EncounterTest extends BaseApiTest {

    private static final String PATIENT_UUID = "59e2c777-2932-42f7-921e-a1e6ff60f40a";
    private static final String VISIT_UUID = "688eb514-cc5b-4016-aa88-0fb5d8d6d293";
    private static final String UNKNOWN_UUID = "00000000-0000-0000-0000-000000000000";
    private static final String ANOTHER_PATIENT_VISIT_UUID = "d6996a93-c77a-4405-be77-99837378165f";
    private static final String VISIT_START_DATETIME = "2026-09-14T18:38:31.000+0000";

    private ApiClient admin;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    @Test
    @DisplayName("Создание Encounter внутри существующего Visit")
    void createEncounterInsideExistingVisit() {
        EncounterCreateRequest request = EncounterTestData.validEncounter(PATIENT_UUID, VISIT_UUID);

        EncounterResponse createdEncounter = admin.encounters().createEncounter(request);
        String encounterUuid = createdEncounter.uuid();

        softly.assertThat(encounterUuid).isNotBlank();

        EncounterResponse actualEncounter = admin.encounters().getEncounterFull(encounterUuid);
        EncounterProviderRequest expectedProvider = request.encounterProviders().get(0);

        softly.assertThat(actualEncounter.uuid()).isEqualTo(encounterUuid);
        softly.assertThat(actualEncounter.encounterDatetime()).isEqualTo(request.encounterDatetime());
        softly.assertThat(actualEncounter.patient().uuid()).isEqualTo(request.patient());
        softly.assertThat(actualEncounter.visit().uuid()).isEqualTo(request.visit());
        softly.assertThat(actualEncounter.encounterType().uuid()).isEqualTo(request.encounterType());
        softly.assertThat(actualEncounter.location().uuid()).isEqualTo(request.location());
        softly.assertThat(actualEncounter.voided()).isFalse();

        softly.assertThat(actualEncounter.encounterProviders())
                .singleElement()
                .satisfies(actualProvider -> {
                    softly.assertThat(actualProvider.provider().uuid()).isEqualTo(expectedProvider.provider());
                    softly.assertThat(actualProvider.encounterRole().uuid())
                            .isEqualTo(expectedProvider.encounterRole());
                });
    }

    @Test
    @DisplayName("Создание Encounter с минимальным набором обязательных данных")
    void createEncounterWithRequiredFieldsOnly() {
        EncounterCreateRequest request = EncounterTestData.minimalEncounter(PATIENT_UUID);

        EncounterResponse createdEncounter = admin.encounters().createEncounter(request);
        EncounterResponse actualEncounter = admin.encounters().getEncounterFull(createdEncounter.uuid());

        softly.assertThat(actualEncounter.uuid()).isNotBlank();
        softly.assertThat(actualEncounter.patient().uuid()).isEqualTo(request.patient());
        softly.assertThat(actualEncounter.encounterType().uuid()).isEqualTo(request.encounterType());
        softly.assertThat(actualEncounter.encounterDatetime()).isNotBlank();
        softly.assertThat(actualEncounter.voided()).isFalse();
    }

    @Test
    @DisplayName("Encounter создаётся с датой, равной началу Visit")
    void createEncounterAtVisitStart() {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(PATIENT_UUID, VISIT_UUID);
        EncounterCreateRequest request = EncounterTestData.withEncounterDatetime(validRequest, VISIT_START_DATETIME);

        EncounterResponse createdEncounter = admin.encounters().createEncounter(request);
        EncounterResponse actualEncounter = admin.encounters().getEncounterFull(createdEncounter.uuid());

        softly.assertThat(actualEncounter.uuid()).isEqualTo(createdEncounter.uuid());
        softly.assertThat(actualEncounter.encounterDatetime()).isEqualTo(VISIT_START_DATETIME);
        softly.assertThat(actualEncounter.patient().uuid()).isEqualTo(PATIENT_UUID);
        softly.assertThat(actualEncounter.visit().uuid()).isEqualTo(VISIT_UUID);
    }

    @Test
    @DisplayName("Изменение Encounter сохраняется")
    void updateEncounter() {
        EncounterCreateRequest createRequest = EncounterTestData.validEncounter(PATIENT_UUID, VISIT_UUID);
        EncounterResponse createdEncounter = admin.encounters().createEncounter(createRequest);
        String encounterUuid = createdEncounter.uuid();

        String updatedDatetime = EncounterTestData.oneMinuteBefore(createdEncounter.encounterDatetime());
        EncounterCreateRequest updateRequest = EncounterTestData.withEncounterDatetime(createRequest, updatedDatetime);

        admin.encounters().updateEncounter(encounterUuid, updateRequest);

        EncounterResponse actualEncounter = admin.encounters().getEncounterFull(encounterUuid);
        EncounterProviderRequest expectedProvider = updateRequest.encounterProviders().get(0);

        softly.assertThat(actualEncounter.uuid()).isEqualTo(encounterUuid);
        softly.assertThat(actualEncounter.encounterDatetime()).isEqualTo(updateRequest.encounterDatetime());
        softly.assertThat(actualEncounter.patient().uuid()).isEqualTo(updateRequest.patient());
        softly.assertThat(actualEncounter.visit().uuid()).isEqualTo(updateRequest.visit());
        softly.assertThat(actualEncounter.encounterType().uuid()).isEqualTo(updateRequest.encounterType());
        softly.assertThat(actualEncounter.location().uuid()).isEqualTo(updateRequest.location());
        softly.assertThat(actualEncounter.voided()).isFalse();

        softly.assertThat(actualEncounter.encounterProviders())
                .singleElement()
                .satisfies(actualProvider -> {
                    softly.assertThat(actualProvider.provider().uuid()).isEqualTo(expectedProvider.provider());
                    softly.assertThat(actualProvider.encounterRole().uuid())
                            .isEqualTo(expectedProvider.encounterRole());
                });
    }

    @Test
    @DisplayName("Encounter не создаётся без Encounter Type")
    void encounterIsNotCreatedWithoutEncounterType() {
        EncounterCreateRequest request = EncounterTestData.withEncounterType(
                EncounterTestData.minimalEncounter(PATIENT_UUID),
                null
        );

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithRawMessage(response, ENCOUNTER_TYPE_FIELD);
    }

    @Test
    @DisplayName("Encounter не создаётся с датой в будущем")
    void encounterIsNotCreatedWithFutureDatetime() {
        EncounterCreateRequest request = EncounterTestData.withEncounterDatetime(
                EncounterTestData.minimalEncounter(PATIENT_UUID),
                EncounterTestData.futureDatetime()
        );

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(response, ENCOUNTER_DATETIME_FIELD, FUTURE_DATETIME);
    }

    @Test
    @DisplayName("Encounter не создаётся без Patient")
    void encounterIsNotCreatedWithoutPatient() {
        EncounterCreateRequest request = EncounterTestData.minimalEncounter(null);

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithRawMessage(response, PATIENT_FIELD);
    }

    @Test
    @DisplayName("Encounter не создаётся с несуществующим Patient")
    void encounterIsNotCreatedWithUnknownPatient() {
        EncounterCreateRequest request = EncounterTestData.minimalEncounter(UNKNOWN_UUID);

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(response, PATIENT_FIELD, PATIENT_REQUIRED);
    }

    @Test
    @DisplayName("Encounter не создаётся, если Patient не совпадает с Patient Visit")
    void encounterIsNotCreatedWhenPatientDoesNotMatchVisitPatient() {
        EncounterCreateRequest request = EncounterTestData.validEncounter(PATIENT_UUID, ANOTHER_PATIENT_VISIT_UUID);

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(response, VISIT_FIELD, PATIENT_VISIT_MISMATCH);
    }

    @Test
    @DisplayName("Encounter не создаётся раньше начала Visit")
    void encounterIsNotCreatedBeforeVisitStart() {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(PATIENT_UUID, VISIT_UUID);
        String datetimeBeforeVisit = EncounterTestData.oneMinuteBefore(VISIT_START_DATETIME);
        EncounterCreateRequest request = EncounterTestData.withEncounterDatetime(validRequest, datetimeBeforeVisit);

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(response, ENCOUNTER_DATETIME_FIELD, DATETIME_OUTSIDE_VISIT_RANGE);
    }

    @Test
    @DisplayName("Encounter не создаётся с несуществующим Encounter Type")
    void encounterIsNotCreatedWithUnknownEncounterType() {
        EncounterCreateRequest request = EncounterTestData.withEncounterType(
                EncounterTestData.minimalEncounter(PATIENT_UUID),
                UNKNOWN_UUID
        );

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(response, ENCOUNTER_TYPE_FIELD, ENCOUNTER_TYPE_REQUIRED);
    }

    @Test
    @Disabled("BUG: API создаёт Encounter с несуществующим Visit вместо возврата 400")
    @DisplayName("Encounter не создаётся с несуществующим Visit")
    void encounterIsNotCreatedWithUnknownVisit() {
        EncounterCreateRequest request = EncounterTestData.validEncounter(PATIENT_UUID, UNKNOWN_UUID);

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Disabled("BUG: API создаёт Encounter с несуществующим Location вместо возврата 400")
    @DisplayName("Encounter не создаётся с несуществующим Location")
    void encounterIsNotCreatedWithUnknownLocation() {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(PATIENT_UUID, VISIT_UUID);
        EncounterCreateRequest request = EncounterTestData.withLocation(validRequest, UNKNOWN_UUID);

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Disabled("BUG: API возвращает 500 для несуществующего Provider вместо клиентской ошибки")
    @DisplayName("Encounter не создаётся с несуществующим Provider")
    void encounterIsNotCreatedWithUnknownProvider() {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(PATIENT_UUID, VISIT_UUID);
        EncounterCreateRequest request = EncounterTestData.withProvider(validRequest, UNKNOWN_UUID);

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Disabled("BUG: API возвращает 500 для несуществующего Encounter Role вместо клиентской ошибки")
    @DisplayName("Encounter не создаётся с несуществующим Encounter Role")
    void encounterIsNotCreatedWithUnknownEncounterRole() {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(PATIENT_UUID, VISIT_UUID);
        EncounterCreateRequest request = EncounterTestData.withEncounterRole(validRequest, UNKNOWN_UUID);

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }
}
