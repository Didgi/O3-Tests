package api.models.user;

import api.utils.GeneratingRule;
import api.utils.RegexData;

public record RoleRequest(
        @GeneratingRule(regex = RegexData.ROLE_NAME)
        String name,
        @GeneratingRule(regex = RegexData.ROLE_DESCRIPTION)
        String description
) {
}
