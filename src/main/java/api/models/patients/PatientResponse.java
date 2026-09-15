package api.models.patients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientResponse(
        String uuid,
        List<PatientIdentifierResponse> identifiers,
        PatientPersonResponse person
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PatientPersonResponse(
            String uuid,
            String gender,
            String birthdate,
            Integer age,
            boolean birthdateEstimated,
            boolean dead,
            PreferredName preferredName
    ){
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PreferredName(
            String uuid,
            String display
    ){
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PatientIdentifierResponse(
            String uuid
    ) {
    }
}