package api.models.user;

public record LinkResponse(
        String rel,
        String uri,
        String resourceAlias
) {
}