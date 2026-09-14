# Примеры реализации API requester layer

Статус: иллюстративное приложение к реализованному requester layer; рабочая
композиция resource slice описана в
[`api-requester-resource-slice.md`](../guides/api-requester-resource-slice.md).
Операции и параметры сверяются с [инвентаризацией OpenMRS API](openmrs-api-inventory.md).

Типовые requester'ы уже реализованы в проекте. Код ниже показывает их контракты
и внутреннюю реализацию; package declarations и imports местами опущены. Для
добавления нового ресурса используйте пошаговый
[`resource-slice guide`](../guides/api-requester-resource-slice.md), а не
собирайте эту композицию вручную в тесте.

## 1. Общие объекты

### 1.1. EndpointSpec

```java
public record EndpointSpec<RES>(
        String pathTemplate,
        TypeRef<RES> responseTypeRef
) {
}
```

### 1.2. ReadOptions

```java
public record ReadOptions(String representation) {

    public ReadOptions {
        if (representation != null && representation.isBlank()) {
            throw new IllegalArgumentException("representation must not be blank");
        }
    }

    public static ReadOptions defaults() {
        return new ReadOptions(null);
    }

    public static ReadOptions ref() {
        return new ReadOptions("ref");
    }

    public static ReadOptions defaultRepresentation() {
        return new ReadOptions("default");
    }

    public static ReadOptions full() {
        return new ReadOptions("full");
    }

    public static ReadOptions custom(String fields) {
        return new ReadOptions("custom:(" + fields + ")");
    }

    public boolean isExplicit() {
        return representation != null;
    }
}
```

### 1.3. DeleteMode

```java
public enum DeleteMode {
    DEFAULT,
    PURGE
}
```

### 1.4. QueryParams

```java
@FunctionalInterface
public interface QueryParams {
    Map<String, ?> asMap();

    static QueryParams empty() {
        return Map::of;
    }

    static QueryParams of(Map<String, ?> parameters) {
        Objects.requireNonNull(parameters, "parameters must not be null");
        Map<String, ?> copy = Map.copyOf(parameters);
        return () -> copy;
    }
}
```

Для list достаточно `QueryParams.empty()`. Resource-specific params-модель создается только при реализации реальных фильтров и возвращает только заданные query parameters.

## 2. Observation CRUD

### 2.1. Контракт и группа операций

```java
public interface CrudEndpoint<CREATE, UPDATE> {

    Response create(CREATE request);

    Response get(String id, ReadOptions options);

    Response update(String id, UPDATE request);

    Response delete(String id, DeleteMode mode);

    default Response get(String id) {
        return get(id, ReadOptions.defaults());
    }

    default Response delete(String id) {
        return delete(id, DeleteMode.DEFAULT);
    }
}
```

```java
public record CrudOperations<RES>(
        EndpointSpec<RES> create,
        EndpointSpec<RES> get,
        EndpointSpec<RES> update,
        EndpointSpec<Void> delete
) {
}
```

```java
public final class ObservationEndpoints {

    public static final EndpointSpec<ObservationResponse> CREATE =
            new EndpointSpec<>("/obs", new TypeRef<>() {});

    public static final EndpointSpec<ObservationResponse> READ =
            new EndpointSpec<>("/obs/{id}", new TypeRef<>() {});

    public static final EndpointSpec<ObservationResponse> UPDATE =
            new EndpointSpec<>("/obs/{id}", new TypeRef<>() {});

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>("/obs/{id}", new TypeRef<>() {});

    public static final CrudOperations<ObservationResponse> CRUD =
            new CrudOperations<>(CREATE, READ, UPDATE, DELETE);

    private ObservationEndpoints() {
    }
}
```

### 2.2. Raw requester

