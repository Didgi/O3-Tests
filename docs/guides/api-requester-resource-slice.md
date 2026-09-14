# Как добавить новый ресурс в API requester layer

Этот гайд нужен, когда для ресурса еще нет готовых `Endpoints` и `Steps`. Он
описывает минимальный resource slice: модели, endpoint specs, Steps, регистрацию
в `ApiClient` и тест.

## Как устроен путь запроса

```text
Тест
  → ApiClient
    → ProductSteps
      → SuccessfulRequester
        → raw Requester
          → API
```

Каждый слой отвечает только за свою часть:

| Слой | Что в нем описывается |
|---|---|
| `api.models.<resource>` | request, response и query params модели |
| `*Endpoints` | пути endpoint’ов и типы успешных response |
| [`RequesterFactory`](../../src/main/java/api/requests/skeleton/requesters/RequesterFactory.java) | сборка raw requester и successful-обертки; для CRUD также дает raw-доступ |
| `*Steps` | понятные предметные операции ресурса |
| [`CrudStepsSupport`](../../src/main/java/api/requests/steps/CrudStepsSupport.java) | необязательная общая инициализация successful и raw CRUD для Steps |
| `ApiClient` | авторизация и доступ к Steps |
| `ReferenceTestData` | доступ к идентификаторам заранее созданных reference objects |
| `*TestData` | создание уникальных или временных данных для конкретного теста |
| тест | проверка бизнес-результата |

Позитивный тест обычно работает только с `ApiClient` и `Steps`. Детали
`RequesterFactory` нужны при создании нового resource slice, но не должны
появляться в каждом тесте.

В текущем дереве проекта эти роли находятся в следующих пакетах:

| Пакет | Содержимое |
|---|---|
| `api.requests.endpoints` | `EndpointSpec`, `CrudOperations` и ресурсные `*Endpoints` |
| `api.requests.skeleton.interfaces` | контракты `AuthEndpoint`, `CrudEndpoint`, `SearchEndpoint` и nested-варианты |
| `api.requests.skeleton.options` | `ReadOptions`, `DeleteMode` |
| `api.requests.skeleton.query` | `QueryParams` и его реализации в моделях ресурсов |
| `api.requests.skeleton.requesters` | raw/successful requester’ы и `RequesterFactory` |
| `api.requests.steps` | `ApiClient`, resource Steps и Steps-support классы |

## Шаг 1. Подготовьте модели

Создайте в `api.models.<resource>`:

- response DTO для ответа API;
- request DTO для create/update, если ресурс поддерживает запись;
- `*SearchParams`, если у collection endpoint есть query parameters.

Например, для наблюдений в проекте есть:

```text
src/main/java/api/models/observations/
├── ObservationCreateRequest.java
├── ObservationResponse.java
├── ObservationSearchParams.java
├── ObservationSearchResponse.java
└── ObservationUpdateRequest.java
```

Готовый resource slice наблюдений можно использовать как рабочий пример:
[`ObservationEndpoints.java`](../../src/main/java/api/requests/endpoints/ObservationEndpoints.java),
[`ObservationSteps.java`](../../src/main/java/api/requests/steps/ObservationSteps.java) и
[`ApiClient.java`](../../src/main/java/api/requests/steps/ApiClient.java).

Params-модель должна реализовывать `QueryParams`. Подробнее о сопоставлении
полей модели с query parameters написано в
[`api-requester-search-params.md`](api-requester-search-params.md).

Для list без фильтров достаточно пустой модели:

```java
public record ObservationSearchParams() implements QueryParams {

    @Override
    public Map<String, ?> asMap() {
        return Map.of();
    }
}
```

## Seed/reference data

### Что это такое

Seed/reference data — это заранее созданные на тестовом стенде объекты, которые
нужны как входные данные, но сами не являются целью текущего теста. Например,
тесту наблюдения может понадобиться уже существующий concept или location.

Такие данные не нужно создавать заново в каждом тесте и не нужно зашивать их
UUID непосредственно в тестовый код. В репозитории хранятся только их значения
или идентификаторы, необходимые для обращения к ним.

### Где брать значения

Ключи находятся в
[`src/main/resources/config.properties`](../../src/main/resources/config.properties):

```properties
patient_identifier_source_uuid=8549f706-7e85-4c1d-9424-217d50a2988b
test_location_uuid=
concept_id=5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
```

В коде обращайтесь к ним через
[`ReferenceTestData.java`](../../src/main/java/api/testdata/ReferenceTestData.java):

