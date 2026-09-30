package api.models.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
            List<OrderResponse.Link> links,
            String type
    ) {
    }
}
