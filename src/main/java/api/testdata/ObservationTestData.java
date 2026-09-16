package api.testdata;

import api.models.observations.ObservationCreateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class ObservationTestData {

    private static final OffsetDateTime OBS_DATETIME = OffsetDateTime.of(
            2026, 9, 14,
            17, 31, 0, 0,
            ZoneOffset.ofHours(3)
    );
    private static final int WEIGHT_VALUE = 70;
    private static final int HEIGHT_VALUE = 170;

    private ObservationTestData() {
    }

    public static ObservationCreateRequest validObservation(
            String patientUuid,
            JsonNode value
    ) {
        return validCreateWeightObservationRequest()
                .person(patientUuid)
                .value(value)
                .build();
    }

    public static ObservationCreateRequest validObservation(
            String patientUuid
    ) {
        return validCreateWeightObservationRequest()
                .person(patientUuid)
                .value(IntNode.valueOf(WEIGHT_VALUE))
                .build();
    }

    public static ObservationCreateRequest validHeightObservation(
            String patientUuid,
            JsonNode value
    ) {
        return ObservationCreateRequest.builder()
                .concept(ReferenceTestData.heightConceptId())
                .value(value)
                .obsDatetime(OBS_DATETIME)
                .person(patientUuid)
                .build();
    }

    public static ObservationCreateRequest validHeightObservation(
            String patientUuid
    ) {
        return ObservationCreateRequest.builder()
                .concept(ReferenceTestData.heightConceptId())
                .value(IntNode.valueOf(HEIGHT_VALUE))
                .obsDatetime(OBS_DATETIME)
                .person(patientUuid)
                .build();
    }

    public static ObservationCreateRequest validObservationWithEncounter(
            String patientUuid,
            String encounterUuid
    ) {
        return validCreateWeightObservationRequest()
                .encounter(encounterUuid)
                .person(patientUuid)
                .value(IntNode.valueOf(WEIGHT_VALUE))
                .build();
    }

    private static ObservationCreateRequest.ObservationCreateRequestBuilder
    validCreateWeightObservationRequest() {
        return ObservationCreateRequest.builder()
                .concept(ReferenceTestData.conceptId())
                .obsDatetime(OBS_DATETIME);
    }
}
