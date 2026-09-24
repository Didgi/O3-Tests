package api.requests.steps;

import api.requests.endpoints.CrudOperations;
import api.requests.skeleton.interfaces.CrudEndpoint;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulCrudRequester;

abstract class CrudStepsSupport<C, U, R> {
    protected final SuccessfulCrudRequester<C, U, R> successfulCrud;
    protected final CrudEndpoint<C, U> rawCrud;

    protected CrudStepsSupport(
            RequesterFactory requester,
            CrudOperations<R> operations
    ) {
        successfulCrud = requester.successfulCrud(operations);
        rawCrud = requester.rawCrud(operations);
    }
}
