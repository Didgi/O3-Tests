package api.models.visit;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VisitUpdateRequest(
        String stopDatetime
) {
}
