#!/usr/bin/env bash
set -euo pipefail

# Usage: infra/update_history.sh HISTORY_JSON TEST_STEP_DURATION_JSON TEST_RUN_JSON
if [[ $# -ne 3 ]]; then
  echo "Usage: $0 HISTORY_JSON TEST_STEP_DURATION_JSON TEST_RUN_JSON" >&2
  exit 2
fi
command -v jq >/dev/null || { echo 'jq is required' >&2; exit 1; }
history_file=$1
duration_file=$2
test_run_file=$3
[[ -f "$test_run_file" ]] || { echo "Missing test run file: $test_run_file" >&2; exit 1; }
jq -e '.summary.passRate | type == "number" and . >= 0 and . <= 100' "$test_run_file" >/dev/null || { echo "Invalid test-run summary.passRate" >&2; exit 1; }
[[ -f "$duration_file" ]] || { echo "Missing duration file: $duration_file" >&2; exit 1; }
mkdir -p "$(dirname "$history_file")"

if [[ -f "$history_file" ]]; then
  jq -e 'type == "array"' "$history_file" >/dev/null || { echo 'History must be a JSON array' >&2; exit 1; }
else
  printf '[]\n' > "$history_file"
fi

jq -e '
  type == "object" and
  (.workflowRunId | type == "string" and length > 0) and
  (.workflowRunAttempt | type == "string" and length > 0) and
  (.completedAt | type == "string" and length > 0) and
  (.testRunDurationMs | type == "number" and . >= 0 and floor == .)
' "$duration_file" >/dev/null || { echo 'Invalid duration record' >&2; exit 1; }

# Add the last-run pass rate and branch without modifying legacy history records.
# Branch comes from the originating CI workflow (not the metrics workflow).
branch=${TEST_BRANCH:-}
[[ -n "$branch" ]] || { echo 'TEST_BRANCH is required' >&2; exit 1; }
item_tmp=$(mktemp "${history_file}.item.XXXXXX")
trap 'rm -f "${tmp:-}" "$item_tmp"' EXIT
jq -n --slurpfile duration "$duration_file" --slurpfile execution "$test_run_file" --arg branch "$branch" '
  $duration[0] + {branch: $branch, passRate: $execution[0].summary.passRate}
' > "$item_tmp"

tmp=$(mktemp "${history_file}.tmp.XXXXXX")
trap 'rm -f "$tmp" "$item_tmp"' EXIT
jq -s '
  .[0] as $history | .[1] as $item |
  ($history | map(select(
    ((.workflowRunId | tostring) != $item.workflowRunId) or
    ((.workflowRunAttempt | tostring) != $item.workflowRunAttempt)
  ))) + [$item]
  | sort_by(.completedAt)
  | .[-100:]
' "$history_file" "$item_tmp" > "$tmp"
mv "$tmp" "$history_file"
echo "History updated: $(jq 'length' "$history_file") CI runs"
