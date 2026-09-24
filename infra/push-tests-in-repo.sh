#!/bin/bash

set -e

echo ">>> Получен хэш коммита: $COMMIT_HASH"

echo ">>> Авторизация в docker"
echo ">>> Логин получен из секрета: $DOCKER_USERNAME"
echo ">>> Токен получен из секрета: $DOCKER_TOKEN"
echo "$DOCKER_TOKEN" | docker login -u $DOCKER_USERNAME --password-stdin

IMAGE_NAME=O3-tests
DOCKER_IMAGE="${DOCKER_USERNAME}/${IMAGE_NAME}:${COMMIT_HASH::7}"

echo ">>> Старт сборки докер образа"
docker build -t ${DOCKER_IMAGE} .

echo ">>> Собран докер образ с именем: ${DOCKER_IMAGE}"
echo ">>> Старт публикации образа в dockerhub"

docker push ${DOCKER_IMAGE};

echo ">>> Образ с именем: ${DOCKER_IMAGE} опубликован в dockerhub"
echo ">>> Скачать данный образ можно выполнив команду: docker pull ${DOCKER_IMAGE}"