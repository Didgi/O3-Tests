package ui;

import api.models.appointment.AppointmentResponse;
import api.models.patients.PatientResponse;
import api.testdata.ReferenceTestData;
import api.utils.RandomData;
import common.annotations.WithPatient;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.AppointmentPage;
import ui.pages.ServiceQueuesPage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.visible;

public class AppointmentUiTest extends UIBaseTest {

    @Test
    @Issue("TRACK-013")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Переход на страницу appointments")
    public void openAppointmentPageUiTest() {
        new ServiceQueuesPage().open().goToAppointmentPage().getGetHeaderText().shouldBe(visible);
    }

    @Test
    @Issue("TRACK-013")
    @Severity(SeverityLevel.CRITICAL)
    @WithPatient
    @DisplayName("Создание appointment")
    public void createAppointmentPanelUiTest(PatientResponse patient) {
        String personName = patient.person().preferredName().display();
        String service = ReferenceTestData.appointmentServiceName(); 
        LocalDate date = RandomData.randomFutureDate();
        String time = RandomData.generateRandomTime();
        String duration = RandomData.generateRandomDuration();
        String note = RandomData.randomString(10);
        String parsedDate = date.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);


        new AppointmentPage()
                .open()
                .openCreateAppointmentPanel()
                .shouldBeOpened()
                .searchPatient(personName)
                .patientShouldBeFound(personName)
                .selectPatient(personName)
                .fillForm(
                        service,
                        date,
                        time,
                        duration,
                        note
                )
                .selectFormat()
                .saveAndClose()
                .appointmentScheduledShouldAppear();

        AppointmentResponse createdAppointment =
                admin.appointments().getAppointmentsForDate(parsedDate)
                        .stream()
                        .filter(ap -> ap.patient().name().equals(personName))
                        .toList()
                        .getFirst();

        softly.assertThat(createdAppointment.patient().name()).isEqualTo(personName);
        softly.assertThat(createdAppointment.service().name()).isEqualTo(service);

        String period = ReferenceTestData.appointmentTimePeriod();
        LocalTime localTime = RandomData.to24HourTime(time, period);

        long actualTimestamp = Long.parseLong(
                createdAppointment.startDateTime()
        );
        long expectedTimestamp = date
                .atTime(localTime)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli();
        softly.assertThat(actualTimestamp)
                .isEqualTo(expectedTimestamp);
    }
}