```java
public class CrudRequester<CREATE, UPDATE>
        implements CrudEndpoint<CREATE, UPDATE> {

    private final RequestSpecification requestSpecification;
    private final CrudOperations<?> operations;

    public CrudRequester(
            RequestSpecification requestSpecification,
            CrudOperations<?> operations
    ) {
        this.requestSpecification = requestSpecification;
        this.operations = operations;
    }

    @Override
    public Response create(CREATE request) {
        return given()
                .spec(requestSpecification)
                .body(request)
                .post(operations.create().pathTemplate());
    }

    @Override
    public Response get(String id, ReadOptions options) {
        RequestSpecification request = given()
                .spec(requestSpecification)
                .pathParam("id", id);

        if (options.isExplicit()) {
            request.queryParam("v", options.representation());
        }

        return request.get(operations.get().pathTemplate());
    }

    @Override
    public Response update(String id, UPDATE requestBody) {
        return given()
                .spec(requestSpecification)
                .pathParam("id", id)
                .body(requestBody)
                .post(operations.update().pathTemplate());
    }

    @Override
    public Response delete(String id, DeleteMode mode) {
        RequestSpecification request = given()
                .spec(requestSpecification)
                .pathParam("id", id);

        if (mode == DeleteMode.PURGE) {
            request.queryParam("purge", true);
        }

        return request.delete(operations.delete().pathTemplate());
    }
}
```

Raw requester не вызывает `.then()` и не принимает `ResponseSpecification`.

### 2.3. Successful requester

```java
public class SuccessfulCrudRequester<CREATE, UPDATE, RES> {

    private final CrudEndpoint<CREATE, UPDATE> requester;
    private final CrudOperations<RES> operations;

    public SuccessfulCrudRequester(
            CrudEndpoint<CREATE, UPDATE> requester,
            CrudOperations<RES> operations
    ) {
        this.requester = requester;
        this.operations = operations;
    }

    public RES create(CREATE request) {
        return requester.create(request)
                .then()
                .statusCode(201)
                .extract()
                .as(operations.create().responseTypeRef());
    }

    public RES get(String id, ReadOptions options) {
        return requester.get(id, options)
                .then()
                .statusCode(200)
                .extract()
                .as(operations.get().responseTypeRef());
    }

    public RES get(String id) {
        return get(id, ReadOptions.defaults());
    }

    public RES update(String id, UPDATE request) {
        return requester.update(id, request)
                .then()
                .statusCode(201)
                .extract()
                .as(operations.update().responseTypeRef());
    }

    public Response delete(String id, DeleteMode mode) {
        Response response = requester.delete(id, mode);
        response.then().statusCode(204);
        return response;
    }
}
```

Пример конфигурации:

```java
CrudEndpoint<ObservationCreateRequest, ObservationUpdateRequest> rawObservation =
        new CrudRequester<>(authorizedRequestSpec, ObservationEndpoints.CRUD);

SuccessfulCrudRequester<ObservationCreateRequest, ObservationUpdateRequest, ObservationResponse> observation =
        new SuccessfulCrudRequester<>(rawObservation, ObservationEndpoints.CRUD);
```

## 3. Observation Search

### 3.1. List без фильтров

List — это вызов того же collection GET с пустыми query parameters:

```java
obsSearch.search(QueryParams.empty());
```

Результирующий запрос: `GET /obs`.

### 3.2. Params-модель для поиска

Следующий пример показывает, как расширить params-модель Observation при появлении
реальных фильтров. В текущем коде `ObservationSearchParams` пока пустая и
используется для list без фильтров.

```java
public record ObsSearchParams(
        String patient,
        String concept
) implements QueryParams {

    public static ObsSearchParams all() {
        return new ObsSearchParams(null, null);
    }

    public static ObsSearchParams byPatient(String patientId) {
        return new ObsSearchParams(patientId, null);
    }

    public static ObsSearchParams byConcept(String conceptId) {
        return new ObsSearchParams(null, conceptId);
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> params = new HashMap<>();

        putIfNotNull(params, "patient", patient);
        putIfNotNull(params, "concept", concept);

        return params;
    }

    private static void putIfNotNull(
            Map<String, Object> params,
            String name,
            Object value
    ) {
        if (value != null) {
            params.put(name, value);
        }
    }
}
```

