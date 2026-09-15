package api;

import api.models.patients.*;
import api.requests.steps.ApiClient;
import api.specs.ResponseSpecs;
import api.testdata.PatientTestData;
import api.utils.comparison.ModelAssertions;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PatientTest extends BaseApiTest {
    private ApiClient admin;

    private void assertPreferredName(
            PatientResponse response,
            String expectedDisplay
    ) {
        softly.assertThat(response.person().preferredName())
                .as("Preferred name")
                .isNotNull()
                .satisfies(name ->
                        softly.assertThat(name.display())
                                .as("Preferred name display")
                                .isEqualTo(expectedDisplay)
                );
    }

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    // PAT-P0-01: создание пациента с валидными минимальными данными
    @Test
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

    // PAT-P0-02: получение созданного пациента по UUID
    @Test
    void getCreatedPatientByUuid() {
        PatientCreateRequest request =
                PatientTestData.validPatient();

        PatientResponse created =
                admin.patients().createPatient(request);

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
                                                            + expectedName.familyName()
                                            )
                            );
                });
    }

    // PAT-P0-03: поиск пациента по идентификатору возвращает созданного пациента
    @Test
    void searchPatientByIdentifierReturnsCreatedPatient() {
        PatientCreateRequest request =
                PatientTestData.validPatient();

        PatientResponse created =
                admin.patients().createPatient(request);

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

    // PAT-P0-04: обновление пациента с сохранением остальных данных
    @Test
    void updatePatientAndPreserveOtherData() {
        PatientCreateRequest createRequest =
                PatientTestData.validPatient();

        PatientResponse created =
                admin.patients().createPatient(createRequest);

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

    // PAT-P0-05: отклонение дублирующего идентификатора пациента
    @Test
    void rejectDuplicatePatientIdentifier() {
        PatientCreateRequest firstRequest =
                PatientTestData.validPatient();

        admin.patients().createPatient(firstRequest);

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

    // PAT-P1-01: поиск пациента по имени возвращает созданного пациента
    @Test
    void searchPatientByNameReturnsCreatedPatient() {
        PatientCreateRequest request =
                PatientTestData.validPatient();

        PatientResponse created =
                admin.patients().createPatient(request);

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

    // PAT-P1-02: получение пациента по несуществующему UUID возвращает 404
    @Test
    void getPatientByNonExistingUuidReturnsNotFound() {
        String nonExistingUuid =
                PatientTestData.nonExistingUuid();

        Response response =
                admin.patients().getPatientRaw(nonExistingUuid);

        response.then()
                .spec(ResponseSpecs.requestReturnsNotFound());
    }

    // PAT-P1-03: создание пациента без имени отклоняется
    @Test
    void createPatientWithoutNameReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithoutName();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    // PAT-P1-04: создание пациента без пола отклоняется
    @Test
    void createPatientWithoutGenderReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithoutGender();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    // PAT-P1-05: создание пациента без идентификатора отклоняется
    @Test
    void createPatientWithoutIdentifierReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithoutIdentifier();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    // PAT-P1-06: создание пациента с датой рождения в будущем отклоняется
    @Test
    void createPatientWithFutureBirthdateReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithFutureBirthdate();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    // PAT-P1-07: создание пациента со слишком старой датой рождения отклоняется
    @Test
    void createPatientWithTooOldBirthdateReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithTooOldBirthdate();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    // PAT-P1-08: создание пациента с невалидным идентификатором отклоняется
    @Test
    void createPatientWithInvalidIdentifierReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithInvalidIdentifier();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    // PAT-P1-09: создание пациента с несуществующим типом идентификатора отклоняется
    @Test
    void createPatientWithNonExistingIdentifierTypeReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithNonExistingIdentifierType();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    // PAT-P1-10: создание пациента с несуществующей локацией идентификатора отклоняется
    @Test
    void createPatientWithNonExistingLocationReturnsBadRequest() {
        PatientCreateRequest request =
                PatientTestData.patientWithNonExistingLocation();

        Response response =
                admin.patients().createPatientRaw(request);

        response.then()
                .spec(ResponseSpecs.requestReturnsBadRequest());
    }

    // PAT-P1-11: поиск несуществующего пациента возвращает пустой список
    @Test
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

    // PAT-P1-12: создание пациента без даты рождения
    @Test
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