#!/bin/bash

set -e

COVERAGE_FILE="swagger-coverage-results.json"
MIN_COVERAGE=50

echo ">>> Проверка Swagger API Coverage"

if [ ! -f "$COVERAGE_FILE" ]; then
    echo "❌ Coverage file not found: $COVERAGE_FILE"
    exit 1
fi

ALL=$(jq '.conditionCounter.all' "$COVERAGE_FILE")
COVERED=$(jq '.conditionCounter.covered' "$COVERAGE_FILE")

if [ "$ALL" -eq 0 ]; then
    echo "❌ Total number of conditions is 0"
    exit 1
fi

COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($COVERED / $ALL) * 100}")

echo ">>> Покрыто проверок: $COVERED"
echo ">>> Общее количество возможных проверок: $ALL"
echo ">>> API Coverage (%): $COVERAGE%"
echo ">>> Минимальное покрытие (%): $MIN_COVERAGE%"

if (( $(awk "BEGIN {print ($COVERAGE < $MIN_COVERAGE)}") )); then
    echo "❌ Quality Gate FAILED"
    echo "❌ API coverage ($COVERAGE%) is below than $MIN_COVERAGE%"
    exit 1
fi

echo "✅ Проверка Quality Gate пройдена"