Pagination и representation добавляются в модель тогда, когда они понадобятся конкретным тестам.

### 3.3. Тройка Search

```java
public interface SearchEndpoint<PARAMS extends QueryParams> {
    Response search(PARAMS params);
}
```

```java
public class SearchRequester<PARAMS extends QueryParams>
        implements SearchEndpoint<PARAMS> {

    private final RequestSpecification requestSpecification;
    private final EndpointSpec<?> endpoint;

    public SearchRequester(
            RequestSpecification requestSpecification,
            EndpointSpec<?> endpoint
    ) {
        this.requestSpecification = requestSpecification;
        this.endpoint = endpoint;
    }

    @Override
    public Response search(PARAMS params) {
        return given()
                .spec(requestSpecification)
                .queryParams(params.asMap())
                .get(endpoint.pathTemplate());
    }
}
```

```java
public class SuccessfulSearchRequester<
        PARAMS extends QueryParams,
        RES
        > {

    private final SearchEndpoint<PARAMS> requester;
    private final EndpointSpec<RES> endpoint;

    public SuccessfulSearchRequester(
            SearchEndpoint<PARAMS> requester,
            EndpointSpec<RES> endpoint
    ) {
        this.requester = requester;
        this.endpoint = endpoint;
    }

    public RES search(PARAMS params) {
        return requester.search(params)
                .then()
                .statusCode(200)
                .extract()
                .as(endpoint.responseTypeRef());
    }
}
```

Один requester выполняет и list, и поиск:

```java
obsSearch.search(ObsSearchParams.all());

obsSearch.search(ObsSearchParams.byPatient(patientId));
```

Если resource-specific params-модели еще нет, requester можно параметризовать общим `QueryParams` и использовать `QueryParams.empty()` для list.

## 4. Auth

### 4.1. Контракт и raw requester

```java
public record Credentials(String username, String password) {
}
```

```java
public interface AuthEndpoint {
    Response getSession(Credentials credentials);
    Response logout();
}
```

```java
public class AuthRequester implements AuthEndpoint {

    private final RequestSpecification requestSpecification;
    private final EndpointSpec<SessionResponse> sessionEndpoint;

    public AuthRequester(
            RequestSpecification requestSpecification,
            EndpointSpec<SessionResponse> sessionEndpoint
    ) {
        this.requestSpecification = requestSpecification;
        this.sessionEndpoint = sessionEndpoint;
    }

    @Override
    public Response getSession(Credentials credentials) {
        return given()
                .spec(requestSpecification)
                .auth()
                .preemptive()
                .basic(credentials.username(), credentials.password())
                .get(sessionEndpoint.pathTemplate());
    }

    @Override
    public Response logout() {
        return given()
                .spec(requestSpecification)
                .delete(sessionEndpoint.pathTemplate());
    }
}
```

Для `logout` requester конфигурируется `RequestSpecification` с текущей Basic auth или session cookie.

### 4.2. Successful requester

```java
public class SuccessfulAuthRequester {

    private final AuthEndpoint requester;
    private final EndpointSpec<SessionResponse> sessionEndpoint;

    public SuccessfulAuthRequester(
            AuthEndpoint requester,
            EndpointSpec<SessionResponse> sessionEndpoint
    ) {
        this.requester = requester;
        this.sessionEndpoint = sessionEndpoint;
    }

    public SessionResponse getSession(Credentials credentials) {
        return requester.getSession(credentials)
                .then()
                .statusCode(200)
                .body("authenticated", equalTo(true))
                .extract()
                .as(sessionEndpoint.responseTypeRef());
    }

    public Response logout() {
        Response response = requester.logout();
        response.then().statusCode(204);
        return response;
    }
}
```

