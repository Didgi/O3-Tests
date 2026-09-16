package api.helpers;

import io.restassured.response.Response;

import static api.specs.ResponseSpecs.requestReturnsBadRequest;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

public final class EncounterErrorAssertions {

    private EncounterErrorAssertions() {
    }

    public static void assertBadRequestWithFieldError(Response response, String field, String errorCode) {
        response.then()
                .spec(requestReturnsBadRequest())
                .body("error.fieldErrors." + field + ".code", hasItem(errorCode));
    }

    public static void assertBadRequestWithRawMessage(Response response, String messageFragment) {
        response.then()
                .spec(requestReturnsBadRequest())
                .body("error.rawMessage", containsString(messageFragment));
    }
}
