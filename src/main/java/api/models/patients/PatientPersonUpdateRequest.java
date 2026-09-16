package api.models.patients;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record PatientPersonUpdateRequest(
        String uuid,
        List<JsonNode> addresses,
        List<JsonNode> attributes,
        String birthdate,
        boolean birthdateEstimated,
        boolean dead,
        String gender,
        List<PatientNameUpdateRequest> names
) {
}