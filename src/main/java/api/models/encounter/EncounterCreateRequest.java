package api.models.encounter;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EncounterCreateRequest(
        String encounterDatetime,
        String patient,
        String encounterType,
        String location,
        List<EncounterProviderRequest> encounterProviders,
        String visit
) {
}
