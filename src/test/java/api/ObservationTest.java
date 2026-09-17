package api;

import api.models.observations.ObservationCreateRequest;
import api.models.observations.ObservationResponse;
import api.models.observations.ObservationSearchResponse;
import api.models.observations.ObservationUpdateRequest;
import api.models.patients.PatientResponse;
import api.requests.skeleton.options.ReadOptions;
import api.requests.steps.ApiClient;
import api.testdata.EncounterTestData;
import api.testdata.ObservationTestData;
import api.testdata.PatientTestData;
import api.testdata.ReferenceTestData;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import common.annotations.WithPatient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.tuple;

public class ObservationTest extends BaseApiTest {
    private static final String UPDATED_OBSERVATION_STATUS = "AMENDED";
    private static final String UNKNOWN_UUID = "00000000-0000-0000-0000-000000000000";
    private static final String UNKNOWN_CONCEPT = "0000AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String TEXT_CONCEPT = "159650AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private ApiClient admin;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    @DisplayName("OBS-P0-01 Создание наблюдения с числовым значением")
    @WithPatient
    @Test
    void createNumericObservationTest(PatientResponse patient) {

        ObservationCreateRequest request =
                ObservationTestData.validObservation(
                        patient.uuid(),
                        IntNode.valueOf(70)
                );

        ObservationResponse observationResponse =
                admin.observations().createObservation(request);

        softly.assertThat(observationResponse.uuid())
                .as("Created observation UUID is present")
                .isNotNull();
        softly.assertThat(observationResponse.person().uuid())
                .as("Created observation references the requested patient")
                .isEqualTo(request.person());
        softly.assertThat(observationResponse.concept().uuid())
                .as("Created observation references the requested concept")
                .isEqualTo(request.concept());
        softly.assertThat(observationResponse.value().decimalValue())
                .as("Created observation numeric value matches the request")
                .isEqualByComparingTo(request.value().decimalValue());
        softly.assertThat(observationResponse.obsDatetime())
                .as("Created observation datetime matches the request")
                .isEqualTo(request.obsDatetime());
    }

    @DisplayName("OBS-P0-02 Отклонение несовместимого значения без создания наблюдения")
    @WithPatient
    @Test
    void rejectIncompatibleValueWithoutCreatingObservationTest(PatientResponse patient) {

        ObservationCreateRequest request =
                ObservationTestData.validObservation(
                        patient.uuid(),
                        TextNode.valueOf("abc")
                );

        admin.observations().createObservationRaw(request)
                .then()
                .statusCode(400);

        ObservationSearchResponse searchObservationResponse =
                admin.observations().searchObservationByPatientAndConcept(
                        patient.uuid(),
                        ReferenceTestData.conceptId()
                );

        softly.assertThat(searchObservationResponse.results())
                .as("Search by patient and concept returns no observations after rejecting an incompatible value")
                .isEmpty();
    }

    @DisplayName("OBS-P0-03 Получение созданного наблюдения по UUID")
    @WithPatient
    @Test
    void getCreatedObservationByUuid(PatientResponse patient) {

        ObservationCreateRequest request =
                ObservationTestData.validObservation(patient.uuid());

        ObservationResponse createdObservation =
                admin.observations().createObservation(request);

        ObservationResponse observationResponse =
                admin.observations().getObservation(createdObservation.uuid());

        softly.assertThat(observationResponse.uuid())
                .as("Retrieved observation UUID matches the created observation")
                .isEqualTo(createdObservation.uuid());
        softly.assertThat(observationResponse.voided())
                .as("Retrieved observation is not voided")
                .isFalse();
        softly.assertThat(observationResponse)
                .as("Retrieved observation patient, concept, value, and datetime match the created observation")
                .usingRecursiveComparison()
                .comparingOnlyFields("person", "concept", "value", "obsDatetime")
                .isEqualTo(createdObservation);
    }