Status logout и способ передачи session context должны быть подтверждены на текущем `qa` deployment.

## 5. EncounterProvider Nested CRUD и Search

### 5.1. Контракты

```java
public interface NestedCrudEndpoint<CREATE, UPDATE> {
    Response create(String parentId, CREATE request);
    Response get(String parentId, String id, ReadOptions options);
    Response update(String parentId, String id, UPDATE request);
    Response delete(String parentId, String id, DeleteMode mode);
}
```

```java
public interface NestedSearchEndpoint<PARAMS extends QueryParams> {
    Response search(String parentId, PARAMS params);
}
```

### 5.2. Спецификации

Для nested CRUD используется тот же `CrudOperations<RES>`, что и для обычного
CRUD. Отличие nested-контракта находится в сигнатурах операций requester’а:
они дополнительно получают `parentId`.

```java
public final class EncounterProviderEndpoints {

    public static final EndpointSpec<EncounterProviderResponse> CREATE =
            new EndpointSpec<>(
                    "/encounter/{parentId}/encounterprovider",
                    new TypeRef<>() {}
            );

    public static final EndpointSpec<EncounterProviderResponse> READ =
            new EndpointSpec<>(
                    "/encounter/{parentId}/encounterprovider/{id}",
                    new TypeRef<>() {}
            );

    public static final EndpointSpec<EncounterProviderResponse> UPDATE =
            new EndpointSpec<>(
                    "/encounter/{parentId}/encounterprovider/{id}",
                    new TypeRef<>() {}
            );

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>(
                    "/encounter/{parentId}/encounterprovider/{id}",
                    new TypeRef<>() {}
            );

    public static final EndpointSpec<EncounterProviderSearchResponse> SEARCH =
            new EndpointSpec<>(
                    "/encounter/{parentId}/encounterprovider",
                    new TypeRef<>() {}
            );

    public static final CrudOperations<EncounterProviderResponse> CRUD =
            new CrudOperations<>(CREATE, READ, UPDATE, DELETE);

    private EncounterProviderEndpoints() {
    }
}
```

### 5.3. Raw requesters

Nested CRUD отличается от обычного CRUD контрактом обязательных parent и item IDs:

```java
public class NestedCrudRequester<CREATE, UPDATE>
        implements NestedCrudEndpoint<CREATE, UPDATE> {

    private final RequestSpecification requestSpecification;
    private final CrudOperations<?> operations;

    public NestedCrudRequester(
            RequestSpecification requestSpecification,
            CrudOperations<?> operations
    ) {
        this.requestSpecification = requestSpecification;
        this.operations = operations;
    }

    @Override
    public Response create(String parentId, CREATE request) {
        return given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId)
                .body(request)
                .post(operations.create().pathTemplate());
    }

    @Override
    public Response get(String parentId, String id, ReadOptions options) {
        RequestSpecification request = given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId)
                .pathParam("id", id);

        if (options.isExplicit()) {
            request.queryParam("v", options.representation());
        }

        return request.get(operations.get().pathTemplate());
    }

    @Override
    public Response update(String parentId, String id, UPDATE request) {
        return given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId)
                .pathParam("id", id)
                .body(request)
                .post(operations.update().pathTemplate());
    }

    @Override
    public Response delete(String parentId, String id, DeleteMode mode) {
        RequestSpecification request = given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId)
                .pathParam("id", id);

        if (mode == DeleteMode.PURGE) {
            request.queryParam("purge", true);
        }

        return request.delete(operations.delete().pathTemplate());
    }
}
```

Nested Search подставляет только parent ID и query parameters:

