package api.models.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserSearchErrorResponse(
        ErrorItem error
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorItem(
            String message,
            String rawMessage,
            String translatedMessage
    ) {
    }
}