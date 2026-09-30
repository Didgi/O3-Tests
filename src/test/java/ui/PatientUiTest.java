package ui;

import api.models.patients.PatientCreateRequest;
import api.models.patients.PatientNameRequest;
import api.models.patients.PatientPersonRequest;
import api.models.patients.PatientResponse;
import api.testdata.PatientTestData;
import common.annotations.WithPatient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import ui.pages.PatientChartPage;

@Execution(ExecutionMode.SAME_THREAD)
public class PatientUiTest extends UIBaseTest {

    @Test
    @DisplayName("PAT-UI-P0-01 Регистрация пациента с валидными минимальными данными")
    public void registerPatientWithValidMinimalData() {
        PatientPersonRequest patient = PatientTestData.validPatientPerson();

        PatientNameRequest patientName = patient.names().getFirst();

        PatientChartPage patientChartPage = serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickAddPatient()
                .checkPatientRegistrationOpened()
                .inputGivenName(patientName.givenName())
                .inputFamilyName(patientName.familyName())
                .selectGender(patient.gender())
                .inputBirthDate(patient.birthdate())
                .registerPatient();

        patientChartPage.checkPatientName(
                patientName.givenName(),
                patientName.familyName()
        );
    }

    @Test
    @WithPatient
    @DisplayName("PAT-UI-P0-02 Поиск пациента и открытие Patient Chart")
    public void searchPatientAndOpenPatientChart(PatientCreateRequest request, PatientResponse created) {
        PatientNameRequest patientName = request.person().names().getFirst();

        PatientChartPage patientChartPage = serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickSearchPatient()
                .searchPatient(patientName.givenName())
                .openPatient(created.uuid());

        patientChartPage.checkPatientName(patientName.givenName(), patientName.middleName(), patientName.familyName());
    }

    @Test
    @WithPatient
    @DisplayName("PAT-UI-P0-03 Редактирование данных пациента")
    public void editPatient(PatientCreateRequest request, PatientResponse created) {
        PatientNameRequest patientName = request.person().names().getFirst();
        String newGivenName = PatientTestData.updatedGivenName();

        PatientChartPage patientChartPage = serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickSearchPatient()
                .searchPatient(patientName.givenName())
                .openPatient(created.uuid());

        patientChartPage
                .openEditPatientDetails()
                .inputGivenName(newGivenName)
                .updatePatient()
                .refreshPage()
                .checkPatientName(newGivenName, patientName.middleName(), patientName.familyName());
    }

    @Test
    @DisplayName("PAT-UI-P1-01 Регистрация без имени невозможна")
    public void registrationWithoutGivenName() {
        PatientPersonRequest patient = PatientTestData.validPatientPerson();
        PatientNameRequest patientName = patient.names().getFirst();

        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickAddPatient()
                .checkPatientRegistrationOpened()
                .inputFamilyName(patientName.familyName())
                .selectGender(patient.gender())
                .inputBirthDate(patient.birthdate())
                .submitRegistration()
                .checkPatientRegistrationOpened();
    }

    @Test
    @DisplayName("PAT-UI-P1-02 Регистрация без пола невозможна")
    public void registrationWithoutGender() {
        PatientPersonRequest patient = PatientTestData.validPatientPerson();
        PatientNameRequest patientName = patient.names().getFirst();

        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickAddPatient()
                .checkPatientRegistrationOpened()
                .inputGivenName(patientName.givenName())
                .inputFamilyName(patientName.familyName())
                .inputBirthDate(patient.birthdate())
                .submitRegistration()
                .checkGenderRequiredError()
                .checkPatientRegistrationOpened();
    }

    @Test
    @DisplayName("PAT-UI-P1-03 Регистрация без даты рождения невозможна")
    public void registrationWithoutBirthDate() {
        PatientPersonRequest patient = PatientTestData.validPatientPerson();
        PatientNameRequest patientName = patient.names().getFirst();

        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickAddPatient()
                .checkPatientRegistrationOpened()
                .inputGivenName(patientName.givenName())
                .inputFamilyName(patientName.familyName())
                .selectGender(patient.gender())
                .submitRegistration()
                .checkBirthDateRequiredError()
                .checkPatientRegistrationOpened();
    }

    @Test
    @DisplayName("PAT-UI-P1-04 Регистрация пациента с неизвестным именем")
    public void registerPatientWithUnknownName() {
        PatientPersonRequest patient = PatientTestData.validPatientPerson();
        String unknownName = PatientTestData.unknownName();

        PatientChartPage patientChartPage = serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickAddPatient()
                .checkPatientRegistrationOpened()
                .selectUnknownName()
                .selectGender(patient.gender())
                .inputBirthDate(patient.birthdate())
                .registerPatient();

        patientChartPage.checkPatientName(unknownName,unknownName);
    }

    @Test
    @DisplayName("PAT-UI-P1-05 Регистрация пациента с приблизительным возрастом")
    public void registerPatientWithEstimatedAge() {
        PatientPersonRequest patient = PatientTestData.validPatientPerson();
        PatientNameRequest patientName = patient.names().getFirst();

        PatientChartPage patientChartPage = serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickAddPatient()
                .checkPatientRegistrationOpened()
                .inputGivenName(patientName.givenName())
                .inputFamilyName(patientName.familyName())
                .selectGender(patient.gender())
                .selectBirthDateUnknown()
                .inputEstimatedAge(patient.birthdate())
                .registerPatient();

        patientChartPage.checkPatientName(patientName.givenName(), patientName.familyName());
    }

    @Test
    @WithPatient
    @DisplayName("PAT-UI-P1-06 Отмена несохранённых изменений пациента")
    public void discardUnsavedPatientChanges(PatientCreateRequest request, PatientResponse created) {
        PatientNameRequest patientName = request.person().names().getFirst();
        String newGivenName = PatientTestData.updatedGivenName();

        PatientChartPage patientChartPage = serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickSearchPatient()
                .searchPatient(patientName.givenName())
                .openPatient(created.uuid());

        patientChartPage
                .openEditPatientDetails()
                .inputGivenName(newGivenName)
                .cancelEditing()
                .checkDiscardChangesModal()
                .discardChanges()
                .checkPatientName(patientName.givenName(), patientName.middleName(), patientName.familyName());
    }

    @Test
    @DisplayName("PAT-UI-P1-07 Проверка состояния поиска при отсутствии результатов")
    public void searchNonexistentPatient() {
        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickSearchPatient()
                .searchPatient(PatientTestData.nonExistingQuery())
                .checkNoPatientsFound();
    }
}