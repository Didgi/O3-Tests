package api.requests.skelethon.requesters;

import api.models.BaseModel;
import api.requests.skelethon.EndpointRequests;
import api.requests.skelethon.HttpBaseRequest;
import api.requests.skelethon.interfaces.CrudRequestsInterface;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.UUID;

public class ValidatableCrudRequester<T extends BaseModel> extends HttpBaseRequest implements CrudRequestsInterface {
    private CrudRequester crudRequester;

    public ValidatableCrudRequester(RequestSpecification requestSpecification, EndpointRequests endpointRequests, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpointRequests, responseSpecification);
        crudRequester = new CrudRequester(requestSpecification, endpointRequests, responseSpecification);
    }

    @Override
    public T POST(BaseModel baseModel) {
        return (T) crudRequester.POST(baseModel)
                .assertThat()
                .extract()
                .as(endpointRequests.getResponseModel());
    }

    @Override
    public T GET() {
        return (T) crudRequester.GET()
                .assertThat()
                .extract()
                .as(endpointRequests.getResponseModel());
    }

    @Override
    public T PUT(BaseModel baseModel) {
        return (T) crudRequester.PUT(baseModel)
                .assertThat()
                .extract()
                .as(endpointRequests.getResponseModel());
    }

    @Override
    public T PATCH(BaseModel baseModel) {
        return (T) crudRequester.PATCH(baseModel)
                .assertThat()
                .extract()
                .as(endpointRequests.getResponseModel());
    }

    @Override
    public Object DELETE(UUID id) {
        return null;
    }

}
