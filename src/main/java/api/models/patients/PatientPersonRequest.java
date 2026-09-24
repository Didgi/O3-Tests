package api.models.patients;

import api.utils.BooleanGeneration;
import api.utils.GeneratingRule;
import api.utils.RegexData;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record PatientPersonRequest(
        List<PatientAddressRequest> addresses,
        List<JsonNode> attributes,
        @GeneratingRule(regex = RegexData.BIRTHDATE_TEMPLATE)
        String birthdate,
        @GeneratingRule(regex = "", booleanValue = BooleanGeneration.FALSE)
        boolean birthdateEstimated,
        @GeneratingRule(regex = "", booleanValue = BooleanGeneration.FALSE)
        boolean dead,
        @GeneratingRule(regex = RegexData.GENDER_TEMPLATE)
        String gender,
        List<PatientNameRequest> names
){
}