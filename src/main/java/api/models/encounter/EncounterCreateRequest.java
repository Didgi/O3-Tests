package api.models.encounter;

import java.util.List;

public record EncounterCreateRequest(
        String encounterDatetime,
        String patient,
        String encounterType,
        String location,
        List<EncounterProviderRequest> encounterProviders,
        String visit
) {
}
