package api;

import api.models.observations.ObservationCreateRequest;
import api.models.observations.ObservationResponse;
import api.models.observations.ObservationSearchResponse;
import api.models.observations.ObservationUpdateRequest;
import api.models.patients.PatientCreateRequest;
import api.models.patients.PatientResponse;
import api.requests.skeleton.options.ReadOptions;
import api.requests.steps.ApiClient;
import api.testdata.ObservationTestData;
import api.testdata.PatientTestData;
import api.testdata.ReferenceTestData;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import common.annotations.WithPatient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.tuple;

public class ObservationTest extends BaseApiTest {
    private static final String UPDATED_OBSERVATION_STATUS = "AMENDED";

    private ApiClient admin;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    @DisplayName("OBS-P0-01 Create numeric observation")
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
                .as("response uuid is not null")
                .isNotNull();
        softly.assertThat(observationResponse.person().uuid())
                .as("person uuid")
                .isEqualTo(request.person());
        softly.assertThat(observationResponse.concept().uuid())
                .as("concept uuid")
                .isEqualTo(request.concept());
        softly.assertThat(observationResponse.value().decimalValue())
                .as("value field")
                .isEqualByComparingTo(request.value().decimalValue());
        softly.assertThat(observationResponse.obsDatetime())
                .as("date field")
                .isEqualTo(request.obsDatetime());
    }

    @DisplayName("OBS-P0-02 Reject incompatible value without creating Observation")
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
                .isEmpty();
    }

    @DisplayName("OBS-P0-03 Get created observation by UUID")
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
                .as("uuid field")
                .isEqualTo(createdObservation.uuid());
        softly.assertThat(observationResponse.voided())
                .as("voided field is false")
                .isFalse();
        softly.assertThat(observationResponse)
                .usingRecursiveComparison()
                .comparingOnlyFields("person", "concept", "value", "obsDatetime")
                .isEqualTo(createdObservation);
    }

    @DisplayName("OBS-P0-04 Filter observations by patient + concept")
    @WithPatient
    @Test
    void filterObservationByPatientAndConcept(PatientResponse patient) {

        // Arrange

        PatientCreateRequest anotherPatientRequest =
                PatientTestData.validPatient();

        PatientResponse anotherPatient =
                admin.patients().createPatient(anotherPatientRequest);

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
                .as("Patient weight is in results; " +
                        "Patient height is NOT in result; " +
                        "Another Patient weight is NOT in results")
                .extracting(ObservationSearchResponse.ObservationItem::uuid)
                .contains(patientWeightResponse.uuid())
                .doesNotContain(
                        patientHeightResponse.uuid(),
                        anotherPatientWeightResponse.uuid()
                );
    }

    @DisplayName("OBS-P0-05 Create observation linked to Encounter")
    @WithPatient
    @Test
    void createObservationLinkedToEncounter(PatientResponse patient) {

        // TODO: Replace the direct Rest Assured call with encounter steps once they are available.
        String encounterUuid = ObservationTestData.createEncounter(patient.uuid());

        ObservationCreateRequest request =
                ObservationTestData.validObservationWithEncounter(
                        patient.uuid(),
                        encounterUuid
                );

        ObservationResponse response =
                admin.observations().createObservation(request);

        softly.assertThat(response.encounter().uuid())
                .isEqualTo(encounterUuid);
        softly.assertThat(response.person().uuid())
                .isEqualTo(patient.uuid());

        ObservationSearchResponse searchResponse =
                admin.observations().searchObservationByEncounter(encounterUuid);

        softly.assertThat(searchResponse.results())
                .extracting(ObservationSearchResponse.ObservationItem::uuid)
                .contains(response.uuid());
    }

    @DisplayName("OBS-P0-06 Update observation preserving version history")
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
                .isEqualByComparingTo(updateRequest.value().decimalValue());
        softly.assertThat(updatedResponse.uuid())
                .isNotEqualTo(createdResponse.uuid());
        softly.assertThat(updatedResponse.voided())
                .isFalse();
        softly.assertThat(updatedResponse.status())
                .isEqualTo(UPDATED_OBSERVATION_STATUS);

        ObservationResponse observationWithHistory =
                admin.observations().getObservation(
                        updatedResponse.uuid(),
                        ReadOptions.custom("uuid,previousVersions:(uuid,value)")
                );

        softly.assertThat(observationWithHistory.previousVersions())
                .extracting(
                        ObservationResponse::uuid,
                        ObservationResponse::value
                )
                .contains(tuple(createdResponse.uuid(), createdResponse.value()));


        ObservationResponse oldObservation = admin.observations()
                .getObservation(createdResponse.uuid());

        softly.assertThat(oldObservation.voided())
                .isTrue();
        softly.assertThat(oldObservation.value().decimalValue())
                .isEqualByComparingTo(createRequest.value().decimalValue());
    }

    @DisplayName("OBS-P0-07 Delete observation performs void, not purge")
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
                .isTrue();

        ObservationSearchResponse patientObservations = admin.observations()
                .searchObservationByPatient(patient.uuid());

        softly.assertThat(patientObservations.results())
                .extracting(ObservationSearchResponse.ObservationItem::uuid)
                .doesNotContain(createdObservation.uuid());
    }
}
