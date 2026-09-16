package api.appointment;

import api.BaseApiTest;
import api.models.appointment.AppointmentCreateRequest;
import api.models.appointment.AppointmentResponse;
import api.models.appointment.AppointmentStatus;
import api.models.appointment.AppointmentStatusChangeRequest;
import api.requests.steps.ApiClient;
import api.testdata.ReferenceTestData;
import api.utils.RandomData;
import api.utils.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class AppointmentTest extends BaseApiTest {
    LocalDateTime start = RandomData.startDate();
    LocalDateTime end = RandomData.endDate();
    private ApiClient admin;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    @Test
    @DisplayName("Создание нового appointment")
    void adminCanCreateAppointment() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                ReferenceTestData.patientUuid(),
                ReferenceTestData.appointmentServiceUuid(),
                start.toString(),
                end.toString(),
                ReferenceTestData.appointmentKind(),
                ReferenceTestData.locationUuid(),
                start.toString()
        );

        AppointmentResponse created = admin.appointments().createAppointment(request);

        ModelAssertions.assertThatModels(request, created).match();


        softly.assertThat(created.voided())
                .as("Newly created appointment is not voided")
                .isFalse();

        AppointmentResponse fetched = admin.appointments().getAppointment(created.uuid());

        ModelAssertions.assertThatModels(request, fetched).match();

        softly.assertThat(fetched.uuid())
                .as("GET returns the created appointment")
                .isEqualTo(created.uuid());
    }

    @Test
    @DisplayName("Получение списка Appointment за один день")
    void getAppointmentsByOneDay() {

        AppointmentCreateRequest request = new AppointmentCreateRequest(
                ReferenceTestData.patientUuid(),
                ReferenceTestData.appointmentServiceUuid(),
                start.toString(),
                end.toString(),
                AppointmentStatus.SCHEDULED.toString(),
                ReferenceTestData.locationUuid(),
                start.toString()
        );

        AppointmentResponse created = admin.appointments().createAppointment(request);

        String forDate = start.atZone(ZoneId.of(TIME_ZONE)).format(ON_DATE_FORMAT);
        List<AppointmentResponse> appointments =
                admin.appointments().getAppointmentsForDate(forDate);

        softly.assertThat(appointments)
                .as("Search by date contains the created appointment")
                .extracting(AppointmentResponse::uuid)
                .contains(created.uuid());
    }

    @Test
    @DisplayName("Получение списка Appointments пациента")
    void getAppointmentsByPatient() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                ReferenceTestData.patientUuid(),
                ReferenceTestData.appointmentServiceUuid(),
                start.toString(),
                end.toString(),
                AppointmentStatus.SCHEDULED.toString(),
                ReferenceTestData.locationUuid(),
                start.toString()
        );

        AppointmentResponse created = admin.appointments().createAppointment(request);

        List<AppointmentResponse> appointments =
                admin.appointments().searchAppointmentsByPatient(
                        ReferenceTestData.patientUuid(),
                        Instant.now().toString()
                );

        softly.assertThat(appointments)
                .as("Search by patient contains the created appointment")
                .extracting(AppointmentResponse::uuid)
                .contains(created.uuid());
    }

    @Test
    @DisplayName("Checkout appointment: статус становится Completed")
    void adminCanCheckoutAppointment() {
        AppointmentCreateRequest createRequest = new AppointmentCreateRequest(
                ReferenceTestData.patientUuid(),
                ReferenceTestData.appointmentServiceUuid(),
                start.toString(),
                end.toString(),
                AppointmentStatus.SCHEDULED.toString(),
                ReferenceTestData.locationUuid(),
                start.toString()
        );

        AppointmentResponse created = admin.appointments().createAppointment(createRequest);

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
