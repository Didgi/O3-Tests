package api.models.patients;

import api.utils.BooleanGeneration;
import api.utils.GeneratingRule;
import api.utils.RegexData;

public record PatientNameRequest(
        @GeneratingRule(regex = "", booleanValue = BooleanGeneration.TRUE)
        boolean preferred,
        @GeneratingRule(regex = RegexData.NAME_TEMPLATE)
        String givenName,
        @GeneratingRule(regex = RegexData.NAME_TEMPLATE)
        String middleName,
        @GeneratingRule(regex = RegexData.NAME_TEMPLATE)
        String familyName
) {
}