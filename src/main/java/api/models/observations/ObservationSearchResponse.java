package api.models.observations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ObservationSearchResponse(
        List<ObservationItem> results
) {
    public ObservationSearchResponse {
        results = results == null ? List.of() : results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ObservationItem(
            String uuid,
            String display,
            List<ObservationResponse.Link> links
    ) {
    }
}
