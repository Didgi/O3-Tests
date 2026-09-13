# API-тесты с нуля: выбор requester'а и первый тест за 5 минут

Этот гайд — точка входа для человека, который впервые открыл проект. Здесь нет
описания внутреннего устройства всего фреймворка: только то, что нужно, чтобы
выбрать правильный requester, написать тест и запустить его.

## Самое важное за минуту

Requester — это готовый способ отправить запрос определенной формы. Выбирать его
нужно в два шага:

1. По форме URL и операции выбрать `Auth`, `Crud`, `Search`, `NestedCrud` или
   `NestedSearch`.
2. По сценарию выбрать successful-обертку или raw requester:
   - позитивный сценарий → `Successful...Requester`;
   - негативный сценарий → обычный requester, возвращающий `Response`.

Позитивный тест вызывает предметный класс `*Steps` через `ApiClient`. `ApiClient`
выбирает авторизацию и предоставляет готовые Steps для ресурсов. Конструировать
requester прямо в тесте стоит только для негативного сценария.

```text
Тест
 ├─ позитивный сценарий → ApiClient → Steps → SuccessfulRequester → Requester → API
 └─ негативный сценарий ─────────→ Requester → API → raw Response
```

## Алгоритм выбора requester'а

### Шаг 1. Определите форму endpoint'а

Смотрите не только на HTTP-метод, а на смысл операции и форму URL.

| Что проверяем | Пример URL | Raw requester | Для позитивного сценария |
|---|---|---|---|
| Сессию или logout | `/session` | `AuthRequester` | `SuccessfulAuthRequester` |
| Создание или отдельный ресурс | `/obs`, `/obs/{id}` | `CrudRequester` | `SuccessfulCrudRequester` |
| Список или поиск верхнего уровня | `/user?q=admin` | `SearchRequester` | `SuccessfulSearchRequester` |
| Отдельный дочерний ресурс | `/patient/{parentId}/allergy/{id}` | `NestedCrudRequester` | `SuccessfulNestedCrudRequester` |
| Список или поиск внутри родителя | `/encounter/{parentId}/encounterprovider` | `NestedSearchRequester` | `SuccessfulNestedSearchRequester` |

Короткое дерево решения:

```text
Это работа с сессией?
 └─ да → Auth
 └─ нет
    ├─ URL требует parentId?
    │  ├─ да, нужен конкретный child по id → NestedCrud
    │  └─ да, нужен список children       → NestedSearch
    └─ parentId не нужен
       ├─ нужен конкретный resource / create / update / delete → Crud
       └─ нужен список или поиск                              → Search
```

Особенности OpenMRS, которые важно не перепутать:

- update ресурса отправляется через `POST /resource/{id}`, а не через `PUT` или
  `PATCH`; это уже учтено в `CrudRequester`;
- list — это `SearchRequester` с пустыми параметрами: `QueryParams.empty()`;
- поиск — тот же `SearchRequester`, но с заполненной params-моделью;
- все идентификаторы передаются как `String`;
- `Nested...Requester` нужен только тогда, когда URL требует идентификатор
  родителя, а не просто потому, что объекты связаны по смыслу.

### Шаг 2. Определите ожидаемый результат

#### Позитивный сценарий

Используйте `Successful...Requester` через предметный `*Steps`, который доступен
из `ApiClient`. Successful-обертка сама проверит базовый контракт и вернет DTO:

| Операция | Ожидаемый статус |
|---|---:|
| `create` | `201` |
| `get`, `search`, получение session | `200` |
| `update` | `201` |
| `delete`, `logout` | `204` |

Для auth дополнительно проверяется `authenticated == true`.

```java
UserSearchResponse response = ApiClient.admin()
        .users()
        .searchUsers("admin");
```

Не нужно еще раз проверять в тесте, что status равен `200`: это уже обязанность
successful-обертки. В самом тесте проверяйте бизнес-результат — например, что
нужный пользователь действительно найден.

#### Негативный сценарий

Используйте raw requester. Он ничего не предполагает об успешности и возвращает
полный Rest Assured `Response`, поэтому тест сам задает ожидаемые status и body:

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

Не оборачивайте такой вызов в `SuccessfulSearchRequester`: он ожидает `200` и
остановит тест раньше, чем тот проверит ошибку.

### Шаг 3. Выберите авторизацию

