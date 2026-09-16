package api.models.visit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VisitCreateResponse(
        String uuid,
        String display,
        ResourceReference patient,
        ResourceReference visitType,
        ResourceReference indication,
        ResourceReference location,
        String startDatetime,
        String stopDatetime,
        List<ResourceReference> encounters,
        List<ResourceReference> attributes,
        boolean voided,
        List<Link> links,
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
    public record Link(
            String rel,
            String uri,
            String resourceAlias
    ) {
    }
}
