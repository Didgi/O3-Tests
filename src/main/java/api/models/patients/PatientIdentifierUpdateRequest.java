package api.models.patients;

public record PatientIdentifierUpdateRequest(
        String uuid,
        String identifier,
        String identifierType,
        String location,
        boolean preferred
) {
}