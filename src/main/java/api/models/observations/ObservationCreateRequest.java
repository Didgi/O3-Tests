package api.models.observations;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ObservationCreateRequest(
        @JsonProperty(required = true)
        String person,
        @JsonProperty(required = true)
        String concept,
        @JsonProperty(required = true)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime obsDatetime,
        JsonNode value,
        String encounter
) {
}
