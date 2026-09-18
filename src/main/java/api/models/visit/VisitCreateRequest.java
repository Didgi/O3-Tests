package api.models.visit;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VisitCreateRequest(
        String patient,
        String visitType,
        String startDatetime,
        String location
) {
}
