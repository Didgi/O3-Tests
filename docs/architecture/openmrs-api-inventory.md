# Инвентаризация OpenMRS API для requester layer

Дата анализа: 12 сентября 2026 года.
Назначение: зафиксировать факты, из которых выводится [архитектура API requester layer](api-requester-layer.md).

## 1. Источники и обозначения

| Код | Источник |
|---|---|
| `S1` | Локальный [`infra/o3-api.yaml`](../../infra/o3-api.yaml), Swagger `2.0`, версия API `2.8.0-42ce79`. |
| `S2` | [OpenMRS REST API](https://rest.openmrs.org/) — высокоуровневая документация ресурсов, auth и IDGen. |
| `S3` | Работающий OpenMRS `qa` deployment — окончательный источник перед реализацией и тестированием. |

В таблицах:

- `path:x*` — обязательный path parameter;
- `query:x` — необязательный query parameter;
- `body:resource*` — обязательное JSON-тело;
- `v` — representation ответа;
- `limit` и `startIndex` — pagination;
- success status для `S1` взят из Swagger без предположений.

Каждая операция основных ресурсов и Auth сопоставлена ровно одному архитектурному типу. Служебный IDGen-вызов отмечен отдельно как подготовка test data вне requester layer.

## 2. Итог по текущему scope

Локальный Swagger содержит 64 операции выбранных основных ресурсов и subresources. Из них 52 относятся к CRUD/Nested CRUD и 12 — к Search/Nested Search:

| Группа | Операций |
|---|---:|
| `CrudEndpoint` для шести top-level resources | 24 |
| `SearchEndpoint` для шести top-level resources | 6 |
| `NestedCrudEndpoint` для семи subresources | 28 |
| `NestedSearchEndpoint` для шести subresources | 6 |
| **Всего в локальном Swagger** | **64** |

Дополнительно в scope входят две Auth-операции. Один технический IDGen `POST` используется только для подготовки Patient test data и не входит в requester layer. Эти операции отсутствуют в `S1`: Auth еще требуется подтвердить по `S3`, а фактический IDGen-контракт уже зафиксирован по работающему стенду.

## 3. Top-level resources

### 3.1. User

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/user` | List/search users | `query:limit`, `startIndex`, `v`, `q`, `username` | 200 | `Search.search` | `S1` |
| POST | `/user` | Create user | `body:resource*` | 201 | `Crud.create` | `S1` |
| GET | `/user/{uuid}` | Get user | `path:uuid*`, `query:v` | 200 | `Crud.get` | `S1` |
| POST | `/user/{uuid}` | Update user | `path:uuid*`, `body:resource*` | 201 | `Crud.update` | `S1` |
| DELETE | `/user/{uuid}` | Delete/purge user | `path:uuid*`, `query:purge` | 204 | `Crud.delete` | `S1` |

### 3.2. Patient

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/patient` | List/search patients | `query:limit`, `startIndex`, `v`, `q`, `identifier`, `includeDead`, `searchType` | 200 | `Search.search` | `S1` |
| POST | `/patient` | Create patient | `body:resource*` | 201 | `Crud.create` | `S1` |
| GET | `/patient/{uuid}` | Get patient | `path:uuid*`, `query:v` | 200 | `Crud.get` | `S1` |
| POST | `/patient/{uuid}` | Update patient | `path:uuid*`, `body:resource*` | 201 | `Crud.update` | `S1` |
| DELETE | `/patient/{uuid}` | Delete/purge patient | `path:uuid*`, `query:purge` | 204 | `Crud.delete` | `S1` |

В Swagger параметр `q` для `/patient` задекларирован дважды; в архитектурной модели он считается одним query parameter до проверки фактического контракта.

### 3.3. Person

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/person` | List/search persons | `query:limit`, `startIndex`, `v`, `q` | 200 | `Search.search` | `S1` |
| POST | `/person` | Create person | `body:resource*` | 201 | `Crud.create` | `S1` |
| GET | `/person/{uuid}` | Get person | `path:uuid*`, `query:v` | 200 | `Crud.get` | `S1` |
| POST | `/person/{uuid}` | Update person | `path:uuid*`, `body:resource*` | 201 | `Crud.update` | `S1` |
| DELETE | `/person/{uuid}` | Delete/purge person | `path:uuid*`, `query:purge` | 204 | `Crud.delete` | `S1` |

### 3.4. Visit

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/visit` | List/search visits | `query:limit`, `startIndex`, `v`, `q` | 200 | `Search.search` | `S1` |
| POST | `/visit` | Create visit | `body:resource*` | 201 | `Crud.create` | `S1` |
| GET | `/visit/{uuid}` | Get visit | `path:uuid*`, `query:v` | 200 | `Crud.get` | `S1` |
| POST | `/visit/{uuid}` | Update visit | `path:uuid*`, `body:resource*` | 201 | `Crud.update` | `S1` |
| DELETE | `/visit/{uuid}` | Delete/purge visit | `path:uuid*`, `query:purge` | 204 | `Crud.delete` | `S1` |

### 3.5. Encounter

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/encounter` | List/search encounters | `query:limit`, `startIndex`, `v`, `q`, `obsConcept`, `obsValues`, `todate`, `patient`, `visit`, `totalCount`, `encounterType`, `fromdate`, `order` | 200 | `Search.search` | `S1` |
| POST | `/encounter` | Create encounter | `body:resource*` | 201 | `Crud.create` | `S1` |
| GET | `/encounter/{uuid}` | Get encounter | `path:uuid*`, `query:v` | 200 | `Crud.get` | `S1` |
| POST | `/encounter/{uuid}` | Update encounter | `path:uuid*`, `body:resource*` | 201 | `Crud.update` | `S1` |
| DELETE | `/encounter/{uuid}` | Delete/purge encounter | `path:uuid*`, `query:purge` | 204 | `Crud.delete` | `S1` |

### 3.6. Observation

OpenMRS использует resource path `/obs`.

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/obs` | List/search observations | `query:limit`, `startIndex`, `v`, `q`, `concepts`, `patient`, `concept`, `groupingConcepts`, `answers` | 200 | `Search.search` | `S1` |
| POST | `/obs` | Create observation | `body:resource*` | 201 | `Crud.create` | `S1` |
| GET | `/obs/{uuid}` | Get observation | `path:uuid*`, `query:v` | 200 | `Crud.get` | `S1` |
| POST | `/obs/{uuid}` | Update observation | `path:uuid*`, `body:resource*` | 201 | `Crud.update` | `S1` |
| DELETE | `/obs/{uuid}` | Delete/purge observation | `path:uuid*`, `query:purge` | 204 | `Crud.delete` | `S1` |

## 4. Subresources

### 4.1. Person address

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/person/{parent-uuid}/address` | List addresses | `path:parent-uuid*`, `query:limit`, `startIndex`, `v` | 200 | `NestedSearch.search` | `S1` |
| POST | `/person/{parent-uuid}/address` | Create address | `path:parent-uuid*`, `body:resource*` | 201 | `NestedCrud.create` | `S1` |
| GET | `/person/{parent-uuid}/address/{uuid}` | Get address | `path:parent-uuid*`, `path:uuid*`, `query:v` | 200 | `NestedCrud.get` | `S1` |
| POST | `/person/{parent-uuid}/address/{uuid}` | Update address | `path:parent-uuid*`, `path:uuid*`, `body:resource*` | 201 | `NestedCrud.update` | `S1` |
| DELETE | `/person/{parent-uuid}/address/{uuid}` | Delete/purge address | `path:parent-uuid*`, `path:uuid*`, `query:purge` | 204 | `NestedCrud.delete` | `S1` |

### 4.2. Person attribute

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/person/{parent-uuid}/attribute` | List attributes | `path:parent-uuid*`, `query:limit`, `startIndex`, `v` | 200 | `NestedSearch.search` | `S1` |
| POST | `/person/{parent-uuid}/attribute` | Create attribute | `path:parent-uuid*`, `body:resource*` | 201 | `NestedCrud.create` | `S1` |
| GET | `/person/{parent-uuid}/attribute/{uuid}` | Get attribute | `path:parent-uuid*`, `path:uuid*`, `query:v` | 200 | `NestedCrud.get` | `S1` |
| POST | `/person/{parent-uuid}/attribute/{uuid}` | Update attribute | `path:parent-uuid*`, `path:uuid*`, `body:resource*` | 201 | `NestedCrud.update` | `S1` |
| DELETE | `/person/{parent-uuid}/attribute/{uuid}` | Delete/purge attribute | `path:parent-uuid*`, `path:uuid*`, `query:purge` | 204 | `NestedCrud.delete` | `S1` |

### 4.3. Person name

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/person/{parent-uuid}/name` | List names | `path:parent-uuid*`, `query:limit`, `startIndex`, `v` | 200 | `NestedSearch.search` | `S1` |
| POST | `/person/{parent-uuid}/name` | Create name | `path:parent-uuid*`, `body:resource*` | 201 | `NestedCrud.create` | `S1` |
| GET | `/person/{parent-uuid}/name/{uuid}` | Get name | `path:parent-uuid*`, `path:uuid*`, `query:v` | 200 | `NestedCrud.get` | `S1` |
| POST | `/person/{parent-uuid}/name/{uuid}` | Update name | `path:parent-uuid*`, `path:uuid*`, `body:resource*` | 201 | `NestedCrud.update` | `S1` |
| DELETE | `/person/{parent-uuid}/name/{uuid}` | Delete/purge name | `path:parent-uuid*`, `path:uuid*`, `query:purge` | 204 | `NestedCrud.delete` | `S1` |

### 4.4. Patient identifier

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/patient/{parent-uuid}/identifier` | List identifiers | `path:parent-uuid*`, `query:limit`, `startIndex`, `v` | 200 | `NestedSearch.search` | `S1` |
| POST | `/patient/{parent-uuid}/identifier` | Create identifier | `path:parent-uuid*`, `body:resource*` | 201 | `NestedCrud.create` | `S1` |
| GET | `/patient/{parent-uuid}/identifier/{uuid}` | Get identifier | `path:parent-uuid*`, `path:uuid*`, `query:v` | 200 | `NestedCrud.get` | `S1` |
| POST | `/patient/{parent-uuid}/identifier/{uuid}` | Update identifier | `path:parent-uuid*`, `path:uuid*`, `body:resource*` | 201 | `NestedCrud.update` | `S1` |
| DELETE | `/patient/{parent-uuid}/identifier/{uuid}` | Delete/purge identifier | `path:parent-uuid*`, `path:uuid*`, `query:purge` | 204 | `NestedCrud.delete` | `S1` |

### 4.5. Patient allergy

Collection GET для этого subresource отсутствует в `S1`, поэтому `NestedSearchEndpoint` к нему не применяется.

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| POST | `/patient/{parent-uuid}/allergy` | Create allergy | `path:parent-uuid*`, `body:resource*` | 201 | `NestedCrud.create` | `S1` |
| GET | `/patient/{parent-uuid}/allergy/{uuid}` | Get allergy | `path:parent-uuid*`, `path:uuid*`, `query:v` | 200 | `NestedCrud.get` | `S1` |
| POST | `/patient/{parent-uuid}/allergy/{uuid}` | Update allergy | `path:parent-uuid*`, `path:uuid*`, `body:resource*` | 201 | `NestedCrud.update` | `S1` |
| DELETE | `/patient/{parent-uuid}/allergy/{uuid}` | Delete/purge allergy | `path:parent-uuid*`, `path:uuid*`, `query:purge` | 204 | `NestedCrud.delete` | `S1` |

### 4.6. Visit attribute

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/visit/{parent-uuid}/attribute` | List attributes | `path:parent-uuid*`, `query:limit`, `startIndex`, `v` | 200 | `NestedSearch.search` | `S1` |
| POST | `/visit/{parent-uuid}/attribute` | Create attribute | `path:parent-uuid*`, `body:resource*` | 201 | `NestedCrud.create` | `S1` |
| GET | `/visit/{parent-uuid}/attribute/{uuid}` | Get attribute | `path:parent-uuid*`, `path:uuid*`, `query:v` | 200 | `NestedCrud.get` | `S1` |
| POST | `/visit/{parent-uuid}/attribute/{uuid}` | Update attribute | `path:parent-uuid*`, `path:uuid*`, `body:resource*` | 201 | `NestedCrud.update` | `S1` |
| DELETE | `/visit/{parent-uuid}/attribute/{uuid}` | Delete/purge attribute | `path:parent-uuid*`, `path:uuid*`, `query:purge` | 204 | `NestedCrud.delete` | `S1` |

### 4.7. Encounter provider

| Метод | Path | Назначение | Параметры | Success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/encounter/{parent-uuid}/encounterprovider` | List providers | `path:parent-uuid*`, `query:limit`, `startIndex`, `v` | 200 | `NestedSearch.search` | `S1` |
| POST | `/encounter/{parent-uuid}/encounterprovider` | Create provider | `path:parent-uuid*`, `body:resource*` | 201 | `NestedCrud.create` | `S1` |
| GET | `/encounter/{parent-uuid}/encounterprovider/{uuid}` | Get provider | `path:parent-uuid*`, `path:uuid*`, `query:v` | 200 | `NestedCrud.get` | `S1` |
| POST | `/encounter/{parent-uuid}/encounterprovider/{uuid}` | Update provider | `path:parent-uuid*`, `path:uuid*`, `body:resource*` | 201 | `NestedCrud.update` | `S1` |
| DELETE | `/encounter/{parent-uuid}/encounterprovider/{uuid}` | Delete/purge provider | `path:parent-uuid*`, `path:uuid*`, `query:purge` | 204 | `NestedCrud.delete` | `S1` |

## 5. Auth

`/session` отсутствует в `S1`. Операции подтверждены `S2`, а status codes и детали cookie необходимо перепроверить по `S3`.

| Метод | Path | Назначение | Параметры | Ожидаемый success | Тип | Источник |
|---|---|---|---|---:|---|---|
| GET | `/session` | Получить/authenticate session | Basic credentials | 200 + `authenticated=true` | `Auth.getSession` | `S2`, проверить `S3` |
| DELETE | `/session` | Завершить session | Текущая auth/session context | 204 | `Auth.logout` | `S2`, проверить `S3` |

Наличие `JSESSIONID` само по себе не является достаточным признаком успешной авторизации. Successful-слой проверяет поле `authenticated` в теле ответа.

## 6. Вспомогательный IDGen

Операция нужна только для подготовки данных Patient-тестов. Она отсутствует в `S1` и не означает включение всего IDGen API в тестовое покрытие.

| Метод | Path | Назначение | Параметры | Ожидаемый success | Тип | Источник |
|---|---|---|---|---:|---|---|
| POST | `/idgen/identifiersource/{sourceUuid}/identifier` | Generate one patient identifier | `path:sourceUuid*`; JSON body `{}` | 201 + непустой `identifier` | Test-data setup, вне requester layer | `S3` |

`sourceUuid` указывает на IdentifierSource `OpenMRS ID`, хранится в `config.properties` и не является test target. Метод `PatientTestData.generateIdentifier()` отправляет прямой `POST` с пустым JSON-объектом `{}` и извлекает поле `identifier`. Отдельные interface/requester/successful requester и response DTO для этого вызова не нужны. Batch, reserve и upload-команды не входят в контракт.

## 7. Исключения

В текущей архитектуре и inventory намеренно отсутствуют:

- `POST /password` и `POST /password/{uuid}`;
- `/visitconfiguration`;
- CRUD `/idgen/identifiersource`;
- batch generation через `POST /idgen/identifiersource` с JSON body;
- reserve identifiers;
- `uploadFromSource` и `uploadFromFile`;
- остальные OpenMRS resources и subresources.

Наличие endpoint'а в общей документации OpenMRS не делает его частью текущего scope. При расширении покрытия сначала обновляется этот inventory, затем оценивается необходимость нового endpoint-типа.

## 8. Контроль полноты

- 30 top-level операций сопоставлены `Crud` или `Search`.
- 34 nested операции сопоставлены `NestedCrud` или `NestedSearch`.
- Две Auth-операции сопоставлены `Auth`.
- Одна вспомогательная IDGen-операция намеренно оставлена вне requester layer.
- Ни одна операция текущего scope не требует дополнительного endpoint-типа.
