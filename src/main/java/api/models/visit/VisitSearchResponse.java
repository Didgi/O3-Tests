package api.models.visit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VisitSearchResponse(
        List<VisitCreateResponse> results
) {
    public VisitSearchResponse {
        results = results == null ? List.of() : results;
    }
}
