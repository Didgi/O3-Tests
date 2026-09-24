package api.models.auth.request;

public record ChangePasswordCredentials(String oldPassword, String newPassword) {
}