```java
String sourceUuid = ReferenceTestData.patientIdentifierSourceUuid();
String locationUuid = ReferenceTestData.locationUuid();
String conceptId = ReferenceTestData.conceptId();
```

Методы `ReferenceTestData` скрывают имена configuration keys. Поэтому тесты и
helpers не должны напрямую вызывать `Config.getProperty(...)` для reference data.

Назначение текущих ключей:

| Key | Что означает | Getter |
|---|---|---|
| `patient_identifier_source_uuid` | UUID IdentifierSource `OpenMRS ID`, используемый для генерации patient identifier | `patientIdentifierSourceUuid()` |
| `test_location_uuid` | UUID заранее созданной location | `locationUuid()` |
| `concept_id` | ID заранее созданного concept | `conceptId()` |

Пустое значение `test_location_uuid` означает, что location reference пока не
настроен в этом окружении. Не используйте его в тесте, пока значение не будет
задано для целевого стенда.

`Config` сначала проверяет system property, затем переменную окружения и только
после этого значение из `config.properties`. Поэтому стендоспецифичные значения
можно передать без изменения репозитория, например:

```bash
./mvnw \
  -Dtest_location_uuid=YOUR_LOCATION_UUID \
  -Dconcept_id=YOUR_CONCEPT_ID \
  test
```

Пароли и другие секреты не относятся к seed/reference data. Их нужно передавать
через секреты CI, переменные окружения или system properties, а не добавлять в
новые reference keys.

### Как добавить новую reference data

1. Убедитесь, что объект действительно создается seed-механизмом и существует на
   целевом стенде. В `config.properties` хранится идентификатор объекта, а не
   сам объект и не его provisioning.
2. Получите стабильный UUID или ID объекта и добавьте новый non-secret key в
   `config.properties`.
3. Добавьте в `ReferenceTestData` getter с предметным именем:

   ```java
   public static String answerConceptId() {
       return Config.getProperty("answer_concept_id");
   }
   ```

4. В тесте или helper используйте `ReferenceTestData.answerConceptId()`, а не
   строку ключа и не literal UUID.
5. Проверьте значение на каждом окружении. Если UUID отличаются, задайте
   environment-specific override через system property или переменную окружения.

Добавляйте reference data только для стабильных общих объектов. Если данные
должны быть уникальными для теста, создаваться во время выполнения или удаляться
после теста, это test data, а не seed/reference data. Для таких случаев создайте
отдельный helper, например:

```java
String identifier = PatientTestData.generateIdentifier();
```

`PatientTestData.generateIdentifier()` использует
`patient_identifier_source_uuid`, выполняет служебный `POST` в IDGen и возвращает
новый identifier. IDGen в этом сценарии является подготовкой данных, а не
тестируемым endpoint’ом requester layer.

## Шаг 2. Создайте `*Endpoints`

Для search-only ресурса достаточно одного `EndpointSpec`:

```java
package api.requests.endpoints;

import api.models.product.ProductSearchResponse;
import io.restassured.common.mapper.TypeRef;

public final class ProductEndpoints {

    public static final EndpointSpec<ProductSearchResponse> SEARCH =
            new EndpointSpec<>(
                    "/product",
                    new TypeRef<>() {
                    }
            );

    private ProductEndpoints() {
    }
}
```

`EndpointSpec<RESPONSE>` хранит две вещи:

- `pathTemplate` — путь, например `"/product"` или `"/product/{id}"`;
- `responseTypeRef` — тип DTO, в который successful requester преобразует JSON.

HTTP-метод в `EndpointSpec` не указывается. Он определяется типом requester’а:
`SearchRequester` делает GET коллекции, а `CrudRequester` выбирает GET, POST или
DELETE для соответствующей операции.

Если тот же ресурс поддерживает CRUD, оставьте `SEARCH` и добавьте четыре specs,
объединив их в `CrudOperations`:

```java
public final class ProductEndpoints {

    public static final EndpointSpec<ProductSearchResponse> SEARCH =
            new EndpointSpec<>(
                    "/product",
                    new TypeRef<>() {}
            );

    public static final EndpointSpec<ProductResponse> CREATE =
            new EndpointSpec<>("/product", new TypeRef<>() {});

    public static final EndpointSpec<ProductResponse> READ =
            new EndpointSpec<>("/product/{id}", new TypeRef<>() {});

    public static final EndpointSpec<ProductResponse> UPDATE =
            new EndpointSpec<>("/product/{id}", new TypeRef<>() {});

    public static final EndpointSpec<Void> DELETE =
            new EndpointSpec<>("/product/{id}", new TypeRef<>() {});

    public static final CrudOperations<ProductResponse> CRUD =
            new CrudOperations<>(CREATE, READ, UPDATE, DELETE);

    private ProductEndpoints() {
    }
}
```

