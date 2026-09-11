package api.requests.skelethon.requesters;

import api.config.Config;
import api.models.BaseModel;
import api.requests.skelethon.EndpointRequests;
import api.requests.skelethon.HttpBaseRequest;
import api.requests.skelethon.interfaces.CrudRequestsInterface;
import common.StepLogger;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpBaseRequest implements CrudRequestsInterface {
    public CrudRequester(RequestSpecification requestSpecification, EndpointRequests endpointRequests, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpointRequests, responseSpecification);
    }

    @Override
    @Step("POST запрос")
    public ValidatableResponse POST(BaseModel baseModel) {
        if (baseModel != null && !endpointRequests.getRequestModel().isInstance(baseModel)) {
            throw new RuntimeException("Ожидаемая и переданные модели различаются. " +
                    "Ожидалась модель: " + endpointRequests.getRequestModel().getSimpleName() +
                    ", но передана модель: " + baseModel.toString());
        }
        return StepLogger.log("Тело запроса/ответа по ручке: " + endpointRequests.getPath(), () -> {
            var body = baseModel != null ? baseModel : "";
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(Config.getProperty("api_version") + endpointRequests.getPath())
                    .then()
                    .spec(responseSpecification);
        });
    }

    @Override
    @Step("GET запрос")
    public ValidatableResponse GET() {
        return StepLogger.log("Тело запроса/ответа по ручке: " + endpointRequests.getPath(), () -> {
            return given().spec(requestSpecification)
                    .get(Config.getProperty("api_version") + endpointRequests.getPath())
                    .then()
                    .spec(responseSpecification);
        });
    }

    @Step("GET запрос")
    public ValidatableResponse GET(String patientName) {
        return StepLogger.log("Тело запроса/ответа по ручке: " + endpointRequests.getPath(), () -> {
            return given().spec(requestSpecification)
                    .urlEncodingEnabled(false)
                    .queryParam("q", patientName)
                    .queryParam("v", "default")
                    .get(Config.getProperty("api_version") + endpointRequests.getPath())
                    .then()
                    .spec(responseSpecification);
        });
    }

    @Override
    @Step("PUT запрос")
    public ValidatableResponse PUT(BaseModel baseModel) {
        if (baseModel != null && !endpointRequests.getRequestModel().isInstance(baseModel)) {
            throw new RuntimeException("Ожидаемая и переданные модели различаются. " +
                    "Ожидалась модель: " + endpointRequests.getRequestModel().getSimpleName() +
                    ", но передана модель: " + baseModel.toString());
        }
        return StepLogger.log("Тело запроса/ответа по ручке: " + endpointRequests.getPath(), () -> {
            var body = baseModel != null ? baseModel : "";
            return given().spec(requestSpecification)
                    .body(body)
                    .put(Config.getProperty("api_version") + endpointRequests.getPath())
                    .then()
                    .spec(responseSpecification);
        });
    }

    @Override
    @Step("PATCH запрос")
    public ValidatableResponse PATCH(BaseModel baseModel) {
        if (baseModel != null && !endpointRequests.getRequestModel().isInstance(baseModel)) {
            throw new RuntimeException("Ожидаемая и переданные модели различаются. " +
                    "Ожидалась модель: " + endpointRequests.getRequestModel().getSimpleName() +
                    ", но передана модель: " + baseModel.toString());
        }
        return StepLogger.log("Тело запроса/ответа по ручке: " + endpointRequests.getPath(), () -> {
            var body = baseModel != null ? baseModel : "";
            return given().spec(requestSpecification)
                    .body(body)
                    .patch(Config.getProperty("api_version") + endpointRequests.getPath())
                    .then()
                    .spec(responseSpecification);
        });
    }

    @Override
    @Step("DELETE запрос по UUID: {UUID}")
    public ValidatableResponse DELETE(UUID id) {
        return StepLogger.log("Тело запроса/ответа по ручке: " + endpointRequests.getPath(), () -> {
            return given().spec(requestSpecification)
                    .delete(Config.getProperty("api_version") + endpointRequests.getPath() + id)
                    .then()
                    .spec(responseSpecification);
        });
    }
}
