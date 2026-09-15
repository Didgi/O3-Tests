package api.models.patients;

import java.util.List;

public record PatientUpdateRequest(
        String uuid,
        List<PatientIdentifierUpdateRequest> identifiers,
        PatientPersonUpdateRequest person
) {
}