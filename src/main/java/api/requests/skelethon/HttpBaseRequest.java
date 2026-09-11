package api.requests.skelethon;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class HttpBaseRequest {
    protected RequestSpecification requestSpecification;
    protected EndpointRequests endpointRequests;
    protected ResponseSpecification responseSpecification;
}
