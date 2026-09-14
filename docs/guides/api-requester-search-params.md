# Search и query parameters в API requester layer

Этот гайд объясняет, как описывать query parameters в модели и передавать их в
`SearchRequester` или `NestedSearchRequester`.

Для полного добавления нового ресурса с `Endpoints`, `Steps` и регистрацией в
`ApiClient` используйте [гайд по resource slice](api-requester-resource-slice.md).

## Главное правило

`Search` не означает отдельный HTTP-метод или отдельный URL. И list, и search
выполняют один и тот же `GET` коллекции:

- list — с пустыми query parameters;
- search — с params-моделью, которая превращается в query string.

```java
// GET /user
userSearch.search(QueryParams.empty());

// GET /user?q=admin&v=default
userSearch.search(new UserSearchParams("admin", "default"));
```

## Как создать params-модель

Для каждого ресурса с фильтрами поиска создавайте отдельную модель в
`api.models.<resource>`. Модель должна:

1. быть `record` или обычным классом;
2. реализовывать `QueryParams`;
3. содержать поля для параметров, которые поддерживает конкретный endpoint;
4. в `asMap()` возвращать точные имена query parameters из Swagger или
   документации OpenMRS API.

Например, endpoint пользователей поддерживает параметры `q` и `v`:

```java
package api.models.user;

import api.requests.skeleton.query.QueryParams;

import java.util.Map;

public record UserSearchParams(
        String query,
        String representation
) implements QueryParams {

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "q", query,
                "v", representation
        );
    }
}
```

`query` и `representation` — имена Java-полей. `q` и `v` — реальные имена
параметров HTTP-запроса. В `asMap()` нужно указывать именно вторые.

Params-модель описывает параметры запроса, а не поля response DTO. Например,
`q` может называться `query` в Java-модели, если это делает код понятнее, но
ключ в результирующей map должен остаться `q`.

## Необязательные параметры

Если параметр необязательный, добавляйте его в map только при наличии значения.
`Map.of(...)` не принимает `null`, поэтому для optional-параметров используйте
изменяемую map:

```java
package api.models.observations;

import api.requests.skeleton.query.QueryParams;

import java.util.HashMap;
import java.util.Map;

public record ObservationSearchParams(
        String patient,
        String concept,
        Integer limit,
        Integer startIndex
) implements QueryParams {

    @Override
    public Map<String, ?> asMap() {
        Map<String, Object> params = new HashMap<>();

        putIfNotNull(params, "patient", patient);
        putIfNotNull(params, "concept", concept);
        putIfNotNull(params, "limit", limit);
        putIfNotNull(params, "startIndex", startIndex);

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

Вызов:

```java
obsSearch.search(new ObservationSearchParams(
        patientId,
        null,
        20,
        0
));
```

Результирующий запрос содержит только заданные параметры:
`GET /obs?patient=<patientId>&limit=20&startIndex=0`.

Правило для пустых строк зависит от контракта API. Не превращайте автоматически
пустую строку в `null`: если endpoint различает эти значения, это должно быть
явно отражено в params-модели.

## List и search через один requester

В прикладном позитивном тесте обычно вызывается не requester напрямую, а
ресурсный Steps через `ApiClient`:

```java
ApiClient admin = ApiClient.admin();

admin.users().searchUsers("admin");
admin.observations().listObservations();
```

Низкоуровневые примеры ниже нужны при реализации Steps или для raw/negative
сценариев.

Для list отдельная модель не нужна:

```java
SearchEndpoint<QueryParams> rawSearch = new SearchRequester<>(
        requestSpecification,
        UserEndpoints.SEARCH
);

rawSearch.search(QueryParams.empty());
```

Для search используйте resource-specific модель:

```java
SearchEndpoint<UserSearchParams> rawSearch = new SearchRequester<>(
        requestSpecification,
        UserEndpoints.SEARCH
);

rawSearch.search(new UserSearchParams("admin", "default"));
```

В позитивном тесте params-модель обычно собирается внутри предметного `Steps`:

```java
public UserSearchResponse searchUsers(String query) {
    return userSearchRequester.search(
            new UserSearchParams(query, "default")
    );
}
```

Сам тест вызывает предметный метод и не собирает query string вручную.

Если нужно быстро проверить raw requester или написать негативный сценарий,
допустимо передать параметры напрямую:

```java
rawSearch.search(QueryParams.of(Map.of(
        "q", "admin",
        "v", "default"
)));
```

Для переиспользуемого прикладного кода предпочтительнее отдельная params-модель:
она централизует имена параметров и правила обработки optional-значений.

## Query parameters и path parameters

Параметры в URL нужно разделять по их расположению:

- `/patient/{parentId}/identifier` — `parentId` является path parameter;
- `/patient?identifier=123` — `identifier` является query parameter.

Path parameter передается отдельным аргументом nested requester, а query
parameters — params-моделью:

```java
nestedSearch.search(
        patientId,
        new IdentifierSearchParams(20, 0)
);
```

Не добавляйте `parentId` в `asMap()`, если он уже является частью шаблона пути.
`NestedSearchRequester` подставит его в URL самостоятельно.

## Практический алгоритм

1. Откройте Swagger или инвентаризацию API и найдите collection endpoint.
2. Выпишите только его query parameters и проверьте их точные имена.
3. Создайте `*SearchParams` в `api.models.<resource>`.
4. Реализуйте `QueryParams.asMap()` и добавьте optional-параметры только при
   наличии значения.
5. Передайте модель в `search(...)` внутри steps или raw requester.
6. Для list используйте `QueryParams.empty()`.

Не собирайте `Map` с параметрами непосредственно в каждом тесте: это приводит
к дублированию имён и расхождению между тестами.