```java
public class NestedSearchRequester<PARAMS extends QueryParams>
        implements NestedSearchEndpoint<PARAMS> {

    private final RequestSpecification requestSpecification;
    private final EndpointSpec<?> endpoint;

    public NestedSearchRequester(
            RequestSpecification requestSpecification,
            EndpointSpec<?> endpoint
    ) {
        this.requestSpecification = requestSpecification;
        this.endpoint = endpoint;
    }

    @Override
    public Response search(String parentId, PARAMS params) {
        return given()
                .spec(requestSpecification)
                .pathParam("parentId", parentId)
                .queryParams(params.asMap())
                .get(endpoint.pathTemplate());
    }
}
```

### 5.4. Successful requesters

```java
public class SuccessfulNestedCrudRequester<CREATE, UPDATE, RES> {

    private final NestedCrudEndpoint<CREATE, UPDATE> requester;
    private final CrudOperations<RES> operations;

    public SuccessfulNestedCrudRequester(
            NestedCrudEndpoint<CREATE, UPDATE> requester,
            CrudOperations<RES> operations
    ) {
        this.requester = requester;
        this.operations = operations;
    }

    public RES create(String parentId, CREATE request) {
        return requester.create(parentId, request)
                .then()
                .statusCode(201)
                .extract()
                .as(operations.create().responseTypeRef());
    }

    public RES get(String parentId, String id, ReadOptions options) {
        return requester.get(parentId, id, options)
                .then()
                .statusCode(200)
                .extract()
                .as(operations.get().responseTypeRef());
    }

    public RES update(String parentId, String id, UPDATE request) {
        return requester.update(parentId, id, request)
                .then()
                .statusCode(201)
                .extract()
                .as(operations.update().responseTypeRef());
    }

    public Response delete(String parentId, String id, DeleteMode mode) {
        Response response = requester.delete(parentId, id, mode);
        response.then().statusCode(204);
        return response;
    }
}
```

```java
public class SuccessfulNestedSearchRequester<
        PARAMS extends QueryParams,
        RES
        > {

    private final NestedSearchEndpoint<PARAMS> requester;
    private final EndpointSpec<RES> endpoint;

    public SuccessfulNestedSearchRequester(
            NestedSearchEndpoint<PARAMS> requester,
            EndpointSpec<RES> endpoint
    ) {
        this.requester = requester;
        this.endpoint = endpoint;
    }

    public RES search(String parentId, PARAMS params) {
        return requester.search(parentId, params)
                .then()
                .statusCode(200)
                .extract()
                .as(endpoint.responseTypeRef());
    }
}
```

## 6. IDGen POST для подготовки Patient test data

IDGen сейчас не является test target и не образует отдельный тип endpoint'а. Для
единственного служебного вызова достаточно прямого `POST`. UUID источника
`OpenMRS ID` хранится в конфигурации окружения и читается через
`ReferenceTestData`:

```properties
patient_identifier_source_uuid=8549f706-7e85-4c1d-9424-217d50a2988b
```

```java
public final class PatientTestData {

    private PatientTestData() {
    }

    public static String generateIdentifier() {
        return given()
                .spec(RequestSpecs.withAdminBasicAuth())
                .pathParam(
                        "sourceUuid",
                        ReferenceTestData.patientIdentifierSourceUuid()
                )
                .body("{}")
                .post("/idgen/identifiersource/{sourceUuid}/identifier")
                .then()
                .statusCode(201)
                .extract()
                .path("identifier");
    }
}
```

Запрос содержит пустой JSON-объект `{}`, потому что OpenMRS требует request body для этого `POST`. Отдельный requester-слой и response DTO здесь не дают переиспользования и поэтому не создаются.

## 7. Позитивный и негативный тесты

Позитивный сценарий использует successful requester:

```java
@Test
void getsExistingObservationInFullRepresentation() {
    ObservationResponse observation = successfulObservation.get(
            existingObsId,
            ReadOptions.full()
    );

    assertThat(observation.uuid()).isEqualTo(existingObsId);
}
```

Негативный сценарий вызывает raw requester и сам задает ожидаемую ошибку:

