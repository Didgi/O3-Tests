package api.models.auth.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionResponse(
        String sessionId,
        boolean authenticated,
        User user,
        String locale,
        List<String> allowedLocales,
        SessionLocation sessionLocation
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(
            String uuid,
            String display,
            String username,
            String systemId,
            Map<String, String> userProperties,
            Person person,
            List<Privilege> privileges,
            List<Role> roles
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Person(
            String uuid
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Privilege(
            String uuid,
            String name
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Role(
            String uuid,
            String name
    ) {
    }

    public record SessionLocation(
            String uuid,
            String display,
            List<SessionLocationLinks> links
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SessionLocationLinks(
            String rel,
            String uri,
            String resourceAlias
    ) {
    }
}