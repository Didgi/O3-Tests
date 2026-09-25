package api.models.appointment;

import api.utils.GeneratingRule;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Setter;

import static api.utils.RegexData.END_DATE;
import static api.utils.RegexData.START_DATE;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppointmentCreateRequest(
        @GeneratingRule(property = "patient_identifier_type_uuid")
        String patientUuid,
        @GeneratingRule(property = "test_appointment_service_uuid")
        String serviceUuid,
        @GeneratingRule(regex = START_DATE)
        String startDateTime,
        @GeneratingRule(regex = END_DATE)
        String endDateTime,
        @GeneratingRule(property = "test_appointment_kind")
        String appointmentKind,
        @GeneratingRule(property = "test_location_uuid")
        String locationUuid,
        @GeneratingRule(regex = START_DATE)
        String dateAppointmentScheduled
) {
}