    @DisplayName("OBS-P0-04 Фильтрация наблюдений по пациенту и концепту")
    @WithPatient
    @Test
    void filterObservationByPatientAndConcept(PatientResponse patient) {

        // Arrange

        PatientResponse anotherPatient =
                admin.patients().createPatient(PatientTestData.validPatient());

        ObservationCreateRequest patientWeightRequest =
                ObservationTestData.validObservation(patient.uuid());

        ObservationResponse patientWeightResponse =
                admin.observations()
                        .createObservation(patientWeightRequest);

        ObservationCreateRequest patientHeightRequest =
                ObservationTestData.validHeightObservation(patient.uuid());

        ObservationResponse patientHeightResponse =
                admin.observations()
                        .createObservation(patientHeightRequest);

        ObservationCreateRequest anotherPatientWeightRequest =
                ObservationTestData.validObservation(anotherPatient.uuid());

        ObservationResponse anotherPatientWeightResponse =
                admin.observations()
                        .createObservation(anotherPatientWeightRequest);

        // Act
        ObservationSearchResponse searchObservationResponse =
                admin.observations()
                        .searchObservationByPatientAndConcept(
                                patientWeightResponse.person().uuid(),
                                ReferenceTestData.conceptId()
                        );

        softly.assertThat(searchObservationResponse.results())
                .as("Search by patient and weight concept includes the patient's weight observation " +
                        "and excludes their height observation and another patient's weight observation")
                .extracting(ObservationSearchResponse.ObservationItem::uuid)
                .contains(patientWeightResponse.uuid())
                .doesNotContain(
                        patientHeightResponse.uuid(),
                        anotherPatientWeightResponse.uuid()
                );
    }

    @DisplayName("OBS-P0-05 Создание наблюдения, связанного с медицинским контактом")
    @WithPatient
    @Test
    void createObservationLinkedToEncounter(PatientResponse patient) {

        String encounterUuid =
                admin.encounters().createEncounter(
                        EncounterTestData.minimalEncounter(patient.uuid())
                ).uuid();

        ObservationCreateRequest request =
                ObservationTestData.validObservationWithEncounter(
                        patient.uuid(),
                        encounterUuid
                );

        ObservationResponse response =
                admin.observations().createObservation(request);

        softly.assertThat(response.encounter().uuid())
                .as("Created observation references the requested encounter")
                .isEqualTo(encounterUuid);
        softly.assertThat(response.person().uuid())
                .as("Observation linked to an encounter references the fixture patient")
                .isEqualTo(patient.uuid());

        ObservationSearchResponse searchResponse =
                admin.observations().searchObservationByEncounter(encounterUuid);

        softly.assertThat(searchResponse.results())
                .as("Search by encounter includes the observation linked to that encounter")
                .extracting(ObservationSearchResponse.ObservationItem::uuid)
                .contains(response.uuid());
    }

    @DisplayName("OBS-P0-06 Обновление наблюдения с сохранением истории версий")
    @WithPatient
    @Test
    void updateObservationPreservingVersionHistory(PatientResponse patient) {
        ObservationCreateRequest createRequest =
                ObservationTestData.validObservation(
                        patient.uuid(),
                        IntNode.valueOf(80));

        ObservationResponse createdResponse = admin.observations()
                .createObservation(createRequest);

        ObservationUpdateRequest updateRequest = ObservationUpdateRequest.builder()
                .value(IntNode.valueOf(85))
                .build();

        ObservationResponse updatedResponse =
                admin.observations()
                        .updateObservation(
                                createdResponse.uuid(),
                                updateRequest
                        );

        softly.assertThat(updatedResponse.value().decimalValue())
                .as("Updated observation numeric value matches the update request")
                .isEqualByComparingTo(updateRequest.value().decimalValue());
        softly.assertThat(updatedResponse.uuid())
                .as("Updating an observation creates a new UUID")
                .isNotEqualTo(createdResponse.uuid());
        softly.assertThat(updatedResponse.voided())
                .as("Updated observation version is not voided")
                .isFalse();
        softly.assertThat(updatedResponse.status())
                .as("Updated observation status indicates an amendment")
                .isEqualTo(UPDATED_OBSERVATION_STATUS);

        ObservationResponse observationWithHistory =
                admin.observations().getObservation(
                        updatedResponse.uuid(),
                        ReadOptions.custom("uuid,previousVersions:(uuid,value)")
                );

        softly.assertThat(observationWithHistory.previousVersions())
                .as("Version history contains the previous observation UUID and its original value")
                .extracting(
                        ObservationResponse::uuid,
                        ObservationResponse::value
                )
                .contains(tuple(createdResponse.uuid(), createdResponse.value()));


        ObservationResponse oldObservation = admin.observations()
                .getObservation(createdResponse.uuid());

        softly.assertThat(oldObservation.voided())
                .as("Previous observation version is voided after the update")
                .isTrue();
        softly.assertThat(oldObservation.value().decimalValue())
                .as("Previous observation version retains its original numeric value")
                .isEqualByComparingTo(createRequest.value().decimalValue());
    }

