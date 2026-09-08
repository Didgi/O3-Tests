#!/bin/bash

echo ">>> Остановка Docker Compose"

COMPOSE_FILE=infra/docker-compose.yaml

docker compose -f "$COMPOSE_FILE" down -v

#echo ">>> Docker pull всех образов браузеров"

#json_file="infra/config/browsers.json"
#if ! command -v jq &> /dev/null; then
#    echo ">>> ❌ jq is not installed. Please install jq and try again."
#    exit 1
#fi
#images=$(jq -r '.. | objects | select(.image) | .image' "$json_file")
#for image in $images; do
#    echo ">>> Скачивание $image..."
#    docker pull "$image"
#done

echo ">>> Запуск Docker Compose"

docker compose -f "$COMPOSE_FILE" up -d

echo ">>> Запущенные контейнеры"
docker compose -f "$COMPOSE_FILE" ps