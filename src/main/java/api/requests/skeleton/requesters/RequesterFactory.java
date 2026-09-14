package api.requests.skeleton.requesters;

import api.requests.endpoints.CrudOperations;
import api.requests.endpoints.EndpointSpec;
import api.requests.skeleton.interfaces.CrudEndpoint;
import api.requests.skeleton.interfaces.SearchEndpoint;
import api.requests.skeleton.query.QueryParams;
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

    public <CREATE, UPDATE> CrudEndpoint<CREATE, UPDATE> rawCrud(
            CrudOperations<?> operations
    ) {
        return new CrudRequester<>(specification, operations);
    }
}
