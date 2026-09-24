package api.models.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppointmentPatientSearchRequest(
        String patientUuid,
        String startDate
) {
}
