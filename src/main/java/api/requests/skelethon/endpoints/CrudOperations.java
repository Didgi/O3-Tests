package api.requests.skelethon.endpoints;

import java.util.Objects;

public record CrudOperations<RES>(
        EndpointSpec<RES> create,
        EndpointSpec<RES> get,
        EndpointSpec<RES> update,
        EndpointSpec<Void> delete
) {
    public CrudOperations {
        Objects.requireNonNull(create, "create endpoint must not be null");
        Objects.requireNonNull(get, "get endpoint must not be null");
        Objects.requireNonNull(update, "update endpoint must not be null");
        Objects.requireNonNull(delete, "delete endpoint must not be null");
    }
}
