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

if ! [[ "$ALL" =~ ^[0-9]+$ && "$COVERED" =~ ^[0-9]+$ ]] || [ "$COVERED" -gt "$ALL" ]; then
    echo "❌ Invalid Swagger coverage counters"
    exit 1
fi

if [ "$ALL" -eq 0 ]; then
    echo "❌ Total number of conditions is 0"
    exit 1
fi

COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($COVERED / $ALL) * 100}")

echo ">>> Покрыто проверок: $COVERED"
echo ">>> Общее количество возможных проверок: $ALL"
echo ">>> API Coverage (%): $COVERAGE%"
echo ">>> Цель для минимального покрытия на будущее (%): $MIN_COVERAGE%"

echo "✅ Проверка Swagger API Coverage пройдена"
# Machine-readable metrics for the separate Test Metrics workflow.
mkdir -p target/metrics
jq -n \
  --argjson covered "$COVERED" \
  --argjson all "$ALL" \
  --argjson coveragePercent "$COVERAGE" \
  --argjson minCoveragePercent "$MIN_COVERAGE" \
  '{covered: $covered, all: $all, coveragePercent: $coveragePercent, minCoveragePercent: $minCoveragePercent}' \
  > target/metrics/api-coverage.json
