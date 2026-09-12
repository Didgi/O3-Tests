package api.testdata;

import api.config.Config;
import api.specs.RequestSpecs;

import static io.restassured.RestAssured.given;

public class PatientTestData {
    private PatientTestData() {
    }

    public static String generateIdentifier() {
        return given()
                .spec(RequestSpecs.withAdminBasicAuth())
                .pathParam(
                        "sourceUuid",
                        Config.getProperty(
                                "patient_identifier_source_uuid"
                        )
                )
                // OpenMRS binds the request to SimpleObject and rejects an empty
                // body, even though the IDGen endpoint does not need any fields.
                .body("{}")
                .post(
                        "/idgen/identifiersource/"
                                + "{sourceUuid}/identifier"
                )
                .then()
                .statusCode(201)
                .extract()
                .path("identifier");
    }
}
