package api.models.observations;

import java.util.List;

public record ObservationSearchResponse(
        List<ObservationResponse> results
) {
}
