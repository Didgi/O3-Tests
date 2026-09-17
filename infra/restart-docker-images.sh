#!/usr/bin/env bash

set -euo pipefail

COMPOSE_FILE="infra/docker-compose.yaml"
DB_DUMP="infra/db/openmrs-test.sql"

DB_USER="${OMRS_DB_USER:-openmrs}"
DB_PASSWORD="${OMRS_DB_PASSWORD:-openmrs}"
DB_NAME="openmrs"

echo ">>> Остановка Docker Compose"
docker compose -f "$COMPOSE_FILE" down -v

echo ">>> Проверка DB snapshot"

if [[ ! -f "$DB_DUMP" ]]; then
    echo ">>> ❌ Database dump not found: $DB_DUMP"
    exit 1
fi

echo ">>> Запуск MariaDB"
docker compose -f "$COMPOSE_FILE" up -d db

echo ">>> Ожидание MariaDB"

until docker compose -f "$COMPOSE_FILE" exec -T db \
    mariadb-admin ping \
    -u"$DB_USER" \
    -p"$DB_PASSWORD" \
    --silent
do
    echo ">>> MariaDB ещё не готова..."
    sleep 2
done

echo ">>> ✅ MariaDB готова"

echo ">>> Восстановление OpenMRS DB snapshot"

docker compose -f "$COMPOSE_FILE" exec -T db \
    mariadb \
    -u"$DB_USER" \
    -p"$DB_PASSWORD" \
    "$DB_NAME" \
    < "$DB_DUMP"

echo ">>> ✅ Database восстановлена"

echo ">>> Запуск OpenMRS и остальных сервисов"
docker compose -f "$COMPOSE_FILE" up -d

echo ">>> Ожидание OpenMRS backend"

MAX_ATTEMPTS=60

for ((attempt=1; attempt<=MAX_ATTEMPTS; attempt++)); do

    if docker compose -f "$COMPOSE_FILE" exec -T backend \
        curl -fsS http://localhost:8080/openmrs > /dev/null 2>&1
    then
        echo ">>> ✅ OpenMRS backend готов"
        break
    fi

    if [[ "$attempt" -eq "$MAX_ATTEMPTS" ]]; then
        echo ">>> ❌ OpenMRS backend не запустился"

        echo ">>> Backend logs:"
        docker compose -f "$COMPOSE_FILE" logs --tail=200 backend

        exit 1
    fi

    echo ">>> Backend ещё не готов... ($attempt/$MAX_ATTEMPTS)"
    sleep 5

done

echo ">>> Запущенные контейнеры"
docker compose -f "$COMPOSE_FILE" ps

echo ">>> ✅ Test environment готов"