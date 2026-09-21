package api.models.user;

import api.models.patients.PatientPersonRequest;
import api.utils.GeneratingRule;
import api.utils.RegexData;

import java.util.List;

public record UserCreateRequest(
        @GeneratingRule(regex = RegexData.USERNAME_TEMPLATE)
        String username,
        @GeneratingRule(regex = RegexData.PASSWORD_TEMPLATE)
        String password,
        PatientPersonRequest person,
        List<RoleRequest> roles,
        @GeneratingRule(regex = RegexData.USER_SYSTEM_ID)
        String systemId) {

}

