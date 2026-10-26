package api.appointment;

import api.BaseApiTest;
import api.models.appointment.AppointmentCreateRequest;
import api.models.appointment.AppointmentResponse;
import api.models.appointment.AppointmentStatus;
import api.models.appointment.AppointmentStatusChangeRequest;
import api.models.patients.PatientResponse;
import api.testdata.AppointmentTestData;
import api.testdata.ReferenceTestData;
import api.utils.comparison.ModelAssertions;
import common.annotations.WithPatient;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentTest extends BaseApiTest {

    @Test
    @Issue("TRACK-017")
    @Severity(SeverityLevel.CRITICAL)
    @WithPatient
    @DisplayName("Создание нового appointment")
    void adminCanCreateAppointment(PatientResponse patient) {
        AppointmentCreateRequest appointmentCreateRequest = AppointmentTestData.validAppointmentCreateRequest(patient.uuid());
        AppointmentResponse created = admin.appointments().createAppointment(appointmentCreateRequest);

        ModelAssertions.assertThatModels(appointmentCreateRequest, created).match();

        softly.assertThat(created.voided())
                .as("Newly created appointment is not voided")
                .isFalse();

        AppointmentResponse fetched = admin.appointments().getAppointment(created.uuid());

        ModelAssertions.assertThatModels(appointmentCreateRequest, fetched).match();

        softly.assertThat(fetched.uuid())
                .as("GET returns the created appointment")
                .isEqualTo(created.uuid());
    }

    @Test
    @Issue("TRACK-017")
    @Severity(SeverityLevel.CRITICAL)
    @Disabled("Тест стабильно падает")
    @WithPatient
    @DisplayName("Получение списка Appointment за один день")
    void getAppointmentsByOneDay(PatientResponse patient) {

        AppointmentCreateRequest appointmentCreateRequest = AppointmentTestData.validAppointmentCreateRequest(patient.uuid());

        AppointmentResponse created = admin.appointments().createAppointment(appointmentCreateRequest);
        List<AppointmentResponse> appointments =
                admin.appointments().getAppointmentsForDate(appointmentCreateRequest.startDateTime());

        softly.assertThat(appointments)
                .as("Search by date contains the created appointment")
                .extracting(AppointmentResponse::uuid)
                .contains(created.uuid());
    }

    @Test
    @Issue("TRACK-017")
    @Severity(SeverityLevel.CRITICAL)
    @WithPatient
    @DisplayName("Получение списка Appointments пациента")
    void getAppointmentsByPatient(PatientResponse patient) {
        AppointmentCreateRequest appointmentCreateRequest = AppointmentTestData.validAppointmentCreateRequest(patient.uuid());
        AppointmentResponse created = admin.appointments().createAppointment(appointmentCreateRequest);

        List<AppointmentResponse> appointments =
                admin.appointments().searchAppointmentsByPatient(
                        patient.uuid(),
                        Instant.now().toString()
                );

        softly.assertThat(appointments)
                .as("Search by patient contains the created appointment")
                .extracting(AppointmentResponse::uuid)
                .contains(created.uuid());
    }

    @Test
    @Issue("TRACK-017")
    @Severity(SeverityLevel.CRITICAL)
    @WithPatient
    @DisplayName("Checkout appointment: статус становится Completed")
    void adminCanCheckoutAppointment(PatientResponse patient) {
        AppointmentCreateRequest appointmentCreateRequest = AppointmentTestData.validAppointmentCreateRequest(patient.uuid());
        AppointmentResponse created = admin.appointments().createAppointment(appointmentCreateRequest);

        String onDate = ZonedDateTime.now(ZoneId.of(TIME_ZONE)).format(ON_DATE_FORMAT);
        admin.appointments().changeAppointmentStatus(
                created.uuid(),
                new AppointmentStatusChangeRequest(onDate, TIME_ZONE, AppointmentStatus.CHECKED_IN.toString())
        );

        admin.appointments().changeAppointmentStatus(
                created.uuid(),
                new AppointmentStatusChangeRequest(onDate, TIME_ZONE, AppointmentStatus.COMPLETED.toString())
        );

        AppointmentResponse fetched = admin.appointments().getAppointment(created.uuid());

        softly.assertThat(fetched.status())
                .as("Appointment status after checkout is Completed")
                .isEqualTo(AppointmentStatus.COMPLETED.toString());
    }

}
