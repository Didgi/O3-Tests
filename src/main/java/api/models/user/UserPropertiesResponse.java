package api.models.user;

public record UserPropertiesResponse(
        String loginAttempts,
        String lockoutTimestamp
) {
}