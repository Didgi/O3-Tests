package api.models.patients;

import java.util.List;

public record PatientSearchResponse(
        List<PatientResponse> results
) {
}