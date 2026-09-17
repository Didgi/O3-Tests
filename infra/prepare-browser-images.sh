#!/bin/bash

echo ">>> Docker pull всех образов браузеров"

json_file="infra/config/browsers.json"
if ! command -v jq &> /dev/null; then
    echo ">>> ❌ jq is not installed. Please install jq and try again."
    exit 1
fi
images=$(jq -r '.. | objects | select(.image) | .image' "$json_file")
for image in $images; do
    echo ">>> Скачивание $image..."
    docker pull "$image"
done

echo ">>> Все образы браузеров скачены"