package api.requests.skelethon.requesters;

import api.requests.skelethon.endpoints.CrudOperations;
import api.requests.skelethon.endpoints.EndpointSpec;
import api.requests.skelethon.interfaces.CrudEndpoint;
import api.requests.skelethon.interfaces.SearchEndpoint;
import api.requests.skelethon.query.QueryParams;
import io.restassured.specification.RequestSpecification;

public final class RequesterFactory {
    private final RequestSpecification specification;

    public RequesterFactory(RequestSpecification specification) {
        this.specification = specification;
    }

    public <PARAMS extends QueryParams, RESPONSE>
    SuccessfulSearchRequester <PARAMS, RESPONSE> successfulSearch(
            EndpointSpec<RESPONSE> endpoint
    ) {
        SearchEndpoint<PARAMS> rawRequester =
                new SearchRequester<>(
                        specification,
                        endpoint
                );

        return new SuccessfulSearchRequester<>(
                rawRequester,
                endpoint
        );
    }

    public <CREATE, UPDATE, RESPONSE>
    SuccessfulCrudRequester<CREATE, UPDATE, RESPONSE> successfulCrud(
            CrudOperations<RESPONSE> operations
    ) {
        CrudEndpoint<CREATE, UPDATE> rawRequester =
                new CrudRequester<>(
                        specification,
                        operations
                );

        return new SuccessfulCrudRequester<>(rawRequester, operations);
    }

    public <PARAMS extends QueryParams, RESPONSE>
    SuccessfulNestedSearchRequester<PARAMS, RESPONSE> successfulNestedSearch(
            EndpointSpec<RESPONSE> endpoint
    ) {
        NestedSearchRequester<PARAMS> rawRequester = new NestedSearchRequester<>(
                specification,
                endpoint
        );

        return new SuccessfulNestedSearchRequester<>(rawRequester, endpoint);
    }

    public <CREATE, UPDATE, RESPONSE>
    SuccessfulNestedCrudRequester<CREATE, UPDATE, RESPONSE> successfulNestedCrud(
            CrudOperations<RESPONSE> operations
    ) {
        NestedCrudRequester<CREATE, UPDATE> rawRequester = new NestedCrudRequester<>(
                specification,
                operations
        );

        return new SuccessfulNestedCrudRequester<>(
                rawRequester,
                operations
        );
    }
}
