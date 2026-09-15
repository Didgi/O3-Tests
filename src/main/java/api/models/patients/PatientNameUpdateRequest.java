package api.models.patients;

public record PatientNameUpdateRequest(
        String uuid,
        boolean preferred,
        String givenName,
        String middleName,
        String familyName
) {
}