# O3 Tests

Набор автотестов для OpenMRS 3 на Java 21. Основной текущий scope проекта — API-тесты и переиспользуемый requester layer на базе JUnit 5 и Rest Assured; в проекте также заложены базовые UI-абстракции на Selenide.

## Быстрый старт

### Требования

- JDK 21;
- Docker и Docker Compose, если OpenMRS нужно поднять локально;
- доступный OpenMRS 3 по адресу из конфигурации.

Запуск локального OpenMRS можно выполнить двумя способами:
1. Запустить docker compose локально из корня проекта
```bash
docker compose -f infra/docker-compose.yaml up -d
```
P.S. Особенность данного запуска состоит в том, что при старте db и backend 
будет происходить длительная инициализация и может пройти порядка 10-15 минут прежде чем
сервисы будут готовы к работе.

Чтобы избежать длительной инициализации продукта можно использовать 2 способ запуска.

2. Запустить скрипт из корня проекта, где перед этим необходимо положить файл .env в корень проекта

```bash
./infra/restart-docker-images.sh
```

По умолчанию тесты используют:

```text
http://localhost:80/openmrs/ws/rest/v1
```

После запуска стенда выполните из корня проекта, но убедитесь, что файл .env находится в корне проекта, 
т.к из него берутся данные УЗ admin

```bash
./mvnw test
```

Для запуска конкретного теста:

```bash
./mvnw -Dtest=UserTest test
```

Auth-интеграционные тесты по умолчанию отключены. Включить их можно так:

```bash
./mvnw -Dopenmrs.integration.enabled=true test
```

## Конфигурация

Значения по умолчанию находятся в [`src/main/resources/config.properties`](src/main/resources/config.properties). Для другого стенда или учетных данных используйте system properties либо переменные окружения — они имеют приоритет над файлом:

```bash
./mvnw \
  -Dapi_baseurl=http://localhost:80 \
  -Dapi_version=/openmrs/ws/rest/v1 \
  -Dadmin_username=admin \
  -Dadmin_password='***' \
  -Dtest_location_uuid=YOUR_LOCATION_UUID \
  test
```

Не добавляйте реальные пароли и секреты в репозиторий. Reference data хранится через [`ReferenceTestData`](src/main/java/api/testdata/ReferenceTestData.java), а уникальные данные для тестов — через специализированные test-data helpers.

## Как устроен проект

```text
src/main/java/
├── api/models              # DTO и query params
├── api/requests            # endpoint layer и предметные API Steps
│   ├── endpoints           # EndpointSpec, CrudOperations и *Endpoints
│   ├── skeleton            # generic contracts и requester implementations
│   │   ├── interfaces
│   │   ├── options         # ReadOptions и DeleteMode
│   │   ├── query           # QueryParams
│   │   └── requesters      # raw/successful requesters и RequesterFactory
│   └── steps               # ApiClient и предметные resource Steps
├── api/specs               # request/response specifications
├── api/testdata             # reference и runtime test data
├── api/utils                # генерация данных и сравнение моделей
└── common                   # общие extensions, retry и logging

src/test/java/
├── api/requests/skeleton   # изолированные проверки requester layer
└── api_tests/p0             # API-тесты ресурсов и auth
```

Позитивный сценарий обычно проходит по цепочке:

```text
Тест → ApiClient → *Steps → SuccessfulRequester → raw Requester → OpenMRS API
```

Сейчас через `ApiClient` доступны готовые Steps для пользователей и observations. Для негативных сценариев raw requester возвращает исходный Rest Assured `Response`, чтобы тест сам проверял status и body.

## С чего начать разработку

- Первый запуск и выбор requester-а: [`docs/guides/api-requester-quickstart.md`](docs/guides/api-requester-quickstart.md)
- Добавление нового ресурса: [`docs/guides/api-requester-resource-slice.md`](docs/guides/api-requester-resource-slice.md)
- Маппинг query params: [`docs/guides/api-requester-search-params.md`](docs/guides/api-requester-search-params.md)
- Слои requester architecture: [`docs/architecture/api-requester-layer.md`](docs/architecture/api-requester-layer.md)
- Рабочие примеры: [`docs/architecture/api-requester-examples.md`](docs/architecture/api-requester-examples.md)
- Scope и inventory OpenMRS API: [`docs/architecture/openmrs-api-inventory.md`](docs/architecture/openmrs-api-inventory.md)

Если для ресурса еще нет Steps и endpoint specs, начинайте с resource-slice guide, а не с ручной сборки requester-а внутри каждого теста.
