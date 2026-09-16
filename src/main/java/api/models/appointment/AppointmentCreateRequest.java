package api.models.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppointmentCreateRequest(
        String patientUuid,
        String serviceUuid,
        String startDateTime,
        String endDateTime,
        String appointmentKind,
        String locationUuid,
        String dateAppointmentScheduled
) {
}
