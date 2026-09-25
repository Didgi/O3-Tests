package api.models.patients;

import api.utils.GeneratingRule;
import api.utils.RegexData;

public record PatientNameRequest(
        boolean preferred,
        @GeneratingRule(regex = RegexData.NAME_TEMPLATE)
        String givenName,
        @GeneratingRule(regex = RegexData.NAME_TEMPLATE)
        String middleName,
        @GeneratingRule(regex = RegexData.NAME_TEMPLATE)
        String familyName
) {
}