Названия `CREATE`, `READ`, `UPDATE`, `DELETE`, `SEARCH` — это соглашение,
которое помогает быстро находить нужный контракт. Один и тот же путь может
встречаться в нескольких specs, если у операций разные HTTP-методы или типы
response.

Для nested endpoint в path template укажите `{parentId}` и используйте
`NestedSearchRequester` или `NestedCrudRequester` через соответствующий метод
factory. Идентификатор родителя не добавляется в query params.

## Шаг 3. Создайте `*Steps`

Конструктор Steps принимает `RequesterFactory`. Factory сам создает raw
requester и оборачивает его в successful requester.

### Search-only resource

```java
package api.requests.steps;

import api.models.product.ProductSearchParams;
import api.models.product.ProductSearchResponse;
import api.requests.endpoints.ProductEndpoints;
import api.requests.skeleton.requesters.RequesterFactory;
import api.requests.skeleton.requesters.SuccessfulSearchRequester;

public class ProductSteps {

    private final SuccessfulSearchRequester<
            ProductSearchParams,
            ProductSearchResponse
            > searchRequester;

    public ProductSteps(RequesterFactory requesters) {
        this.searchRequester =
                requesters.successfulSearch(ProductEndpoints.SEARCH);
    }

    public ProductSearchResponse searchProducts(
            ProductSearchParams params
    ) {
        return searchRequester.search(params);
    }
}
```

Типы в поле (`ProductSearchParams`, `ProductSearchResponse`) нужны не только
для читаемости: они помогают Java вывести generic-типы вызова
`requesters.successfulSearch(...)`.

### Resource с CRUD и search

Для CRUD добавьте requester и предметные методы:

```java
public class ProductSteps {

    private final SuccessfulSearchRequester<
            ProductSearchParams,
            ProductSearchResponse
            > searchRequester;
    private final SuccessfulCrudRequester<
            ProductCreateRequest,
            ProductUpdateRequest,
            ProductResponse
            > crudRequester;

    public ProductSteps(RequesterFactory requesters) {
        this.searchRequester =
                requesters.successfulSearch(ProductEndpoints.SEARCH);
        this.crudRequester =
                requesters.successfulCrud(ProductEndpoints.CRUD);
    }

    public ProductSearchResponse searchProducts(
            ProductSearchParams params
    ) {
        return searchRequester.search(params);
    }

    public ProductResponse createProduct(ProductCreateRequest request) {
        return crudRequester.create(request);
    }

    public ProductResponse getProduct(String id) {
        return crudRequester.get(id);
    }

    public ProductResponse updateProduct(
            String id,
            ProductUpdateRequest request
    ) {
        return crudRequester.update(id, request);
    }

    public Response deleteProduct(String id) {
        return crudRequester.delete(id);
    }

    public Response purgeProduct(String id) {
        return crudRequester.delete(id, DeleteMode.PURGE);
    }
}
```

Публичные методы Steps должны иметь предметные имена (`createProduct`,
`searchProducts`, `getProduct`). В них можно собирать params-модель и задавать
часто используемые значения по умолчанию. Assertions и проверки бизнес-данных
в Steps не добавляются — они остаются в тесте.

Реальный пример такого класса находится в
[`ObservationSteps.java`](../../src/main/java/api/requests/steps/ObservationSteps.java).

### Переиспользование CRUD-части в Steps

Если несколько ресурсных Steps повторяют инициализацию successful и raw CRUD,
для общей части можно использовать
[`CrudStepsSupport.java`](../../src/main/java/api/requests/steps/CrudStepsSupport.java):

```java
public class ProductSteps extends CrudStepsSupport<
        ProductCreateRequest,
        ProductUpdateRequest,
        ProductResponse
        > {

    private final SuccessfulSearchRequester<
            ProductSearchParams,
            ProductSearchResponse
            > searchRequester;

    public ProductSteps(RequesterFactory requesters) {
        super(requesters, ProductEndpoints.CRUD);
        this.searchRequester =
                requesters.successfulSearch(ProductEndpoints.SEARCH);
    }

    public ProductResponse createProduct(ProductCreateRequest request) {
        return successfulCrud.create(request);
    }

    public Response getProductRaw(String id) {
        return rawCrud.get(id, ReadOptions.defaults());
    }
}
```

