package api.models.patients;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record PatientPersonRequest(
        List<JsonNode> addresses,
        List<JsonNode> attributes,
        String birthdate,
        boolean birthdateEstimated,
        boolean dead,
        String gender,
        List<PatientNameRequest> names
){
}