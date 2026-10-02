package api.models.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DrugOrderResponse(
        String uuid,
        String orderNumber,
        String accessionNumber,
        OrderResponse.ResourceReference patient,
        OrderResponse.ResourceReference concept,
        String action,
        OrderResponse.ResourceReference careSetting,
        OrderResponse.ResourceReference previousOrder,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime dateActivated,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime scheduledDate,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime dateStopped,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime autoExpireDate,
        OrderResponse.ResourceReference encounter,
        OrderResponse.ResourceReference orderer,
        OrderResponse.ResourceReference orderReason,
        String orderReasonNonCoded,
        OrderResponse.OrderType orderType,
        String urgency,
        String instructions,
        String commentToFulfiller,
        String display,
        OrderResponse.ResourceReference drug,
        String dosingType,
        Double dose,
        OrderResponse.ResourceReference doseUnits,
        OrderResponse.ResourceReference frequency,
        Boolean asNeeded,
        String asNeededCondition,
        Double quantity,
        OrderResponse.ResourceReference quantityUnits,
        Integer numRefills,
        String dosingInstructions,
        Integer duration,
        OrderResponse.ResourceReference durationUnits,
        OrderResponse.ResourceReference route,
        String brandName,
        Boolean dispenseAsWritten,
        String drugNonCoded,
        List<OrderResponse.Link> links,
        String type,
        String resourceVersion
) {
}
