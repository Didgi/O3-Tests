package api.models.observations;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder(toBuilder = true)
public record ObservationCreateRequest(
        @JsonProperty(required = true)
        String person,
        @JsonProperty(required = true)
        String concept,
        @JsonProperty(required = true)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime obsDatetime,
        @JsonProperty(required = true)
        JsonNode value,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String encounter
) {
}
