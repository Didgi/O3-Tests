package api.models.encounter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EncounterResponse(
        String uuid,
        String display,
        String encounterDatetime,
        ResourceReference patient,
        ResourceReference encounterType,
        ResourceReference location,
        ResourceReference visit,
        List<EncounterProvider> encounterProviders,
        boolean voided
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceReference(
            String uuid,
            String display
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EncounterProvider(
            String uuid,
            ResourceReference provider,
            ResourceReference encounterRole
    ) {
    }
}
