package api.models.patients;

public record PatientNameRequest(
        boolean preferred,
        String givenName,
        String middleName,
        String familyName
) {
}