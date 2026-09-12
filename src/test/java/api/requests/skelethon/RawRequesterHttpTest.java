package api.requests.skelethon;

import api.models.auth.request.Credentials;
import api.requests.skelethon.endpoints.CrudOperations;
import api.requests.skelethon.endpoints.EndpointSpec;
import api.requests.skelethon.interfaces.*;
import api.requests.skelethon.query.QueryParams;
import api.requests.skelethon.requesters.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RawRequesterHttpTest {

    private final BlockingQueue<CapturedRequest> requests = new LinkedBlockingQueue<>();
    private HttpServer server;
    private RequestSpecification requestSpecification;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", this::handle);
        server.start();

        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://localhost:" + server.getAddress().getPort())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void crudRequesterBuildsAllRequestsAndKeepsNegativeResponseRaw() {
        CrudOperations<ItemResponse> operations = crudOperations("/items");
        CrudEndpoint<ItemRequest, ItemRequest> requester =
                new CrudRequester<>(requestSpecification, operations);

        requester.create(new ItemRequest("created"));
        assertRequest("POST", "/items", null, "created");

        requester.get("item-1", ReadOptions.full());
        assertRequest("GET", "/items/item-1", "v=full", "");

        requester.get("item-2");
        assertRequest("GET", "/items/item-2", null, "");

        requester.update("item-3", new ItemRequest("updated"));
        assertRequest("POST", "/items/item-3", null, "updated");

        requester.delete("item-4", DeleteMode.PURGE);
        assertRequest("DELETE", "/items/item-4", "purge=true", "");

        Response negativeResponse = requester.get("missing");
        assertThat(negativeResponse.statusCode()).isEqualTo(404);
        assertRequest("GET", "/items/missing", null, "");
    }

    @Test
    void searchRequesterUsesEmptyParamsForListAndFiltersForSearch() {
        EndpointSpec<ItemResponse> endpoint = endpoint("/items");
        SearchEndpoint<QueryParams> requester =
                new SearchRequester<>(requestSpecification, endpoint);

        requester.search(QueryParams.empty());
        assertRequest("GET", "/items", null, "");

        requester.search(QueryParams.of(Map.of("patient", "patient-1", "limit", 10)));
        CapturedRequest search = takeRequest();
        assertThat(search.method()).isEqualTo("GET");
        assertThat(search.path()).isEqualTo("/items");
        assertThat(search.query()).contains("patient=patient-1", "limit=10");
    }

    @Test
    void nestedRequestersPassParentIdItemIdRepresentationAndDeleteMode() {
        CrudOperations<ItemResponse> operations = nestedCrudOperations();
        NestedCrudEndpoint<ItemRequest, ItemRequest> crud =
                new NestedCrudRequester<>(requestSpecification, operations);

        crud.get("parent-1", "child-1", ReadOptions.ref());
        assertRequest(
                "GET",
                "/parents/parent-1/children/child-1",
                "v=ref",
                ""
        );

        crud.delete("parent-1", "child-1", DeleteMode.PURGE);
        assertRequest(
                "DELETE",
                "/parents/parent-1/children/child-1",
                "purge=true",
                ""
        );

        NestedSearchEndpoint<QueryParams> search = new NestedSearchRequester<>(
                requestSpecification,
                endpoint("/parents/{parentId}/children")
        );
        search.search("parent-2", QueryParams.of(Map.of("v", "full")));
        assertRequest("GET", "/parents/parent-2/children", "v=full", "");
    }

    @Test
    void authRequesterUsesItsOwnContract() {
        AuthEndpoint auth = new AuthRequester(requestSpecification, endpoint("/session"));

        auth.getSession(new Credentials("user", "pass"));
        CapturedRequest session = takeRequest();
        assertThat(session.method()).isEqualTo("GET");
        assertThat(session.path()).isEqualTo("/session");
        assertThat(session.authorization()).isEqualTo("Basic dXNlcjpwYXNz");

        auth.logout();
        assertRequest("DELETE", "/session", null, "");
    }

    private CrudOperations<ItemResponse> crudOperations(String collectionPath) {
        return new CrudOperations<>(
                endpoint(collectionPath),
                endpoint(collectionPath + "/{id}"),
                endpoint(collectionPath + "/{id}"),
                voidEndpoint(collectionPath + "/{id}")
        );
    }

    private CrudOperations<ItemResponse> nestedCrudOperations() {
        String collectionPath = "/parents/{parentId}/children";
        String itemPath = collectionPath + "/{id}";
        return new CrudOperations<>(
                endpoint(collectionPath),
                endpoint(itemPath),
                endpoint(itemPath),
                voidEndpoint(itemPath)
        );
    }

    private EndpointSpec<ItemResponse> endpoint(String path) {
        return new EndpointSpec<>(path, new TypeRef<ItemResponse>() {
        });
    }

    private EndpointSpec<Void> voidEndpoint(String path) {
        return new EndpointSpec<>(path, new TypeRef<Void>() {
        });
    }

    private void assertRequest(
            String method,
            String path,
            String query,
            String bodyPart
    ) {
        CapturedRequest request = takeRequest();
        assertThat(request.method()).isEqualTo(method);
        assertThat(request.path()).isEqualTo(path);
        assertThat(request.query()).isEqualTo(query);
        assertThat(request.body()).contains(bodyPart);
    }

    private CapturedRequest takeRequest() {
        try {
            CapturedRequest request = requests.poll(2, TimeUnit.SECONDS);
            assertThat(request).as("captured HTTP request").isNotNull();
            return request;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for HTTP request", exception);
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        requests.add(new CapturedRequest(
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                exchange.getRequestURI().getRawQuery(),
                body,
                exchange.getRequestHeaders().getFirst("Authorization")
        ));

        if (exchange.getRequestMethod().equals("DELETE")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        int status = exchange.getRequestURI().getPath().endsWith("/missing")
                ? 404
                : exchange.getRequestMethod().equals("POST") ? 201 : 200;
        String responseBody = "{\"value\":\"ok\",\"authenticated\":true}";
        byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private record ItemRequest(String value) {
    }

    private record ItemResponse(String value) {
    }

    private record CapturedRequest(
            String method,
            String path,
            String query,
            String body,
            String authorization
    ) {
    }
}
