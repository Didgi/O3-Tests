package api.models.observations;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ObservationResponse(
        String uuid,
        String display,
        Concept concept,
        Person person,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        OffsetDateTime obsDatetime,
        String accessionNumber,
        ObservationResponse obsGroup,
        String valueCodedName,
        List<ObservationResponse> groupMembers,
        String comment,
        ResourceReference location,
        String order,
        ResourceReference encounter,
        boolean voided,
        JsonNode value,
        String valueModifier,
        String formFieldPath,
        String formFieldNamespace,
        String status,
        String interpretation,
        ReferenceRange referenceRange,
        List<Link> links,
        String resourceVersion,
        List<ObservationResponse> previousVersions
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Concept(
            String uuid,
            String display,
            List<Link> links
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Person(
            String uuid,
            String display,
            List<Link> links
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceReference(
            String uuid,
            String display,
            List<Link> links
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReferenceRange(
            String display,
            String uuid,
            Double hiNormal,
            Double hiAbsolute,
            Double hiCritical,
            Double lowNormal,
            Double lowAbsolute,
            Double lowCritical,
            List<Link> links,
            String resourceVersion
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
