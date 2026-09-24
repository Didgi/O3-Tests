package api.models.observations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ObservationUpdateRequest(
        JsonNode value
) {
    public ObservationUpdateRequest() {
        this(null);
    }
}
