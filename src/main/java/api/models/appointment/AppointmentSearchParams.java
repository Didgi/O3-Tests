package api.models.appointment;

import api.requests.skeleton.query.QueryParams;

import java.util.HashMap;
import java.util.Map;

public record AppointmentSearchParams(
        String uuid,
        String forDate
) implements QueryParams {

    @Override
    public Map<String, ?> asMap() {
        Map<String, Object> params = new HashMap<>();

        if (uuid != null) {
            params.put("uuid", uuid);
        }

        if (forDate != null) {
            params.put("forDate", forDate);
        }

        return params;
    }
}
