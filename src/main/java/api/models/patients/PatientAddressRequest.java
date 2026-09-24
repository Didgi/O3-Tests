package api.models.patients;

import api.utils.GeneratingRule;
import api.utils.RegexData;

public record PatientAddressRequest(
        @GeneratingRule(regex = RegexData.ADDRESS_TEMPLATE)
        String address1,
        @GeneratingRule(regex = RegexData.CITY_TEMPLATE)
        String cityVillage,
        @GeneratingRule(regex = RegexData.COUNTRY_TEMPLATE)
        String country,
        @GeneratingRule(regex = RegexData.POSTAL_CODE_TEMPLATE)
        String postalCode
) {
}
