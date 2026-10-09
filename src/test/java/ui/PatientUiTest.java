package ui;

import api.models.patients.PatientCreateRequest;
import api.models.patients.PatientNameRequest;
import api.models.patients.PatientPersonRequest;
import api.models.patients.PatientResponse;
import api.testdata.PatientTestData;
import common.annotations.WithPatient;
import common.annotations.WithPatientData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

public class PatientUiTest extends UIBaseTest {
    @Test
    @WithPatientData
    @DisplayName("PAT-UI-P0-01 Регистрация пациента с валидными минимальными данными")
    public void registerPatientWithValidMinimalData(PatientPersonRequest patient, PatientNameRequest patientName) {
        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickAddPatient()
                .checkPatientRegistrationOpened()
                .inputGivenName(patientName.givenName())
                .inputFamilyName(patientName.familyName())
                .selectGender(patient.gender())
                .inputBirthDate(patient.birthdate())
                .registerPatient()
                .getPatientAside()
                .getPatientBanner()
                .checkPatientName(patientName.givenName(), patientName.familyName());
    }

    @Test
    @WithPatient
    @DisplayName("PAT-UI-P0-02 Поиск пациента и открытие Patient Chart")
    public void searchPatientAndOpenPatientChart(PatientCreateRequest request, PatientResponse created) {
        PatientNameRequest patientName = request.person().names().getFirst();

        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickSearchPatient()
                .searchPatient(patientName.givenName())
                .openPatient(created.uuid())
                .getPatientAside()
                .getPatientBanner()
                .checkPatientName(patientName.givenName(), patientName.middleName(), patientName.familyName());
    }

    @Test
    @WithPatient
    @DisplayName("PAT-UI-P0-03 Редактирование данных пациента")
    public void editPatient(PatientCreateRequest request, PatientResponse created) {
        PatientNameRequest patientName = request.person().names().getFirst();
        String newGivenName = PatientTestData.updatedGivenName();

        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickSearchPatient()
                .searchPatient(patientName.givenName())
                .openPatient(created.uuid())
                .getPatientAside()
                .getPatientBanner()
                .openEditPatientDetails()
                .inputGivenName(newGivenName)
                .updatePatient()
                .refreshPage()
                .getPatientAside()
                .getPatientBanner()
                .checkPatientName(newGivenName, patientName.middleName(), patientName.familyName());
    }

    @Test
    @WithPatientData
    @DisplayName("PAT-UI-P1-01 Регистрация без имени невозможна")
    public void registrationWithoutGivenName(PatientPersonRequest patient, PatientNameRequest patientName) {
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
    @WithPatientData
    @DisplayName("PAT-UI-P1-02 Регистрация без пола невозможна")
    public void registrationWithoutGender(PatientPersonRequest patient, PatientNameRequest patientName) {
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
    @WithPatientData
    @DisplayName("PAT-UI-P1-03 Регистрация без даты рождения невозможна")
    public void registrationWithoutBirthDate(PatientPersonRequest patient, PatientNameRequest patientName) {
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
    @WithPatientData
    @DisplayName("PAT-UI-P1-04 Регистрация пациента с неизвестным именем")
    public void registerPatientWithUnknownName(PatientPersonRequest patient) {
        String unknownName = PatientTestData.unknownName();

        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickAddPatient()
                .checkPatientRegistrationOpened()
                .selectUnknownName()
                .selectGender(patient.gender())
                .inputBirthDate(patient.birthdate())
                .registerPatient()
                .getPatientAside()
                .getPatientBanner()
                .checkPatientName(unknownName, unknownName);
    }

    @Test
    @WithPatientData
    @DisplayName("PAT-UI-P1-05 Регистрация пациента с приблизительным возрастом")
    public void registerPatientWithEstimatedAge(PatientPersonRequest patient, PatientNameRequest patientName) {
        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickAddPatient()
                .checkPatientRegistrationOpened()
                .inputGivenName(patientName.givenName())
                .inputFamilyName(patientName.familyName())
                .selectGender(patient.gender())
                .selectBirthDateUnknown()
                .inputEstimatedAge(patient.birthdate())
                .registerPatient()
                .getPatientAside()
                .getPatientBanner()
                .checkPatientName(patientName.givenName(), patientName.familyName());
    }

    @Test
    @WithPatient
    @DisplayName("PAT-UI-P1-06 Отмена несохранённых изменений пациента")
    public void discardUnsavedPatientChanges(PatientCreateRequest request, PatientResponse created) {
        PatientNameRequest patientName = request.person().names().getFirst();
        String newGivenName = PatientTestData.updatedGivenName();

        serviceQueuesPage
                .open()
                .checkServiceQueuesOpened()
                .clickSearchPatient()
                .searchPatient(patientName.givenName())
                .openPatient(created.uuid())
                .getPatientAside()
                .getPatientBanner()
                .openEditPatientDetails()
                .inputGivenName(newGivenName)
                .cancelEditing()
                .checkDiscardChangesModal()
                .discardChanges()
                .getPatientAside()
                .getPatientBanner()
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