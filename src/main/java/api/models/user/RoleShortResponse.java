package api.models.user;

import java.util.List;

public record RoleShortResponse(
        String uuid,
        String display,
        List<LinkResponse> links
) {
}