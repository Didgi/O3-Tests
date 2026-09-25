package common.extensions;

import api.models.observations.ObservationCreateRequest;
import api.models.patients.PatientResponse;
import api.testdata.ReferenceTestData;
import api.testdata.SeedObservationConcept;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import common.annotations.GeneratedObservationRequest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class ObservationRequestExtension implements ParameterResolver {

    @Override
    public ObservationCreateRequest resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    )
            throws ParameterResolutionException {
        GeneratedObservationRequest observationRequest =
                parameterContext.findAnnotation(
                        GeneratedObservationRequest.class
                ).orElseThrow();

        ExtensionContext.Store store =
                extensionContext.getStore(OpenMrsFixtureExtension.NAMESPACE);

        PatientResponse patient =
                store.get(
                        PatientResponse.class,
                        PatientResponse.class
                );

        if(patient == null) {
            throw new ParameterResolutionException(
                    "@GeneratedObservationRequest requires @WithPatient"
            );
        }

        return createObservationRequest(
                patient.uuid(),
                observationRequest.concept()
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == ObservationCreateRequest.class
                && parameterContext.isAnnotated(GeneratedObservationRequest.class);
    }

    private ObservationCreateRequest createObservationRequest(
            String patientUuid,
            SeedObservationConcept concept
    ) {
        return switch (concept) {
            case WEIGHT -> createRequest(
                    patientUuid,
                    ReferenceTestData.weightConceptId(),
                    IntNode.valueOf(70)
            );

            case HEIGHT -> createRequest(
                    patientUuid,
                    ReferenceTestData.heightConceptId(),
                    IntNode.valueOf(170)
            );

            case TEXT -> createRequest(
                    patientUuid,
                    ReferenceTestData.textConceptId(),
                    TextNode.valueOf("clear and colorless")
            );
        };
    }

    private ObservationCreateRequest createRequest(
            String patientUuid,
            String conceptUuid,
            JsonNode value
    ) {
        return ObservationCreateRequest.builder()
                .person(patientUuid)
                .obsDatetime(
                        OffsetDateTime.now(ZoneOffset.UTC)
                                .truncatedTo(ChronoUnit.SECONDS)
                )
                .value(value)
                .concept(conceptUuid)
                .build();
    }
}
