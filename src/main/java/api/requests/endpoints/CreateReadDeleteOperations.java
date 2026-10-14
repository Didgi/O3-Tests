package api.requests.endpoints;

import java.util.Objects;

public record CreateReadDeleteOperations<RES>(
        EndpointSpec<RES> create,
        EndpointSpec<RES> get,
        EndpointSpec<Void> delete
) {
    public CreateReadDeleteOperations {
        Objects.requireNonNull(
                create,
                "create endpoint must not be null"
        );
        Objects.requireNonNull(
                get,
                "get endpoint must not be null"
        );
        Objects.requireNonNull(
                delete,
                "delete endpoint must not be null"
        );
    }
}
