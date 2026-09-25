#!/usr/bin/env bash

set -euo pipefail

if [[ -f ".env" ]]; then
    set -a
    source .env
    set +a
fi

: "${OMRS_DB_NAME:?OMRS_DB_NAME is not set}"
: "${OMRS_DB_USER:?OMRS_DB_USER is not set}"
: "${OMRS_DB_HOST:?OMRS_DB_HOST is not set}"
: "${OMRS_DB_PASSWORD:?OMRS_DB_PASSWORD is not set}"
: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is not set}"
: "${OMRS_ENCRYPTION_KEY:?OMRS_ENCRYPTION_KEY is not set}"
: "${OMRS_ENCRYPTION_VECTOR:?OMRS_ENCRYPTION_VECTOR is not set}"

COMPOSE_FILE="infra/docker-compose.yaml"
DB_DUMP="infra/db/openmrs-test.sql"
BROWSERS_CONFIG="infra/config/browsers.json"

DB_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}"
DB_NAME="${OMRS_DB_NAME}"

MAX_DB_ATTEMPTS=60
MAX_BACKEND_ATTEMPTS=60

echo ">>> Создание openmrs-runtime.properties"

mkdir -p infra/openmrs-data

cat > infra/openmrs-data/openmrs-runtime.properties <<EOF
# Generated for CI

admin.password.locked=false
auto_update_database=true

connection.driver_class=com.mysql.jdbc.Driver
connection.password=${OMRS_DB_PASSWORD}
connection.url=jdbc\\:mysql\\://${OMRS_DB_HOST}\\:3306/${OMRS_DB_NAME}?autoReconnect\\=true&sessionVariables\\=default_storage_engine\\=InnoDB&useUnicode\\=true&characterEncoding\\=UTF-8
connection.username=${OMRS_DB_USER}

encryption.key=${OMRS_ENCRYPTION_KEY}
encryption.vector=${OMRS_ENCRYPTION_VECTOR}

hibernate.search.backend.analysis.configurer=luceneConfig
hibernate.search.backend.discovery.enabled=true
hibernate.search.backend.type=lucene
hibernate.search.backend.uris=http\\://es\\:9200

module.allow_web_admin=true
EOF

echo ">>> Остановка Docker Compose"

docker compose -f "$COMPOSE_FILE" down -v

echo ">>> Проверка необходимых файлов"

if [[ ! -f "$DB_DUMP" ]]; then
    echo ">>> ❌ Database dump not found: $DB_DUMP"
    exit 1
fi

if [[ ! -f "$BROWSERS_CONFIG" ]]; then
    echo ">>> ❌ Browsers config not found: $BROWSERS_CONFIG"
    exit 1
fi


echo ">>> Проверка jq"

if ! command -v jq &> /dev/null; then
    echo ">>> ❌ jq is not installed"
    exit 1
fi


echo ">>> Docker pull образов браузеров"

images=$(jq -r '.. | objects | select(.image) | .image' "$BROWSERS_CONFIG")

for image in $images; do
    echo ">>> Скачивание $image..."
    docker pull "$image"
done


echo ">>> Запуск MariaDB"

docker compose -f "$COMPOSE_FILE" up -d db


echo ">>> Ожидание MariaDB"

db_ready=false

for ((attempt=1; attempt<=MAX_DB_ATTEMPTS; attempt++)); do

    if docker compose -f "$COMPOSE_FILE" exec -T db \
        mariadb \
        -uroot \
        -p"$DB_ROOT_PASSWORD" \
        -e "SELECT 1;" \
        > /dev/null 2>&1
    then
        db_ready=true
        break
    fi

    echo ">>> MariaDB ещё не готова... ($attempt/$MAX_DB_ATTEMPTS)"
    sleep 2

done

if [[ "$db_ready" != "true" ]]; then
    echo ">>> ❌ MariaDB не запустилась за отведённое время"

    echo ">>> MariaDB logs:"
    docker compose -f "$COMPOSE_FILE" logs --tail=200 db

    exit 1
fi

echo ">>> ✅ MariaDB готова"


echo ">>> Восстановление OpenMRS DB snapshot"

docker compose -f "$COMPOSE_FILE" exec -T db \
    mariadb \
    --max-allowed-packet=256M \
    -uroot \
    -p"$DB_ROOT_PASSWORD" \
    "$DB_NAME" \
    < "$DB_DUMP"

echo ">>> ✅ Database восстановлена"


echo ">>> Запуск OpenMRS и остальных сервисов"

docker compose -f "$COMPOSE_FILE" up -d


echo ">>> Ожидание OpenMRS backend"

MAX_BACKEND_ATTEMPTS=60
backend_ready=false

for ((attempt=1; attempt<=MAX_BACKEND_ATTEMPTS; attempt++)); do

    container_id=$(docker compose -f "$COMPOSE_FILE" ps -q backend)

    if [[ -z "$container_id" ]]; then
        echo ">>> ❌ Backend container не найден"
        docker compose -f "$COMPOSE_FILE" ps
        exit 1
    fi

    health_status=$(docker inspect \
        --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}no-healthcheck{{end}}' \
        "$container_id")

    echo ">>> Backend status: $health_status ($attempt/$MAX_BACKEND_ATTEMPTS)"

    if [[ "$health_status" == "healthy" ]]; then
        backend_ready=true
        break
    fi

    if [[ "$health_status" == "unhealthy" ]]; then
        echo ">>> ⚠️ Backend пока unhealthy"
    fi

    sleep 5

done

if [[ "$backend_ready" != "true" ]]; then
    echo ">>> ❌ OpenMRS backend не запустился за отведённое время"

    echo ">>> Docker Compose status:"
    docker compose -f "$COMPOSE_FILE" ps

    echo ">>> Backend logs:"
#    docker compose -f "$COMPOSE_FILE" logs --tail=200 backend
    docker compose -f "$COMPOSE_FILE" logs backend

    exit 1
fi

echo ">>> ✅ OpenMRS backend готов"


echo ">>> Запущенные контейнеры"

docker compose -f "$COMPOSE_FILE" ps


echo ">>> ✅ Test environment готов"