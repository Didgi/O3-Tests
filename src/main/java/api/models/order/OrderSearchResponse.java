package api.models.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderSearchResponse(
        List<OrderItem> results
) {
    public OrderSearchResponse {
        results = results == null ? List.of() : results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderItem(
            String uuid,
            String display,
            String action,
            OrderResponse.ResourceReference previousOrder,
            Integer numRefills,
            List<OrderResponse.Link> links,
            String type,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            OffsetDateTime dateStopped
    ) {
    }
}
