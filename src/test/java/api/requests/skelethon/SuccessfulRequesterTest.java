package api.requests.skelethon;

import api.models.auth.request.Credentials;
import api.models.auth.response.SessionResponse;
import api.requests.skelethon.endpoints.CrudOperations;
import api.requests.skelethon.endpoints.EndpointSpec;
import api.requests.skelethon.interfaces.*;
import api.requests.skelethon.query.QueryParams;
import api.requests.skelethon.requesters.*;
import io.restassured.builder.ResponseBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SuccessfulRequesterTest {

    @Test
    void successfulCrudDelegatesValidatesAndExtractsResponses() {
        CrudOperations<ItemResponse> operations = crudOperations();
        CrudEndpoint<ItemRequest, ItemRequest> raw = new StubCrudEndpoint();
        SuccessfulCrudRequester<ItemRequest, ItemRequest, ItemResponse> requester =
                new SuccessfulCrudRequester<>(raw, operations);

        assertThat(requester.create(new ItemRequest("create")).value()).isEqualTo("created");
        assertThat(requester.get("item-1", ReadOptions.full()).value()).isEqualTo("found");
        assertThat(requester.update("item-1", new ItemRequest("update")).value())
                .isEqualTo("updated");
        assertThat(requester.delete("item-1", DeleteMode.PURGE).statusCode()).isEqualTo(204);
    }

    @Test
    void successfulSearchAndNestedSearchExtractTypedCollections() {
        EndpointSpec<CollectionResponse> endpoint = collectionEndpoint();
        SearchEndpoint<QueryParams> rawSearch = params -> response(
                200,
                "{\"results\":[{\"value\":\"top-level\"}]}"
        );
        SuccessfulSearchRequester<QueryParams, CollectionResponse> search =
                new SuccessfulSearchRequester<>(rawSearch, endpoint);

        CollectionResponse topLevel = search.search(QueryParams.empty());
        assertThat(topLevel.results()).extracting(ItemResponse::value)
                .containsExactly("top-level");

        NestedSearchEndpoint<QueryParams> rawNestedSearch = (parentId, params) -> response(
                200,
                "{\"results\":[{\"value\":\"nested\"}]}"
        );
        SuccessfulNestedSearchRequester<QueryParams, CollectionResponse> nestedSearch =
                new SuccessfulNestedSearchRequester<>(rawNestedSearch, endpoint);

        CollectionResponse nested = nestedSearch.search("parent-1", QueryParams.empty());
        assertThat(nested.results()).extracting(ItemResponse::value).containsExactly("nested");
    }

    @Test
    void successfulNestedCrudDelegatesValidatesAndExtractsResponses() {
        CrudOperations<ItemResponse> operations = nestedCrudOperations();
        NestedCrudEndpoint<ItemRequest, ItemRequest> raw = new StubNestedCrudEndpoint();
        SuccessfulNestedCrudRequester<ItemRequest, ItemRequest, ItemResponse> requester =
                new SuccessfulNestedCrudRequester<>(raw, operations);

        assertThat(requester.create("parent-1", new ItemRequest("create")).value())
                .isEqualTo("created");
        assertThat(requester.get("parent-1", "item-1", ReadOptions.ref()).value())
                .isEqualTo("found");
        assertThat(requester.update("parent-1", "item-1", new ItemRequest("update")).value())
                .isEqualTo("updated");
        assertThat(requester.delete("parent-1", "item-1").statusCode()).isEqualTo(204);
    }

    @Test
    void successfulAuthChecksHttpStatusAndAuthenticatedFlag() {
        AuthEndpoint raw = new AuthEndpoint() {
            @Override
            public Response getSession(Credentials credentials) {
                return response(200, "{\"sessionId\":\"session-1\",\"authenticated\":true}");
            }

            @Override
            public Response logout() {
                return response(204, "");
            }
        };
        SuccessfulAuthRequester requester = new SuccessfulAuthRequester(raw, sessionEndpoint());

        SessionResponse session = requester.getSession(new Credentials("user", "password"));

        assertThat(session.authenticated()).isTrue();
        assertThat(session.sessionId()).isEqualTo("session-1");
        assertThat(requester.logout().statusCode()).isEqualTo(204);
    }

    @Test
    void successfulAuthRejectsHttpSuccessWithUnauthenticatedSession() {
        AuthEndpoint raw = new AuthEndpoint() {
            @Override
            public Response getSession(Credentials credentials) {
                return response(200, "{\"authenticated\":false}");
            }

            @Override
            public Response logout() {
                return response(204, "");
            }
        };
        SuccessfulAuthRequester requester = new SuccessfulAuthRequester(raw, sessionEndpoint());

        assertThrows(
                AssertionError.class,
                () -> requester.getSession(new Credentials("wrong", "credentials"))
        );
    }

    @Test
    void readOptionsRepresentOpenMrsValues() {
        assertThat(ReadOptions.defaults().isExplicit()).isFalse();
        assertThat(ReadOptions.ref().representation()).isEqualTo("ref");
        assertThat(ReadOptions.defaultRepresentation().representation()).isEqualTo("default");
        assertThat(ReadOptions.full().representation()).isEqualTo("full");
        assertThat(ReadOptions.custom("uuid,display").representation())
                .isEqualTo("custom:(uuid,display)");
    }

    @Test
    void successfulRequesterRejectsUnexpectedStatusWhileRawResponseRemainsAvailable() {
        Response notFound = response(404, "{\"error\":\"not found\"}");
        CrudEndpoint<ItemRequest, ItemRequest> raw = new StubCrudEndpoint() {
            @Override
            public Response get(String id, ReadOptions options) {
                return notFound;
            }
        };
        SuccessfulCrudRequester<ItemRequest, ItemRequest, ItemResponse> successful =
                new SuccessfulCrudRequester<>(raw, crudOperations());

        assertThat(raw.get("missing").statusCode()).isEqualTo(404);
        assertThrows(AssertionError.class, () -> successful.get("missing"));
    }

    private CrudOperations<ItemResponse> crudOperations() {
        return new CrudOperations<>(
                itemEndpoint("/items"),
                itemEndpoint("/items/{id}"),
                itemEndpoint("/items/{id}"),
                voidEndpoint("/items/{id}")
        );
    }

    private CrudOperations<ItemResponse> nestedCrudOperations() {
        return new CrudOperations<>(
                itemEndpoint("/parents/{parentId}/items"),
                itemEndpoint("/parents/{parentId}/items/{id}"),
                itemEndpoint("/parents/{parentId}/items/{id}"),
                voidEndpoint("/parents/{parentId}/items/{id}")
        );
    }

    private EndpointSpec<ItemResponse> itemEndpoint(String path) {
        return new EndpointSpec<>(path, new TypeRef<ItemResponse>() {
        });
    }

    private EndpointSpec<CollectionResponse> collectionEndpoint() {
        return new EndpointSpec<>("/items", new TypeRef<CollectionResponse>() {
        });
    }

    private EndpointSpec<SessionResponse> sessionEndpoint() {
        return new EndpointSpec<>("/session", new TypeRef<SessionResponse>() {
        });
    }

    private EndpointSpec<Void> voidEndpoint(String path) {
        return new EndpointSpec<>(path, new TypeRef<Void>() {
        });
    }

    private Response response(int status, String body) {
        return new ResponseBuilder()
                .setStatusCode(status)
                .setContentType("application/json")
                .setBody(body)
                .build();
    }

    private record ItemRequest(String value) {
    }

    private record ItemResponse(String value) {
    }

    private record CollectionResponse(List<ItemResponse> results) {
    }

    private static class StubCrudEndpoint implements CrudEndpoint<ItemRequest, ItemRequest> {

        @Override
        public Response create(ItemRequest request) {
            return responseOf(201, "created");
        }

        @Override
        public Response get(String id, ReadOptions options) {
            return responseOf(200, "found");
        }

        @Override
        public Response update(String id, ItemRequest request) {
            return responseOf(201, "updated");
        }

        @Override
        public Response delete(String id, DeleteMode mode) {
            return new ResponseBuilder().setStatusCode(204).setBody("").build();
        }
    }

    private static class StubNestedCrudEndpoint
            implements NestedCrudEndpoint<ItemRequest, ItemRequest> {

        @Override
        public Response create(String parentId, ItemRequest request) {
            return responseOf(201, "created");
        }

        @Override
        public Response get(String parentId, String id, ReadOptions options) {
            return responseOf(200, "found");
        }

        @Override
        public Response update(String parentId, String id, ItemRequest request) {
            return responseOf(201, "updated");
        }

        @Override
        public Response delete(String parentId, String id, DeleteMode mode) {
            return new ResponseBuilder().setStatusCode(204).setBody("").build();
        }
    }

    private static Response responseOf(int status, String value) {
        return new ResponseBuilder()
                .setStatusCode(status)
                .setContentType("application/json")
                .setBody("{\"value\":\"" + value + "\"}")
                .build();
    }
}
