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

Позитивный тест вызывает предметный класс `*Steps`. Считайте, что готовых steps
нет: ниже мы создадим step с нуля, соберем в нем raw и successful requester'ы и
дадим операции понятное предметное имя. Конструировать requester прямо в тесте
стоит только для негативного сценария.

```text
Тест
 ├─ позитивный сценарий → Steps → SuccessfulRequester → Requester → API
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

Используйте `Successful...Requester` через предметный `*Steps`, который создается
вместе с тестом. Successful-обертка сама проверит базовый контракт и вернет DTO:

| Операция | Ожидаемый статус |
|---|---:|
| `create` | `201` |
| `get`, `search`, получение session | `200` |
| `update` | `201` |
| `delete`, `logout` | `204` |

Для auth дополнительно проверяется `authenticated == true`.

```java
UserSearchResponse response = userSteps.searchUsers("admin");
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

## Как написать тест за 5 минут

Этот путь рассчитан на ресурс, для которого уже существуют DTO и endpoint spec,
но еще нет `*Steps`. За пять минут мы выберем requester, напишем step с нуля,
добавим тест и запустим его. Если нет также DTO или endpoint spec, сначала нужен
новый resource slice — это отдельная задача, описанная ниже.

### 0:00–1:00. Выберите requester

Допустим, нужно проверить `GET /user?q=admin`:

- URL не содержит `parentId`;
- нужен поиск коллекции, значит выбираем `SearchRequester`;
- сценарий позитивный, значит добавляем `SuccessfulSearchRequester`;
- запрос выполняется от администратора через
  `RequestSpecs.withAdminBasicAuth()`.

Для сборки уже существуют `UserEndpoints.SEARCH`, `UserSearchParams` и
`UserSearchResponse`.

### 1:00–3:00. Напишите step с нуля

Создайте файл `src/main/java/api/requests/steps/UserSteps.java`:

```java
package api.requests.steps;

import api.models.user.UserSearchParams;
import api.models.user.UserSearchResponse;
import api.requests.skelethon.endpoints.UserEndpoints;
import api.requests.skelethon.interfaces.SearchEndpoint;
import api.requests.skelethon.requesters.SearchRequester;
import api.requests.skelethon.requesters.SuccessfulSearchRequester;
import io.restassured.specification.RequestSpecification;

public class UserSteps {

    private final SuccessfulSearchRequester<
            UserSearchParams,
            UserSearchResponse
            > userSearchRequester;

    public UserSteps(RequestSpecification requestSpecification) {
        SearchEndpoint<UserSearchParams> rawRequester = new SearchRequester<>(
                requestSpecification,
                UserEndpoints.SEARCH
        );

        userSearchRequester = new SuccessfulSearchRequester<>(
                rawRequester,
                UserEndpoints.SEARCH
        );
    }

    public UserSearchResponse searchUsers(String query) {
        return userSearchRequester.search(
                new UserSearchParams(query, "default")
        );
    }
}
```

В step есть три части:

1. Поле хранит successful requester нужных generic-типов.
2. Конструктор получает готовую `RequestSpecification`, создает raw requester и
   оборачивает его в successful requester.
3. Публичный метод переводит предметное действие `searchUsers` в технический
   вызов `search(...)` и собирает query parameters.

Step не проверяет конкретные данные ответа и не содержит JUnit assertions. Его
задача — скрыть HTTP-детали и дать тесту предметную операцию, которую можно
переиспользовать.

### 3:00–4:00. Создайте тест

Создайте файл
`src/test/java/api_tests/p0/UserSearchSmokeTest.java`:

