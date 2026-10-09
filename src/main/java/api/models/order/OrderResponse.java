package api.models.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderResponse(
        String uuid,
        String orderNumber,
        String accessionNumber,
        ResourceReference patient,
        ResourceReference concept,
        String action,
        ResourceReference careSetting,
        ResourceReference previousOrder,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime dateActivated,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime scheduledDate,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime dateStopped,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime autoExpireDate,
        ResourceReference encounter,
        ResourceReference orderer,
        ResourceReference orderReason,
        String orderReasonNonCoded,
        OrderType orderType,
        String urgency,
        String instructions,
        String commentToFulfiller,
        String display,
        ResourceReference specimenSource,
        ResourceReference laterality,
        String clinicalHistory,
        String frequency,
        Integer numberOfRepeats,
        List<Link> links,
        String type,
        String resourceVersion
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceReference(
            String uuid,
            String display,
            List<Link> links
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderType(
            String uuid,
            String display,
            String name,
            String javaClassName,
            boolean retired,
            String description,
            List<ConceptClass> conceptClasses,
            OrderType parent,
            List<Link> links,
            String resourceVersion
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConceptClass(
            String uuid,
            String display,
            List<Link> links
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Link(
            String rel,
            String uri,
            String resourceAlias
    ) {
    }
}
