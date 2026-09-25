package api;

import api.models.observations.*;
import api.models.patients.PatientResponse;
import api.requests.skeleton.options.ReadOptions;
import api.testdata.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.annotations.GeneratedObservationRequest;
import common.annotations.WithPatient;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.tuple;

public class ObservationTest extends BaseApiTest {
    private static final String UNKNOWN_UUID = "00000000-0000-0000-0000-000000000000";
    private static final String UNKNOWN_CONCEPT = "0000AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private static final ObjectMapper MAPPER =
            new ObjectMapper().findAndRegisterModules();

    @DisplayName("OBS-P0-01 Создание наблюдения с числовым значением")
    @WithPatient
    @Test
    void createNumericObservationTest(
            @GeneratedObservationRequest ObservationCreateRequest request
    ) {

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
    void rejectIncompatibleValueWithoutCreatingObservationTest(
            @GeneratedObservationRequest ObservationCreateRequest request,
            PatientResponse patient
    ) {

        ObservationCreateRequest incompatibleValueRequest =
                request.toBuilder()
                        .value(ObservationValueGenerator.alphabeticText(3))
                        .build();

        admin.observations().createObservationRaw(incompatibleValueRequest)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        ObservationSearchResponse searchObservationResponse =
                admin.observations().searchObservationByPatientAndConcept(
                        patient.uuid(),
                        ReferenceTestData.weightConceptId()
                );

        softly.assertThat(searchObservationResponse.results())
                .as("Search by patient and concept returns no observations after rejecting an incompatible value")
                .isEmpty();
    }

    @DisplayName("OBS-P0-03 Получение созданного наблюдения по UUID")
    @WithPatient
    @Test
    void getCreatedObservationByUuid(
            @GeneratedObservationRequest ObservationCreateRequest request
    ) {

        ObservationResponse createdObservation =
                admin.observations().createObservation(request);

        ObservationResponse observationResponse =
                admin.observations()
                        .getObservation(createdObservation.uuid());

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
    void filterObservationByPatientAndConcept(
            @GeneratedObservationRequest ObservationCreateRequest weightRequest,
            @GeneratedObservationRequest(
                    concept = SeedObservationConcept.HEIGHT
            ) ObservationCreateRequest heightRequest
    ) {

        // Arrange

        ObservationResponse patientWeightResponse =
                admin.observations()
                        .createObservation(weightRequest);

        ObservationResponse patientHeightResponse =
                admin.observations()
                        .createObservation(heightRequest);

        PatientResponse anotherPatient =
                admin.patients().createPatient(PatientTestData.validPatient());

        ObservationCreateRequest anotherPatientWeightRequest =
                weightRequest.toBuilder()
                        .person(anotherPatient.uuid())
                        .build();

        ObservationResponse anotherPatientWeightResponse =
                admin.observations()
                        .createObservation(anotherPatientWeightRequest);

        // Act
        ObservationSearchResponse searchObservationResponse =
                admin.observations()
                        .searchObservationByPatientAndConcept(
                                patientWeightResponse.person().uuid(),
                                ReferenceTestData.weightConceptId()
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
    void createObservationLinkedToEncounter(
            @GeneratedObservationRequest ObservationCreateRequest request,
            PatientResponse patient
    ) {

        String encounterUuid =
                admin.encounters().createEncounter(
                        EncounterTestData.minimalEncounter(patient.uuid())
                ).uuid();

        ObservationCreateRequest withEncounterRequest =
                request.toBuilder()
                        .encounter(encounterUuid)
                        .build();

        ObservationResponse response =
                admin.observations().createObservation(withEncounterRequest);

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
    void updateObservationPreservingVersionHistory(
            @GeneratedObservationRequest ObservationCreateRequest request
    ) {

        ObservationResponse createdResponse = admin.observations()
                .createObservation(request);

        BigDecimal updatedValue =
                createdResponse.value()
                        .decimalValue()
                        .add(BigDecimal.ONE);

        ObservationUpdateRequest updateRequest = ObservationUpdateRequest.builder()
                .value(DecimalNode.valueOf(updatedValue))
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
                .isEqualTo(UpdatedObservationStatus.AMENDED.value());

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
                .isEqualByComparingTo(createdResponse.value().decimalValue());
    }

    @DisplayName("OBS-P0-07 Логическое удаление наблюдения без физического удаления")
    @WithPatient
    @Test
    void deleteOperationPerformsVoidNotPurge(
            @GeneratedObservationRequest ObservationCreateRequest request,
            PatientResponse patient
    ) {

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
    void rejectRequestWithoutRequiredPersonField(
            @GeneratedObservationRequest ObservationCreateRequest request
    ) {
        Response response = admin.observations().createObservationRawJson(
                observationWithoutField(request, "person")
        );

        response.then().statusCode(HttpStatus.SC_BAD_REQUEST);

    }

    @DisplayName("OBS-P1-02 Missing concept → rejected")
    @WithPatient
    @Test
    void rejectRequestWithoutRequiredConceptField(
            @GeneratedObservationRequest ObservationCreateRequest request
    ) {
        Response response = admin.observations().createObservationRawJson(
                observationWithoutField(request, "concept")
        );

        response.then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @DisplayName("OBS-P1-03 Missing obsDatetime → rejected")
    @WithPatient
    @Test
    void rejectRequestWithoutRequiredObsDatetimeField(
            @GeneratedObservationRequest ObservationCreateRequest request
    ) {
        Response response = admin.observations().createObservationRawJson(
                observationWithoutField(request, "obsDatetime")
        );

        response.then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @DisplayName("OBS-P1-04 Invalid/non-existing concept → rejected")
    @WithPatient
    @Test
    void rejectRequestWithNonexistingConcept(
            @GeneratedObservationRequest ObservationCreateRequest request
    ) {
        ObservationCreateRequest nonexistingConceptRequest =
                request.toBuilder()
                        .concept(UNKNOWN_CONCEPT)
                        .build();

        Response response = admin.observations()
                .createObservationRaw(nonexistingConceptRequest);

        response.then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @DisplayName("OBS-P1-05 Invalid/non-existing person → rejected")
    @WithPatient
    @Test
    void rejectRequestWithNonexistingPatient(
            @GeneratedObservationRequest ObservationCreateRequest request
    ) {
        ObservationCreateRequest unknownPatientRequest =
                request
                        .toBuilder()
                        .person(UNKNOWN_UUID)
                        .build();

        Response response = admin.observations().createObservationRaw(unknownPatientRequest);

        response.then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @DisplayName("OBS-P1-06 Create Text observation")
    @WithPatient
    @Test
    void createTextObservation(
            @GeneratedObservationRequest(
                    concept = SeedObservationConcept.TEXT
            ) ObservationCreateRequest request,
            PatientResponse patient
    ) {

        ObservationResponse response =
                admin.observations().createObservation(request);

        softly.assertThat(response.uuid())
                .isNotNull();
        softly.assertThat(response.concept().uuid())
                .isEqualTo(request.concept());
        softly.assertThat(response.person().uuid())
                .isEqualTo(patient.uuid());
    }


    private ObjectNode observationWithoutField(
            ObservationCreateRequest request,
            String fieldName
    ) {
        ObjectNode body = MAPPER.valueToTree(request);

        if (!body.has(fieldName)) {
            throw new IllegalArgumentException(
                    "Field is absent from the base request: " + fieldName
            );
        }

        body.remove(fieldName);
        return body;
    }
}