| Ситуация | Request specification |
|---|---|
| Публичный запрос или auth endpoint | `RequestSpecs.baseRequest()` |
| Запрос от администратора | `RequestSpecs.withAdminBasicAuth()` |
| Запрос от конкретного пользователя | `RequestSpecs.withAuth(username, password)` |

Base URL и учетные данные по умолчанию берутся из
`src/main/resources/config.properties`. Любое значение можно переопределить
системным параметром или переменной окружения, не меняя файл в репозитории.

## Используйте seed/reference data

Некоторые тесты используют данные, которые заранее созданы на стенде и не
являются test target. Их идентификаторы хранятся в
[`config.properties`](../../src/main/resources/config.properties), а в коде
получаются через [`ReferenceTestData.java`](../../src/main/java/api/testdata/ReferenceTestData.java).

```java
String conceptId = ReferenceTestData.conceptId();
String locationUuid = ReferenceTestData.locationUuid();
```

Не хардкодьте такие UUID и не создавайте один и тот же reference object в каждом
тесте. Если нужна новая reference data, добавьте ключ в properties, getter в
`ReferenceTestData` и используйте getter в тесте или test-data helper. Полное
описание ключей и порядка добавления — в
[`api-requester-resource-slice.md`](api-requester-resource-slice.md).

Для уникальных данных, которые тест создает во время выполнения, используйте
специализированный helper. Например, новый patient identifier получается так:

```java
String identifier = PatientTestData.generateIdentifier();
```

## Как написать тест за 5 минут

Этот путь рассчитан на ресурс, для которого уже существуют DTO, params-модель,
endpoint spec и Steps. Для пользователя такой готовый путь выглядит так:

### 0:00–1:00. Выберите способ авторизации

Для администратора используйте `ApiClient.admin()`. Для конкретного пользователя
передайте `Credentials` в `ApiClient.authenticatedAs(...)`:

```java
ApiClient admin = ApiClient.admin();

ApiClient user = ApiClient.authenticatedAs(
        new Credentials("username", "password")
);
```

`ApiClient` один раз создает `RequestSpecification`, `RequesterFactory` и Steps
для доступных ресурсов. В тесте не нужно вручную собирать raw requester и
successful requester.

### 1:00–4:00. Вызовите операцию через Steps

Создайте файл
`src/test/java/api_tests/p0/UserSearchSmokeTest.java`:

```java
package api_tests.p0;

import api.models.user.UserSearchResponse;
import api.steps.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserSearchSmokeTest extends BaseApiTest {

    private ApiClient admin;

    @BeforeEach
    void setUp() {
        admin = ApiClient.admin();
    }

    @Test
    void findsAdminUser() {
        UserSearchResponse response = admin.users().searchUsers("admin");

        softly.assertThat(response.results())
                .as("Users found by query")
                .isNotEmpty();

        softly.assertThat(response.results())
                .as("Search result contains admin")
                .anyMatch(user -> "admin".equals(user.display()));
    }
}
```

Что здесь дает фреймворк:

- `BaseApiTest` создает `SoftAssertions` и вызывает `assertAll()` после теста;
- `ApiClient.admin()` создает specification с base URL, JSON headers,
  авторизацией, логированием, Allure и сбором Swagger coverage;
- `ApiClient` предоставляет `UserSteps` через `admin.users()`;
- `UserSteps` скрывает сборку requester'а и params-модели;
- `SuccessfulSearchRequester` внутри steps проверяет `200` и преобразует JSON в
  `UserSearchResponse`;
- в тесте остается только действие и проверка бизнес-результата.

Если нужного ресурса еще нет в `ApiClient`, сначала добавьте resource slice по
инструкции [`api-requester-resource-slice.md`](api-requester-resource-slice.md).

### 4:00–5:00. Запустите только свой тест

Сначала убедитесь, что OpenMRS доступен по адресу из `config.properties`. Затем
из корня проекта выполните:

```bash
./mvnw -Dtest=UserSearchSmokeTest test
```

Для другого стенда настройки можно передать без изменения файлов:

```bash
./mvnw \
  -Dtest=UserSearchSmokeTest \
  -Dapi_baseurl=http://localhost:80 \
  -Dadmin_username=admin \
  -Dadmin_password=Admin123 \
  test
```

Запуск всего набора тестов:

```bash
./mvnw test
```

## Частые операции

### Получить ресурс в нужном representation

```java
ObservationResponse observation = ApiClient.admin()
        .observations()
        .getObservation(obsId);
```

