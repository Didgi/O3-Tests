package api.models.observations;

import com.fasterxml.jackson.databind.JsonNode;

public record ObservationCreateRequest(
        String person,
        String concept,
        String obsDatetime,
        JsonNode value
) {
}
