package api.testdata;

import api.models.appointment.AppointmentCreateRequest;
import api.utils.RandomData;

import java.time.LocalDateTime;

public class AppointmentTestData {
    private static LocalDateTime start = RandomData.startDate();
    private static LocalDateTime end = RandomData.endDate();

    public static AppointmentCreateRequest validAppointmentCreateRequest(String patientUuid) {
        return new AppointmentCreateRequest(
                patientUuid,
                ReferenceTestData.appointmentServiceUuid(),
                start.toString(),
                end.toString(),
                ReferenceTestData.appointmentKind(),
                ReferenceTestData.locationUuid(),
                start.toString()
        );
    }
}
