package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static org.apache.http.HttpStatus.*;

public class ResponseSpecs {
    private ResponseSpecs() {
    }

    public static ResponseSpecBuilder basicResponseSpec() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification requestReturnsOk() {
        return basicResponseSpec().expectStatusCode(SC_OK).build();
    }

    public static ResponseSpecification entityWasCreated() {
        return basicResponseSpec().expectStatusCode(SC_CREATED).build();
    }

    public static ResponseSpecification requestReturnsUnauthorized() {
        return basicResponseSpec().expectStatusCode(SC_UNAUTHORIZED).build();
    }

    public static ResponseSpecification requestReturnsForbidden() {
        return basicResponseSpec().expectStatusCode(SC_FORBIDDEN).build();
    }

    public static ResponseSpecification requestReturnsBadRequest() {
        return basicResponseSpec().expectStatusCode(SC_BAD_REQUEST).build();
    }

    public static ResponseSpecification requestReturnsNotFound() {
        return basicResponseSpec().expectStatusCode(SC_NOT_FOUND).build();
    }

    public static ResponseSpecification requestReturnsInternalServiceError() {
        return basicResponseSpec().expectStatusCode(SC_INTERNAL_SERVER_ERROR).build();
    }

}