    @DisplayName("OBS-P0-07 Логическое удаление наблюдения без физического удаления")
    @WithPatient
    @Test
    void deleteOperationPerformsVoidNotPurge(PatientResponse patient) {
        ObservationCreateRequest request =
                ObservationTestData.validObservation(patient.uuid());

        ObservationResponse createdObservation = admin.observations()
                .createObservation(request);

        admin.observations()
                .deleteObservation(createdObservation.uuid());

        ObservationResponse voidedObservation = admin.observations()
                .getObservation(createdObservation.uuid());

        softly.assertThat(voidedObservation.voided())
                .as("Deleted observation is marked as voided")
                .isTrue();

        ObservationSearchResponse patientObservations = admin.observations()
                .searchObservationByPatient(patient.uuid());

        softly.assertThat(patientObservations.results())
                .as("Search by patient excludes the voided observation")
                .extracting(ObservationSearchResponse.ObservationItem::uuid)
                .doesNotContain(createdObservation.uuid());
    }

    @DisplayName("OBS-P1-01 Missing person → rejected")
    @WithPatient
    @Test
    void rejectRequestWithoutRequiredPersonField(PatientResponse patient) {
        Response response = admin.observations().createObservationRawJson(
                ObservationTestData.observationWithoutField(patient.uuid(), "person")
        );

        response.then().statusCode(400);

    }

    @DisplayName("OBS-P1-02 Missing concept → rejected")
    @WithPatient
    @Test
    void rejectRequestWithoutRequiredConceptField(PatientResponse patient) {
        Response response = admin.observations().createObservationRawJson(
                ObservationTestData.observationWithoutField(patient.uuid(), "concept")
        );

        response.then().statusCode(400);
    }

    @DisplayName("OBS-P1-03 Missing obsDatetime → rejected")
    @WithPatient
    @Test
    void rejectRequestWithoutRequiredObsDatetimeField(PatientResponse patient) {
        Response response = admin.observations().createObservationRawJson(
                ObservationTestData.observationWithoutField(patient.uuid(), "obsDatetime")
        );

        response.then().statusCode(400);
    }

    @DisplayName("OBS-P1-04 Invalid/non-existing concept → rejected")
    @WithPatient
    @Test
    void rejectRequestWithNonexistingConcept(PatientResponse patient) {
        ObservationCreateRequest request =
                ObservationTestData.validObservation(patient.uuid())
                        .toBuilder()
                        .concept(UNKNOWN_CONCEPT)
                        .build();

        Response response = admin.observations().createObservationRaw(request);

        response.then().statusCode(400);
    }

    @DisplayName("OBS-P1-05 Invalid/non-existing person → rejected")
    @Test
    void rejectRequestWithNonexistingPatient() {
        ObservationCreateRequest request =
                ObservationTestData.validObservation(UNKNOWN_UUID);

        Response response = admin.observations().createObservationRaw(request);

        response.then().statusCode(400);
    }

    @DisplayName("OBS-P1-06 Create Text observation")
    @WithPatient
    @Test
    void createTextObservation(PatientResponse patient) {
        ObservationCreateRequest request =
                ObservationTestData.validObservation(patient.uuid())
                        .toBuilder()
                        .concept(TEXT_CONCEPT)
                        .value(TextNode.valueOf("clear and colorless"))
                        .build();

        ObservationResponse response =
                admin.observations().createObservation(request);

        softly.assertThat(response.uuid())
                .isNotNull();
        softly.assertThat(response.concept().uuid())
                .isEqualTo(request.concept());
        softly.assertThat(response.person().uuid())
                .isEqualTo(patient.uuid());
    }
}