```java
@Test
void returnsNotFoundForUnknownObservation() {
    Response response = rawObservation.get(
            unknownObsId,
            ReadOptions.defaults()
    );

    response.then()
            .statusCode(404);
}
```

## 8. Композиция `ApiClient` и resource Steps

Текущий прикладной вход — `ApiClient`. Он создает общую авторизованную
`RequestSpecification`, передает ее в `RequesterFactory` и публикует ресурсные
Steps:

```java
public final class ApiClient {

    private final UserSteps users;
    private final ObservationSteps observations;

    private ApiClient(RequestSpecification specification) {
        RequesterFactory requesters =
                new RequesterFactory(specification);

        users = new UserSteps(requesters);
        observations = new ObservationSteps(requesters);
    }

    public static ApiClient admin() {
        return new ApiClient(RequestSpecs.withAdminBasicAuth());
    }

    public UserSteps users() {
        return users;
    }

    public ObservationSteps observations() {
        return observations;
    }
}
```

Каждый resource Steps хранит типизированные successful requester’ы и выставляет
предметные методы (`searchUsers`, `createObservation`, `getObservation`). Новый
ресурс добавляется в `ApiClient` полем, инициализацией через тот же
`RequesterFactory` и accessor’ом. Сам `ApiClient` не формирует HTTP-запросы, а
Steps не содержит assertions.

## 9. Как позднее разделить response-типы

Пока create/get/update возвращают один `RES`:

```java
SuccessfulCrudRequester<CREATE, UPDATE, RES>
```

Если один ресурс окажется исключением, для него добавляется предметная обертка, а общий requester не меняется:

```java
public class SuccessfulSpecialResourceRequester {
    private final CrudEndpoint<CreateRequest, UpdateRequest> requester;

    public SuccessfulSpecialResourceRequester(
            CrudEndpoint<CreateRequest, UpdateRequest> requester
    ) {
        this.requester = requester;
    }

    public CreateResponse create(CreateRequest request) {
        return requester.create(request)
                .then()
                .statusCode(201)
                .extract()
                .as(CreateResponse.class);
    }

    public GetResponse get(String id, ReadOptions options) {
        return requester.get(id, options)
                .then()
                .statusCode(200)
                .extract()
                .as(GetResponse.class);
    }

    public UpdateResponse update(String id, UpdateRequest request) {
        return requester.update(id, request)
                .then()
                .statusCode(201)
                .extract()
                .as(UpdateResponse.class);
    }
}
```

Если разные response-типы повторятся у нескольких ресурсов, вводится обобщенная версия:

```java
public class SuccessfulTypedCrudRequester<
        CREATE,
        UPDATE,
        CREATE_RES,
        GET_RES,
        UPDATE_RES
        > {
    public CREATE_RES create(CREATE request);
    public GET_RES get(String id, ReadOptions options);
    public UPDATE_RES update(String id, UPDATE request);
    public Response delete(String id, DeleteMode mode);
}
```

Существующий `CrudEndpoint<CREATE, UPDATE>` и его raw implementation сохраняются, потому что они всегда возвращают `Response`. `DELETE_RES` не добавляется: успешный DELETE проверяет пустой `204` response.

## 10. Структура пакетов

Ниже показано фактическое расположение requester layer и уже добавленных
прикладных слоев. Каталоги ресурсов, которые еще не реализованы, создаются по
мере появления соответствующего resource slice.

```text
api/
├── config/
├── models/
│   ├── auth/
│   ├── encounter/
│   ├── observations/
│   ├── patient/
│   ├── person/
│   ├── user/
│   └── visit/
├── specs/
├── requests/
│   ├── endpoints/
│   ├── skeleton/
│   │   ├── interfaces/
│   │   ├── options/
│   │   ├── query/
│   │   └── requesters/
│   └── steps/
├── testdata/
└── utils/
```

Пакеты создаются по мере реализации vertical slices; структура не требует заранее создавать пустые классы для каждого ресурса.
