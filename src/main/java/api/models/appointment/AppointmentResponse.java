package api.models.appointment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppointmentResponse(
        String uuid,
        ResourceReference patient,
        ResourceReference service,
        String startDateTime,
        String endDateTime,
        String appointmentKind,
        ResourceReference location,
        String dateAppointmentScheduled,
        String status,
        boolean voided
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceReference(
            String uuid,
            String display,
            String name,
            List<Link> links
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Link(
            String rel,
            String uri,
            String resourceAlias
    ) {
    }
}
