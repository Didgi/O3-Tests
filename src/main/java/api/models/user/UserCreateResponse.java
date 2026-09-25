package api.models.user;

import java.util.List;

public record UserCreateResponse(
        String uuid,
        String display,
        String username,
        String systemId,
        UserPropertiesResponse userProperties,
        PersonShortResponse person,
        List<Object> privileges,
        List<RoleShortResponse> roles,
        boolean retired,
        String email,
        List<LinkResponse> links,
        String resourceVersion
) {
}
