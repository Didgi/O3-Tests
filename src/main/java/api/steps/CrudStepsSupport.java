package api.steps;

import api.requests.skelethon.endpoints.CrudOperations;
import api.requests.skelethon.interfaces.CrudEndpoint;
import api.requests.skelethon.requesters.RequesterFactory;
import api.requests.skelethon.requesters.SuccessfulCrudRequester;

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
