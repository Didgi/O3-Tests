package api.models.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserSearchResponse(
        List<UserItem> results
) {
    public UserSearchResponse {
        results = results == null ? List.of() : results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserItem(
            String uuid,
            String display,
            String username
    ) {
    }
}