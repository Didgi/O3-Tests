package api.models.visit;

import api.utils.GeneratingRule;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VisitCreateRequest(
        @GeneratingRule(property = "patient_identifier_type_uuid")
        String patient,
        @GeneratingRule(property = "test_visit_type_uuid")
        String visitType,
        @GeneratingRule(nullable = true)
        String startDatetime,
        @GeneratingRule(property = "test_location_uuid")
        String location
) {
}
