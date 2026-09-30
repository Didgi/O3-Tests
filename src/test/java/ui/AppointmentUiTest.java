package ui;

import api.models.patients.PatientResponse;
import api.utils.RandomData;
import common.annotations.WithPatient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.AppointmentPage;
import ui.pages.ServiceQueuesPage;

import static com.codeborne.selenide.Condition.visible;

public class AppointmentUiTest extends UIBaseTest {
    @Test
    @DisplayName("Переход на страницу appointments")
    public void openAppointmentPageUiTest() {
        new ServiceQueuesPage().open().goToAppointmentPage().getGetHeaderText().shouldBe(visible);
    }

    @Test
    @DisplayName("Создание appointment")
    @WithPatient
    public void searchPatientInCreateAppointmentPanelUiTest(PatientResponse patient) {
        String personName = patient.person().preferredName().display();

        new AppointmentPage()
                .open()
                .openCreateAppointmentPanel()
                .shouldBeOpened()
                .searchPatient(personName)
                .patientShouldBeFound(personName)
                .selectPatient(personName)
                .fillForm(
                        "General Medicine service",
                        RandomData.randomFutureDate(),
                        RandomData.generateRandomTime(),
                        "30",
                        "Test"
                )
                .saveAndClose();
    }

}
