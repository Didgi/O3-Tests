package api.testdata;

import api.models.observations.ObservationCreateRequest;
import api.specs.RequestSpecs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static io.restassured.RestAssured.given;

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

    // TODO: Remove this method when encounter steps are available.
    public static String createEncounter(String patientUuid) {
        return given()
                .spec(RequestSpecs.withAdminBasicAuth())
                .body(Map.of(
                        "encounterDatetime", OBS_DATETIME.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        ),
                        "patient", patientUuid,
                        "encounterType", ReferenceTestData.encounterVitalsTypeUuid(),
                        "location", ReferenceTestData.locationUuid()
                ))
                .post("/encounter")
                .then()
                .statusCode(201)
                .extract()
                .path("uuid");
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
