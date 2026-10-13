package api;

import api.models.encounter.EncounterCreateRequest;
import api.models.encounter.EncounterProviderRequest;
import api.models.encounter.EncounterResponse;
import api.models.patients.PatientResponse;
import api.models.visit.VisitCreateResponse;
import api.testdata.EncounterTestData;
import api.testdata.PatientTestData;
import api.utils.RandomData;
import api.utils.comparison.ModelAssertions;
import common.annotations.WithPatient;
import common.annotations.WithVisit;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static api.helpers.EncounterErrorAssertions.assertBadRequestWithFieldError;
import static api.helpers.EncounterErrorAssertions.assertBadRequestWithRawMessage;
import static api.specs.ResponseSpecs.requestReturnsBadRequest;
import static api.specs.ResponseSpecs.requestReturnsNotFound;
import static api.testdata.EncounterValidationErrors.*;

public class EncounterTest extends BaseApiTest {

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.CRITICAL)
    @WithVisit
    @DisplayName("Создание Encounter внутри существующего Visit")
    void createEncounterInsideExistingVisit(PatientResponse patient, VisitCreateResponse visit) {
        EncounterCreateRequest request = EncounterTestData.validEncounter(patient.uuid(), visit.uuid());

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
    @Issue("TRACK-005")
    @Severity(SeverityLevel.CRITICAL)
    @WithPatient
    @DisplayName("Создание Encounter с минимальным набором обязательных данных")
    void createEncounterWithRequiredFieldsOnly(PatientResponse patient) {
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
    @Issue("TRACK-005")
    @Severity(SeverityLevel.CRITICAL)
    @WithVisit
    @DisplayName("Encounter создаётся с датой, равной началу Visit")
    void createEncounterAtVisitStart(PatientResponse patient, VisitCreateResponse visit) {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(patient.uuid(), visit.uuid());
        EncounterCreateRequest request =
                EncounterTestData.withEncounterDatetime(validRequest, visit.startDatetime());

        EncounterResponse createdEncounter = admin.encounters().createEncounter(request);
        EncounterResponse actualEncounter = admin.encounters().getEncounterFull(createdEncounter.uuid());

        softly.assertThat(actualEncounter.encounterDatetime()).isEqualTo(visit.startDatetime());
    }

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.CRITICAL)
    @WithVisit
    @DisplayName("Изменение Encounter сохраняется")
    void updateEncounter(PatientResponse patient, VisitCreateResponse visit) {
        EncounterCreateRequest createRequest = EncounterTestData.validEncounter(patient.uuid(), visit.uuid());
        EncounterResponse createdEncounter = admin.encounters().createEncounter(createRequest);

        EncounterCreateRequest updateRequest =
                EncounterTestData.withEncounterDatetime(createRequest, visit.startDatetime());

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
    @Issue("TRACK-005")
    @Severity(SeverityLevel.CRITICAL)
    @WithPatient
    @DisplayName("Encounter не создаётся без Encounter Type")
    void encounterIsNotCreatedWithoutEncounterType(PatientResponse patient) {
        EncounterCreateRequest request = EncounterTestData.withEncounterType(
                EncounterTestData.minimalEncounter(patient.uuid()),
                null
        );

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithRawMessage(response, ENCOUNTER_TYPE_FIELD);
    }

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.NORMAL)
    @WithPatient
    @DisplayName("Encounter не создаётся с датой в будущем")
    void encounterIsNotCreatedWithFutureDatetime(PatientResponse patient) {
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
    @Issue("TRACK-005")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Encounter не создаётся без Patient")
    void encounterIsNotCreatedWithoutPatient() {
        EncounterCreateRequest request = EncounterTestData.minimalEncounter(null);

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithRawMessage(response, PATIENT_FIELD);
    }

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.NORMAL)
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
    @Issue("TRACK-005")
    @Severity(SeverityLevel.CRITICAL)
    @WithVisit
    @DisplayName("Encounter не создаётся, если Patient не совпадает с Patient Visit")
    void encounterIsNotCreatedWhenPatientDoesNotMatchVisitPatient(VisitCreateResponse visit) {
        PatientResponse anotherPatient = admin.patients().createPatient(PatientTestData.validPatient());
        EncounterCreateRequest request = EncounterTestData.validEncounter(anotherPatient.uuid(), visit.uuid());

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(
                response,
                VISIT_FIELD,
                PATIENT_VISIT_MISMATCH
        );
    }

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.NORMAL)
    @WithVisit
    @DisplayName("Encounter не создаётся раньше начала Visit")
    void encounterIsNotCreatedBeforeVisitStart(PatientResponse patient, VisitCreateResponse visit) {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(patient.uuid(), visit.uuid());
        String datetimeBeforeVisit = EncounterTestData.oneMinuteBefore(visit.startDatetime());
        EncounterCreateRequest request = EncounterTestData.withEncounterDatetime(validRequest, datetimeBeforeVisit);

        Response response = admin.encounters().createEncounterRaw(request);

        assertBadRequestWithFieldError(
                response,
                ENCOUNTER_DATETIME_FIELD,
                DATETIME_OUTSIDE_VISIT_RANGE
        );
    }

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.NORMAL)
    @WithPatient
    @DisplayName("Encounter не создаётся с несуществующим Encounter Type")
    void encounterIsNotCreatedWithUnknownEncounterType(PatientResponse patient) {
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
    @Issue("TRACK-005")
    @Severity(SeverityLevel.NORMAL)
    @WithPatient
    @Disabled("BUG: API создаёт Encounter с несуществующим Visit вместо возврата 400")
    @DisplayName("Encounter не создаётся с несуществующим Visit")
    void encounterIsNotCreatedWithUnknownVisit(PatientResponse patient) {
        EncounterCreateRequest request = EncounterTestData.validEncounter(patient.uuid(), RandomData.randomUuid());

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.NORMAL)
    @WithVisit
    @Disabled("BUG: API создаёт Encounter с несуществующим Location вместо возврата 400")
    @DisplayName("Encounter не создаётся с несуществующим Location")
    void encounterIsNotCreatedWithUnknownLocation(PatientResponse patient, VisitCreateResponse visit) {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(patient.uuid(), visit.uuid());
        EncounterCreateRequest request = EncounterTestData.withLocation(validRequest, RandomData.randomUuid());

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.NORMAL)
    @WithVisit
    @Disabled("BUG: API возвращает 500 для несуществующего Provider вместо клиентской ошибки")
    @DisplayName("Encounter не создаётся с несуществующим Provider")
    void encounterIsNotCreatedWithUnknownProvider(PatientResponse patient, VisitCreateResponse visit) {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(patient.uuid(), visit.uuid());
        EncounterCreateRequest request = EncounterTestData.withProvider(validRequest, RandomData.randomUuid());

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.NORMAL)
    @WithVisit
    @Disabled("BUG: API возвращает 500 для несуществующего Encounter Role вместо клиентской ошибки")
    @DisplayName("Encounter не создаётся с несуществующим Encounter Role")
    void encounterIsNotCreatedWithUnknownEncounterRole(PatientResponse patient, VisitCreateResponse visit) {
        EncounterCreateRequest validRequest = EncounterTestData.validEncounter(patient.uuid(), visit.uuid());
        EncounterCreateRequest request = EncounterTestData.withEncounterRole(validRequest, RandomData.randomUuid());

        Response response = admin.encounters().createEncounterRaw(request);

        response.then().spec(requestReturnsBadRequest());
    }

    @Test
    @Issue("TRACK-005")
    @Severity(SeverityLevel.CRITICAL)
    @WithVisit
    @DisplayName("Физическое удаление Encounter")
    void purgeEncounter(PatientResponse patient, VisitCreateResponse visit) {
        EncounterCreateRequest request = EncounterTestData.validEncounter(patient.uuid(), visit.uuid());
        EncounterResponse createdEncounter = admin.encounters().createEncounter(request);

        admin.encounters().purgeEncounter(createdEncounter.uuid());

        Response response = admin.encounters().getEncounterRaw(createdEncounter.uuid());

        response.then().spec(requestReturnsNotFound());
    }
}
