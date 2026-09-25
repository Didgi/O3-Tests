package api;

import api.models.patients.*;
import api.specs.ResponseSpecs;
import api.testdata.PatientTestData;
import api.utils.comparison.ModelAssertions;
import common.annotations.WithPatient;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PatientTest extends BaseApiTest {

    @Test
    @DisplayName("PAT-P0-01: Создание пациента с валидными минимальными данными")
    void createPatientWithValidMinimalData() {
        PatientCreateRequest request =
                PatientTestData.validPatient();

        PatientResponse response =
                admin.patients().createPatient(request);

        ModelAssertions.assertThatModels(request, response).match();

        softly.assertThat(response.uuid())
                .as("Created patient UUID")
                .isNotBlank();

        softly.assertThat(response.person())
                .as("Created patient person")
                .isNotNull()
                .satisfies(person -> {
                    softly.assertThat(person.uuid())
                            .as("Person UUID matches patient UUID")
                            .isEqualTo(response.uuid());

                    softly.assertThat(person.birthdate())
                            .as("Patient birthdate")
                            .startsWith(request.person().birthdate());

                    softly.assertThat(person.preferredName())
                            .as("Preferred name")
                            .isNotNull()
                            .satisfies(name -> {
                                softly.assertThat(name.uuid())
                                        .as("Preferred name UUID")
                                        .isNotBlank();

                                PatientNameRequest expectedName =
                                        request.person()
                                                .names()
                                                .getFirst();

                                softly.assertThat(name.display())
                                        .as("Preferred name display")
                                        .isEqualTo(
                                                expectedName.givenName()
                                                        + " "
                                                        + expectedName.middleName()
                                                        + " "
                                                        + expectedName.familyName()
                                        );
                            });
                });

        softly.assertThat(response.identifiers())
                .as("Patient identifiers")
                .hasSize(request.identifiers().size());

        softly.assertThat(response.identifiers().getFirst().uuid())
                .as("Patient identifier UUID")
                .isNotBlank();
    }

    @Test
    @WithPatient
    @DisplayName("PAT-P0-02: Получение созданного пациента по UUID")
    void getCreatedPatientByUuid(PatientCreateRequest request, PatientResponse created) {
        PatientResponse found =
                admin.patients().getPatient(created.uuid());

        ModelAssertions.assertThatModels(request, found).match();

        softly.assertThat(found.uuid())
                .as("Found patient UUID")
                .isEqualTo(created.uuid());

        softly.assertThat(found.person())
                .as("Found patient person")
                .isNotNull()
                .satisfies(person -> {
                    softly.assertThat(person.uuid())
                            .as("Person UUID matches patient UUID")
                            .isEqualTo(created.uuid());

                    softly.assertThat(person.birthdate())
                            .as("Patient birthdate")
                            .startsWith(request.person().birthdate());

                    PatientNameRequest expectedName =
                            request.person()
                                    .names()
                                    .getFirst();

                    softly.assertThat(person.preferredName())
                            .as("Preferred name")
                            .isNotNull()
                            .satisfies(name ->
                                    softly.assertThat(name.display())
                                            .as("Preferred name display")
                                            .isEqualTo(
                                                    expectedName.givenName()
                                                            + " "
                                                            + expectedName.middleName()
                                                            + " "
                                                            + expectedName.familyName()
                                            )
                            );
                });
    }

    @Test
    @WithPatient
    @DisplayName("PAT-P0-03: Поиск пациента по идентификатору возвращает созданного пациента")
    void searchPatientByIdentifierReturnsCreatedPatient(
            PatientCreateRequest request,
            PatientResponse created
    ) {
        String identifier =
                request.identifiers()
                        .getFirst()
                        .identifier();

        PatientSearchResponse searchResponse =
                admin.patients().searchPatients(
                        new PatientSearchParams(identifier)
                );

        softly.assertThat(searchResponse.results())
                .as("Patients found by identifier")
                .anyMatch(patient ->
                        created.uuid().equals(patient.uuid())
                );
    }

    @Test
    @WithPatient
    @DisplayName("PAT-P0-04: Обновление пациента с сохранением остальных данных")
    void updatePatientAndPreserveOtherData(
            PatientCreateRequest createRequest,
            PatientResponse created
    ) {
        String newGivenName =
                PatientTestData.updatedGivenName();

        PatientUpdateRequest updateRequest =
                PatientTestData.updateName(
                        createRequest,
                        created,
                        newGivenName
                );

        PatientResponse updated =
                admin.patients().updatePatient(
                        created.uuid(),
                        updateRequest
                );

        ModelAssertions.assertThatModels(
                updateRequest,
                updated
        ).match();

        softly.assertThat(updated.person().birthdate())
                .as("Patient birthdate remains unchanged")
                .startsWith(createRequest.person().birthdate());

        String expectedDisplayName =
                newGivenName
                        + " "
                        + createRequest.person()
                        .names()
                        .getFirst()
                        .middleName()
                        + " "
                        + createRequest.person()
                        .names()
                        .getFirst()
                        .familyName();

        softly.assertThat(updated.person().preferredName())
                .as("Updated preferred name")
                .isNotNull()
                .satisfies(name ->
                        softly.assertThat(name.display())
                                .as("Updated preferred name display")
                                .isEqualTo(expectedDisplayName)
                );
    }

    @Test
    @WithPatient
    @DisplayName("PAT-P0-05: Отклонение дублирующего идентификатора пациента")
    void rejectDuplicatePatientIdentifier(PatientCreateRequest firstRequest) {
        String existingIdentifier =
                firstRequest.identifiers()
                        .getFirst()
                        .identifier();

        PatientCreateRequest duplicateRequest =
                PatientTestData.patientWithExistingIdentifier(
                        existingIdentifier
                );

        Response response =
                admin.patients().createPatientRaw(
                        duplicateRequest
                );

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    @Test
    @WithPatient
    @DisplayName("PAT-P1-01: Поиск пациента по имени возвращает созданного пациента")
    void searchPatientByNameReturnsCreatedPatient(
            PatientCreateRequest request,
            PatientResponse created
    ) {
        String givenName =
                request.person()
                        .names()
                        .getFirst()
                        .givenName();

        PatientSearchResponse searchResponse =
                admin.patients().searchPatients(
                        new PatientSearchParams(givenName)
                );

        softly.assertThat(searchResponse.results())
                .as("Patients found by name")
                .anyMatch(patient ->
                        created.uuid().equals(patient.uuid())
                );
    }

    @Test
    @DisplayName("PAT-P1-02: Получение пациента по несуществующему UUID возвращает 404")
    void getPatientByNonExistingUuidReturnsNotFound() {
        String nonExistingUuid =
                PatientTestData.nonExistingUuid();

        Response response =
                admin.patients().getPatientRaw(nonExistingUuid);

        response.then()
                .spec(ResponseSpecs.requestReturnsNotFound());
    }

    @Test
    @DisplayName("PAT-P1-03: Создание пациента без имени отклоняется")
    void createPatientWithoutNameReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithoutName();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    @Test
    @DisplayName("PAT-P1-04: Создание пациента без пола отклоняется")
    void createPatientWithoutGenderReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithoutGender();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    @Test
    @DisplayName("PAT-P1-05: Создание пациента без идентификатора отклоняется")
    void createPatientWithoutIdentifierReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithoutIdentifier();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    @Test
    @DisplayName("PAT-P1-06: Создание пациента с датой рождения в будущем отклоняется")
    void createPatientWithFutureBirthdateReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithFutureBirthdate();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    @Test
    @DisplayName("PAT-P1-07: Создание пациента со слишком старой датой рождения отклоняется")
    void createPatientWithTooOldBirthdateReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithTooOldBirthdate();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    @Test
    @DisplayName("PAT-P1-08: Создание пациента с невалидным идентификатором отклоняется")
    void createPatientWithInvalidIdentifierReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithInvalidIdentifier();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    @Test
    @DisplayName("PAT-P1-09: Создание пациента с несуществующим типом идентификатора отклоняется")
    void createPatientWithNonExistingIdentifierTypeReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithNonExistingIdentifierType();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    @Test
    @DisplayName("PAT-P1-10: Создание пациента с несуществующей локацией идентификатора отклоняется")
    void createPatientWithNonExistingLocationReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithNonExistingLocation();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    @Test
    @DisplayName("PAT-P1-11: Поиск несуществующего пациента возвращает пустой список")
    void searchNonExistingPatientReturnsEmptyResults() {
        String nonExistingQuery =
                PatientTestData.nonExistingQuery();

        PatientSearchResponse searchResponse =
                admin.patients().searchPatients(
                        new PatientSearchParams(nonExistingQuery)
                );

        softly.assertThat(searchResponse.results())
                .as("Search results for non-existing patient")
                .isEmpty();
    }

    @Test
    @DisplayName("PAT-P1-12: Создание пациента без даты рождения")
    void createPatientWithoutBirthdate() {
        PatientCreateRequest request =
                PatientTestData.patientWithoutBirthdate();

        PatientResponse response =
                admin.patients().createPatient(request);

        ModelAssertions.assertThatModels(request, response).match();

        softly.assertThat(response.person().birthdate())
                .as("Patient birthdate")
                .isNull();

        softly.assertThat(response.person().age())
                .as("Patient age")
                .isNull();
    }
}