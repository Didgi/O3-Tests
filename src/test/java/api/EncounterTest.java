package api;

import api.helpers.EncounterTestHelper;
import api.helpers.EncounterTestHelper.PatientVisit;
import api.models.encounter.EncounterCreateRequest;
import api.models.encounter.EncounterProviderRequest;
import api.models.encounter.EncounterResponse;
import api.models.patients.PatientResponse;
import api.requests.steps.ApiClient;
import api.testdata.EncounterTestData;
import api.utils.RandomData;
import api.utils.comparison.ModelAssertions;
import common.annotations.Bug;
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

    private ApiClient admin;
    private EncounterTestHelper helper;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
        helper = new EncounterTestHelper(admin);
    }

    @Test
    @DisplayName("Создание Encounter внутри существующего Visit")
    void createEncounterInsideExistingVisit() {
        PatientVisit data = helper.createPatientWithVisit();
        EncounterCreateRequest request = EncounterTestData.validEncounter(data.patient().uuid(), data.visit().uuid());

        EncounterResponse createdEncounter = admin.encounters().createEncounter(request);
        EncounterResponse actualEncounter = admin.encounters().getEncounterFull(createdEncounter.uuid());

        ModelAssertions.assertThatModels(request, actualEncounter).match();

        EncounterProviderRequest expectedProvider = request.encounterProviders().getFirst();

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
        PatientResponse patient = helper.createPatient();
        EncounterCreateRequest request = EncounterTestData.minimalEncounter(patient.uuid());

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
        PatientVisit data = helper.createPatientWithVisit();
        EncounterCreateRequest validRequest =
                EncounterTestData.validEncounter(data.patient().uuid(), data.visit().uuid());
        EncounterCreateRequest request =
                EncounterTestData.withEncounterDatetime(validRequest, data.visit().startDatetime());

        EncounterResponse createdEncounter = admin.encounters().createEncounter(request);
        EncounterResponse actualEncounter = admin.encounters().getEncounterFull(createdEncounter.uuid());

        softly.assertThat(actualEncounter.encounterDatetime()).isEqualTo(data.visit().startDatetime());
    }

    @Test
    @DisplayName("Изменение Encounter сохраняется")
    void updateEncounter() {
        PatientVisit data = helper.createPatientWithVisit();
        EncounterCreateRequest createRequest =
                EncounterTestData.validEncounter(data.patient().uuid(), data.visit().uuid());

        EncounterResponse createdEncounter = admin.encounters().createEncounter(createRequest);
        String updatedDatetime = EncounterTestData.oneMinuteBefore(createdEncounter.encounterDatetime());
        EncounterCreateRequest updateRequest = EncounterTestData.withEncounterDatetime(createRequest, updatedDatetime);

        admin.encounters().updateEncounter(createdEncounter.uuid(), updateRequest);

        EncounterResponse actualEncounter = admin.encounters().getEncounterFull(createdEncounter.uuid());

        ModelAssertions.assertThatModels(updateRequest, actualEncounter).match();

        EncounterProviderRequest expectedProvider = updateRequest.encounterProviders().getFirst();

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
        PatientResponse patient = helper.createPatient();

        EncounterCreateRequest request = EncounterTestData.withEncounterType(
                EncounterTestData.minimalEncounter(patient.uuid()),
                null
        );

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithRawMessage(response, ENCOUNTER_TYPE_FIELD);
    }

    @Test
    @DisplayName("Encounter не создаётся с датой в будущем")
    void encounterIsNotCreatedWithFutureDatetime() {
        PatientResponse patient = helper.createPatient();

        EncounterCreateRequest request = EncounterTestData.withEncounterDatetime(
                EncounterTestData.minimalEncounter(patient.uuid()),
                EncounterTestData.futureDatetime()
        );

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(
                response,
                ENCOUNTER_DATETIME_FIELD,
                FUTURE_DATETIME
        );
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
        EncounterCreateRequest request = EncounterTestData.minimalEncounter(RandomData.randomUuid());

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(
                response,
                PATIENT_FIELD,
                PATIENT_REQUIRED
        );
    }

    @Test
    @DisplayName("Encounter не создаётся, если Patient не совпадает с Patient Visit")
    void encounterIsNotCreatedWhenPatientDoesNotMatchVisitPatient() {
        PatientVisit data = helper.createPatientWithVisit();
        PatientResponse anotherPatient = helper.createPatient();
        EncounterCreateRequest request = EncounterTestData.validEncounter(anotherPatient.uuid(), data.visit().uuid());

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(
                response,
                VISIT_FIELD,
                PATIENT_VISIT_MISMATCH
        );
    }

    @Test
    @DisplayName("Encounter не создаётся раньше начала Visit")
    void encounterIsNotCreatedBeforeVisitStart() {
        PatientVisit data = helper.createPatientWithVisit();
        EncounterCreateRequest validRequest =
                EncounterTestData.validEncounter(data.patient().uuid(), data.visit().uuid());
        String datetimeBeforeVisit = EncounterTestData.oneMinuteBefore(data.visit().startDatetime());
        EncounterCreateRequest request = EncounterTestData.withEncounterDatetime(validRequest, datetimeBeforeVisit);

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(
                response,
                ENCOUNTER_DATETIME_FIELD,
                DATETIME_OUTSIDE_VISIT_RANGE
        );
    }

    @Test
    @DisplayName("Encounter не создаётся с несуществующим Encounter Type")
    void encounterIsNotCreatedWithUnknownEncounterType() {
        PatientResponse patient = helper.createPatient();

        EncounterCreateRequest request = EncounterTestData.withEncounterType(
                EncounterTestData.minimalEncounter(patient.uuid()),
                RandomData.randomUuid()
        );

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(
                response,
                ENCOUNTER_TYPE_FIELD,
                ENCOUNTER_TYPE_REQUIRED
        );
    }

    @Test
    @Bug(true)
    @Disabled("BUG: API создаёт Encounter с несуществующим Visit вместо возврата 400")
    @DisplayName("Encounter не создаётся с несуществующим Visit")
    void encounterIsNotCreatedWithUnknownVisit() {
        PatientResponse patient = helper.createPatient();
        EncounterCreateRequest request = EncounterTestData.validEncounter(patient.uuid(), RandomData.randomUuid());

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Bug(true)
    @Disabled(
            "BUG: API создаёт Encounter с несуществующим Location вместо возврата 400"
    )
    @DisplayName("Encounter не создаётся с несуществующим Location")
    void encounterIsNotCreatedWithUnknownLocation() {
        PatientVisit data = helper.createPatientWithVisit();
        EncounterCreateRequest validRequest =
                EncounterTestData.validEncounter(data.patient().uuid(), data.visit().uuid());
        EncounterCreateRequest request = EncounterTestData.withLocation(validRequest, RandomData.randomUuid());

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Bug(true)
    @Disabled("BUG: API возвращает 500 для несуществующего Provider вместо клиентской ошибки")
    @DisplayName("Encounter не создаётся с несуществующим Provider")
    void encounterIsNotCreatedWithUnknownProvider() {
        PatientVisit data = helper.createPatientWithVisit();
        EncounterCreateRequest validRequest =
                EncounterTestData.validEncounter(data.patient().uuid(), data.visit().uuid());
        EncounterCreateRequest request = EncounterTestData.withProvider(validRequest, RandomData.randomUuid());

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Bug(true)
    @Disabled("BUG: API возвращает 500 для несуществующего Encounter Role вместо клиентской ошибки")
    @DisplayName("Encounter не создаётся с несуществующим Encounter Role")
    void encounterIsNotCreatedWithUnknownEncounterRole() {
        PatientVisit data = helper.createPatientWithVisit();
        EncounterCreateRequest validRequest =
                EncounterTestData.validEncounter(data.patient().uuid(), data.visit().uuid());
        EncounterCreateRequest request = EncounterTestData.withEncounterRole(validRequest, RandomData.randomUuid());

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }
}