Готовый `ObservationSteps` предоставляет базовое получение ресурса через
`getObservation(id)`. Если тесту нужно явно выбирать `v=ref`, `v=full` или
`v=custom:(...)`, добавьте соответствующий метод в предметный Steps и передайте
`ReadOptions` в underlying CRUD requester. Подробности о `ReadOptions` находятся
в [`api-requester-layer.md`](../architecture/api-requester-layer.md).

Если Steps предоставляет управление representation, в низкоуровневом CRUD
requester доступны варианты:

- `ReadOptions.defaults()` — не передавать параметр `v`;
- `ReadOptions.ref()` — `v=ref`;
- `ReadOptions.defaultRepresentation()` — явно передать `v=default`;
- `ReadOptions.full()` — `v=full`;
- `ReadOptions.custom("uuid,display")` — `v=custom:(uuid,display)`.

### Получить список или выполнить поиск

Подробное объяснение params-моделей, сопоставления Java-полей с именами API и
разницы между query и path parameters — в отдельном гайде:
[`api-requester-search-params.md`](api-requester-search-params.md).

```java
ApiClient admin = ApiClient.admin();

// List без фильтров
admin.observations().listObservations();

// Search с фильтрами
admin.users().searchUsers("admin");
```

Для правил создания `*SearchParams` и низкоуровневого raw-примера используйте
отдельный [гайд по search и query parameters](api-requester-search-params.md).

### Удалить ресурс

```java
ApiClient admin = ApiClient.admin();

admin.observations().deleteObservation(id); // обычное удаление
admin.observations().purgeObservation(id);  // ?purge=true
```

### Вызвать nested endpoint

У nested requester первым аргументом всегда идет `parentId`, затем `id` дочернего
ресурса. Для nested list/search передаются `parentId` и params-модель. Пример
ниже относится к уровню requester layer; готовый предметный Steps для nested
ресурса оформляется по [гайду resource slice](api-requester-resource-slice.md):

```java
ProviderSearchResponse providers = successfulProviderSearch.search(
        encounterId,
        QueryParams.empty()
);
```

## Если нет DTO или EndpointSpec

«Тест за 5 минут» предполагает, что resource slice уже описан. Если ресурса еще
нет в `ApiClient`, добавьте его по отдельному
[`api-requester-resource-slice.md`](api-requester-resource-slice.md). Вкратце,
нужны следующие части:

1. request/response DTO в `api.models.<resource>`;
2. params-модель с `QueryParams`, если нужен search;
3. `EndpointSpec` или `CrudOperations` в
   `api.requests.skelethon.endpoints`;
4. новый класс с предметными методами в `api.steps`;
5. регистрация Steps в `ApiClient`;
6. тест, который вызывает `ApiClient` и Steps.

Если endpoint не укладывается ни в один из пяти типов, не маскируйте его под
CRUD. Сначала проверьте актуальный Swagger и обсудите отдельный контракт. Прямой
Rest Assured допустим для узкого служебного test-data helper, как генерация
patient identifier в `PatientTestData`, но не как обычный способ писать тесты.

## Чек-лист перед отправкой теста

- Requester выбран по форме endpoint'а, а не только по HTTP-методу.
- Позитивный тест идет через `ApiClient` и предметный Steps.
- Негативный тест использует raw requester и сам проверяет status/body.
- Тест проверяет бизнес-результат, а не повторяет status, уже проверенный
  successful-оберткой.
- Авторизация задается через `ApiClient`, секреты не записаны в тест.
- Для item GET используется подходящий метод Steps; `ReadOptions` добавляется в
  Steps, если тестам нужен выбор representation.
- Для delete осознанно выбран обычный режим или `PURGE`.
- Запущен хотя бы новый тест; перед merge — подходящий Maven profile.

## Куда идти дальше

- [`api-requester-layer.md`](../architecture/api-requester-layer.md) — принципы и
  архитектурные решения.
- [`api-requester-resource-slice.md`](api-requester-resource-slice.md) — пошаговое
  добавление DTO, Endpoints, Steps и регистрации ресурса в `ApiClient`.
- [`api-requester-search-params.md`](api-requester-search-params.md) — создание
  params-моделей и работа с query parameters.
- [`api-requester-examples.md`](../architecture/api-requester-examples.md) —
  полные примеры реализации всех типов requester'ов.
- [`openmrs-api-inventory.md`](../architecture/openmrs-api-inventory.md) — какие
  операции реально поддерживаются OpenMRS API.
