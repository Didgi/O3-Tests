package api.models.encounter;

public record EncounterProviderRequest(
        String provider,
        String encounterRole
) {
}
