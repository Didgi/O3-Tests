package api.models.patients;

public record PatientIdentifierRequest(
        String identifier,
        String identifierType,
        String location,
        boolean preferred
) {
}
