package api.models.user;

import java.util.List;

public record PersonShortResponse(
        String uuid,
        String display,
        List<LinkResponse> links
) {
}