```java
package api_tests.p0;

import api.models.user.UserSearchResponse;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserSearchSmokeTest extends BaseApiTest {

    private UserSteps userSteps;

    @BeforeEach
    void setUp() {
        userSteps = new UserSteps(RequestSpecs.withAdminBasicAuth());
    }

    @Test
    void findsAdminUser() {
        UserSearchResponse response = userSteps.searchUsers("admin");

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
- `RequestSpecs.withAdminBasicAuth()` добавляет base URL, JSON headers,
  авторизацию, логирование, Allure и сбор Swagger coverage;
- `UserSteps` скрывает сборку requester'а и params-модели;
- `SuccessfulSearchRequester` внутри steps проверяет `200` и преобразует JSON в
  `UserSearchResponse`;
- в тесте остается только действие и проверка бизнес-результата.

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

Запуск всего набора API P0:

```bash
./mvnw -Papi-p0 test
```

## Частые операции

### Получить ресурс в нужном representation

```java
ObsResponse observation = successfulObs.get(obsId, ReadOptions.full());
```

Доступные варианты:

- `ReadOptions.defaults()` — не передавать параметр `v`;
- `ReadOptions.ref()` — `v=ref`;
- `ReadOptions.defaultRepresentation()` — явно передать `v=default`;
- `ReadOptions.full()` — `v=full`;
- `ReadOptions.custom("uuid,display")` — `v=custom:(uuid,display)`.

### Получить список или выполнить поиск

```java
// List без фильтров
rawSearch.search(QueryParams.empty());

// Search с фильтрами — в прикладном коде предпочтительнее отдельная params-модель
rawSearch.search(QueryParams.of(Map.of("q", "admin", "v", "default")));
```

### Удалить ресурс

```java
successfulRequester.delete(id);                    // обычное удаление
successfulRequester.delete(id, DeleteMode.PURGE); // ?purge=true
```

### Вызвать nested endpoint

У nested requester первым аргументом всегда идет `parentId`, затем `id`
дочернего ресурса:

```java
AllergyResponse allergy = successfulAllergies.get(
        patientId,
        allergyId,
        ReadOptions.full()
);
```

Для nested list/search нужен только `parentId` и параметры:

```java
ProviderSearchResponse providers = successfulProviderSearch.search(
        encounterId,
        QueryParams.empty()
);
```

## Если нет DTO или EndpointSpec

Steps мы создаем вместе с тестом. Но «тест за 5 минут» предполагает, что контракт
ресурса уже описан DTO и endpoint spec. Если их нет, не пишите Rest
Assured-вызов прямо в позитивном тесте. Добавьте минимальный вертикальный срез:

1. request/response DTO в `api.models.<resource>`;
2. params-модель с `QueryParams`, если нужен search;
3. `EndpointSpec` или `CrudOperations` в
   `api.requests.skelethon.endpoints`;
4. raw requester выбранного типа;
5. соответствующую successful-обертку;
6. новый класс с предметным методом в `api.requests.steps`;
7. тест, который вызывает steps.

Минимальная сборка search-операции выглядит так:

```java
SearchEndpoint<UserSearchParams> rawRequester = new SearchRequester<>(
        requestSpecification,
        UserEndpoints.SEARCH
);

SuccessfulSearchRequester<UserSearchParams, UserSearchResponse> successfulRequester =
        new SuccessfulSearchRequester<>(rawRequester, UserEndpoints.SEARCH);
```

`EndpointSpec` связывает путь с типом успешного ответа:

```java
public static final EndpointSpec<UserSearchResponse> SEARCH =
        new EndpointSpec<>(
                "/user",
                new TypeRef<UserSearchResponse>() {}
        );
```

Если endpoint не укладывается ни в один из пяти типов, не маскируйте его под
CRUD. Сначала проверьте актуальный Swagger и обсудите отдельный контракт. Прямой
Rest Assured допустим для узкого служебного test-data helper, как генерация
patient identifier в `PatientTestData`, но не как обычный способ писать тесты.

## Чек-лист перед отправкой теста

- Requester выбран по форме endpoint'а, а не только по HTTP-методу.
- Позитивный тест идет через steps/successful requester.
- Негативный тест использует raw requester и сам проверяет status/body.
- Тест проверяет бизнес-результат, а не повторяет status, уже проверенный
  successful-оберткой.
- Авторизация задается через `RequestSpecs`, секреты не записаны в тест.
- Для item GET осознанно выбран `ReadOptions`.
- Для delete осознанно выбран обычный режим или `PURGE`.
- Запущен хотя бы новый тест; перед merge — подходящий Maven profile.

## Куда идти дальше

- [`api-requester-layer.md`](../architecture/api-requester-layer.md) — принципы и
  архитектурные решения.
- [`api-requester-examples.md`](../architecture/api-requester-examples.md) —
  полные примеры реализации всех типов requester'ов.
- [`openmrs-api-inventory.md`](../architecture/openmrs-api-inventory.md) — какие
  операции реально поддерживаются OpenMRS API.