`CrudStepsSupport` не добавляет предметных методов и не содержит assertions: он
только хранит типизированные `successfulCrud` и `rawCrud`. Последний нужен,
когда тому же Steps требуется негативный CRUD-сценарий или проверка raw
response; для обычного позитивного Steps достаточно прямого поля
`SuccessfulCrudRequester`.

Если в нескольких Steps начнет повторяться именно связка `Crud + Search`, можно
добавить отдельный абстрактный `CrudSearchStepsSupport`, который расширяет
`CrudStepsSupport` и инициализирует `SuccessfulSearchRequester`. Такой класс не
нужно вводить заранее: сначала должна появиться реальная повторяемость и
одинаковый контракт.

Nested CRUD и Nested Search не следует включать в эту иерархию. У них другой
контракт с обязательным `parentId`, а наличие nested search не следует из
наличия nested CRUD. Храните nested requester’ы отдельными полями в конкретном
Steps или отдельным композиционным support-объектом:

```java
public class PatientSteps extends CrudStepsSupport<
        PatientCreateRequest,
        PatientUpdateRequest,
        PatientResponse
        > {

    private final SuccessfulNestedCrudRequester<
            AllergyCreateRequest,
            AllergyUpdateRequest,
            AllergyResponse
            > allergyCrud;

    public PatientSteps(RequesterFactory requesters) {
        super(requesters, PatientEndpoints.CRUD);
        this.allergyCrud =
                requesters.successfulNestedCrud(PatientEndpoints.ALLERGY_CRUD);
    }
}
```

Так top-level CRUD переиспользуется через наследование, а nested API остается
явной композицией с собственным parent ID.

## Шаг 4. Зарегистрируйте Steps в `ApiClient`

Добавьте ресурс в три места `ApiClient`:

```java
public final class ApiClient {

    private final ProductSteps products;

    private ApiClient(RequestSpecification specification) {
        RequesterFactory requesters =
                new RequesterFactory(specification);

        products = new ProductSteps(requesters);
    }

    public ProductSteps products() {
        return products;
    }
}
```

В существующем `ApiClient` это означает:

1. добавить поле `private final ProductSteps products`;
2. создать его в конструкторе через тот же `RequesterFactory`;
3. добавить accessor `products()`.

Не создавайте новый `RequestSpecification` для каждого ресурса: все Steps,
созданные одним `ApiClient`, должны использовать одну и ту же авторизацию и
общие настройки запроса.

## Шаг 5. Напишите тест

```java
class ProductTest extends BaseApiTest {

    private ApiClient admin;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    @Test
    void searchesProducts() {
        ProductSearchResponse response =
                admin.products().searchProducts(
                        new ProductSearchParams("keyboard")
                );

        softly.assertThat(response.results()).isNotEmpty();
    }
}
```

Если params-модель собирается внутри Steps, тест может быть еще короче:

```java
ProductSearchResponse response =
        admin.products().searchProducts("keyboard");
```

## Если нужен негативный сценарий

Основное назначение `RequesterFactory` — создание successful requester’ов. Для
CRUD он также предоставляет `rawCrud(...)`, поэтому общий CRUD-support может
хранить raw requester рядом с successful. В отдельном негативном тесте можно
создать raw requester напрямую и проверять `Response` самостоятельно:

```java
SearchEndpoint<UserSearchParams> requester = new SearchRequester<>(
        RequestSpecs.withAuth("wrong-user", "wrong-password"),
        UserEndpoints.SEARCH
);

Response response = requester.search(
        new UserSearchParams("admin", "default")
);

response.then().statusCode(401);
```

Не используйте successful requester для запроса, который должен вернуть ошибку:
он проверит ожидаемый успешный статус раньше assertions теста.

## Чек-лист нового resource slice

- [ ] DTO находятся в `api.models.<resource>`.
- [ ] Query parameters описаны в `*SearchParams`, если они нужны.
- [ ] `*Endpoints` содержит корректные пути и response types.
- [ ] CRUD specs объединены в `CrudOperations`, если ресурс поддерживает CRUD.
- [ ] `*Steps` получает `RequesterFactory`, а не создает `RequestSpecification`.
- [ ] У Steps есть предметные публичные методы без assertions.
- [ ] Steps зарегистрирован в `ApiClient`.
- [ ] Позитивный тест использует `ApiClient.admin()` или
      `ApiClient.authenticatedAs(...)`.
- [ ] Для негативного теста используется raw requester.
