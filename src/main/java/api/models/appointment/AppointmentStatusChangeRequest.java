package api.models.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppointmentStatusChangeRequest(
        String onDate,
        String timeZone,
        String toStatus
) {
}
