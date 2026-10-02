package api.models.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DrugOrderCreateRequest(
        @JsonProperty(required = true)
        String patient,

        @JsonProperty(required = true)
        String concept,

        @JsonProperty(required = true)
        String orderType,

        @JsonProperty(required = true)
        String type,

        String action,

        String urgency,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime dateActivated,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime scheduledDate,

        String careSetting,

        String encounter,

        String orderer,

        String previousOrder,

        @JsonProperty(required = true)
        String drug,

        String dosingType,

        Double dose,

        String doseUnits,

        String route,

        String frequency,

        Boolean asNeeded,

        String asNeededCondition,

        String dosingInstructions,

        Double quantity,

        String quantityUnits,

        Integer numRefills,

        Integer duration,

        String durationUnits,

        Boolean dispenseAsWritten,

        String brandName,

        String orderReason,

        String orderReasonNonCoded,

        String instructions,

        String commentToFulfiller,

        String accessionNumber,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime dateStopped,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime autoExpireDate
) {
}
