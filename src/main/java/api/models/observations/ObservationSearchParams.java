package api.models.observations;

import api.requests.skeleton.query.QueryParams;
import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

@Builder
public record ObservationSearchParams(
        String patientUuid,
        String conceptUuid,
        String encounterUuid
) implements QueryParams {

    @Override
    public Map<String, ?> asMap() {
        Map<String, Object> params = new HashMap<>();

        if (patientUuid != null) {
            params.put("patient", patientUuid);
        }

        if (conceptUuid != null) {
            params.put("concept", conceptUuid);
        }

        if (encounterUuid != null) {
            params.put("encounter", encounterUuid);
        }

        return params;
    }
}
