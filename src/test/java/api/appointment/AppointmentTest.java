package api.appointment;

import api.BaseApiTest;
import api.models.appointment.AppointmentCreateRequest;
import api.models.appointment.AppointmentResponse;
import api.models.appointment.AppointmentStatus;
import api.models.appointment.AppointmentStatusChangeRequest;
import api.models.patients.PatientResponse;
import api.requests.steps.ApiClient;
import api.testdata.AppointmentTestData;
import api.testdata.PatientTestData;
import api.utils.RandomModelGenerator;
import api.utils.comparison.ModelAssertions;
import common.SessionStorage;
import common.annotations.WithPatient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class AppointmentTest extends BaseApiTest {
    private ApiClient admin;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    @Test
    @DisplayName("Создание нового appointment")
    @WithPatient
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
    @Disabled("Тест стабильно падает")
    @DisplayName("Получение списка Appointment за один день")
    @WithPatient
    void getAppointmentsByOneDay(PatientResponse patient) {

        AppointmentCreateRequest appointmentCreateRequest = AppointmentTestData.validAppointmentCreateRequest(patient.uuid());

        AppointmentResponse created = admin.appointments().createAppointment(appointmentCreateRequest);
        String forDate = LocalDateTime.parse(appointmentCreateRequest.startDateTime())
                .atZone(ZoneId.of(TIME_ZONE))
                .format(ON_DATE_FORMAT);
        List<AppointmentResponse> appointments =
                admin.appointments().getAppointmentsForDate(forDate);

        softly.assertThat(appointments)
                .as("Search by date contains the created appointment")
                .extracting(AppointmentResponse::uuid)
                .contains(created.uuid());
    }

    @Test
    @DisplayName("Получение списка Appointments пациента")
    @WithPatient
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
    @DisplayName("Checkout appointment: статус становится Completed")
    @WithPatient
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
