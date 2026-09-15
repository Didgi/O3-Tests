package api.models.patients;

import java.util.List;

public record PatientCreateRequest(
        List<PatientIdentifierRequest> identifiers,
        PatientPersonRequest person
) {
}