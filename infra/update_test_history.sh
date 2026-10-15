#!/usr/bin/env bash
# Maintain branch-scoped CI test history and calculate history-based flaky candidates.
set -euo pipefail

usage() {
  cat >&2 <<'USAGE'
Usage: update_test_history.sh --history FILE --test-run FILE --output FILE \
  --branch BRANCH --run-id ID --attempt N --sha SHA [--completed-at ISO8601]
USAGE
  exit 2
}

history='' test_run='' output='' branch='' run_id='' attempt='' sha='' completed_at=''
while (($#)); do
  (($# >= 2)) || usage
  case "$1" in
    --history) history=$2 ;;
    --test-run) test_run=$2 ;;
    --output) output=$2 ;;
    --branch) branch=$2 ;;
    --run-id) run_id=$2 ;;
    --attempt) attempt=$2 ;;
    --sha) sha=$2 ;;
    --completed-at) completed_at=$2 ;;
    *) usage ;;
  esac
  shift 2
done
for value in "$history" "$test_run" "$output" "$branch" "$run_id" "$attempt" "$sha"; do
  [[ -n "$value" ]] || usage
done
command -v jq >/dev/null || { echo 'ERROR: jq is required' >&2; exit 1; }
[[ -f "$test_run" ]] || { echo "ERROR: missing $test_run" >&2; exit 1; }
[[ -n "$completed_at" ]] || completed_at=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
mkdir -p "$(dirname "$history")" "$(dirname "$output")"

# Validate the source and historical schemas before modifying either file.
jq -e '
  type == "object" and (.tests | type == "array") and
  ([.tests[] | (.id | type == "string" and length > 0)] | all) and
  ([.tests[].id] | length == (unique | length))
' "$test_run" >/dev/null || { echo 'ERROR: invalid test-run.json (tests or duplicate IDs)' >&2; exit 1; }

if [[ -f "$history" ]]; then
  jq -e '
    type == "object" and .schemaVersion == 1 and (.runs | type == "array") and
    ([.runs[] | (.branch | type == "string" and length > 0) and (.tests | type == "object")] | all)
  ' "$history" >/dev/null || { echo 'ERROR: invalid test-history.json schema' >&2; exit 1; }
else
  printf '{"schemaVersion":1,"runs":[]}\n' > "$history"
fi

tmp_history=$(mktemp "${history}.tmp.XXXXXX")
tmp_metrics=$(mktemp "${output}.tmp.XXXXXX")
trap 'rm -f "$tmp_history" "$tmp_metrics"' EXIT

# Preserve up to 100 runs per branch. A rerun supersedes earlier attempts
# of the same workflow run on the same branch.
jq -n \
  --slurpfile old "$history" \
  --slurpfile current "$test_run" \
  --arg branch "$branch" \
  --arg runId "$run_id" \
  --arg attempt "$attempt" \
  --arg sha "$sha" \
  --arg completedAt "$completed_at" '
  ["PASSED", "FAILED", "BROKEN"] as $valid |
  ($current[0].tests | map(select(.status as $s | $valid | index($s) != null))
    | map({key: .id, value: .status}) | from_entries) as $tests |
  {workflowRunId:$runId, workflowRunAttempt:$attempt, branch:$branch,
   sourceSha:$sha, completedAt:$completedAt, tests:$tests} as $entry |
  (($old[0].runs | map(select(.branch != $branch or (.workflowRunId | tostring) != $runId))) + [$entry]
    | sort_by(.branch)
    | group_by(.branch)
    | map(sort_by((.completedAt // "" | tostring), (.workflowRunId // "" | tostring)) | .[-100:])
    | add
    | sort_by((.completedAt // "" | tostring), .branch)) as $runs |
  {schemaVersion:1, runs:$runs}
' > "$tmp_history"

jq -n \
  --slurpfile history "$tmp_history" \
  --arg branch "$branch" '
  ["PASSED", "FAILED", "BROKEN"] as $valid |
  ($history[0].runs | map(select(.branch == $branch)) | .[-10:]) as $recent |
  ([$recent[].tests | to_entries[] | select(.value as $s | $valid | index($s) != null)]
    | sort_by(.key) | group_by(.key)
    | map({id:.[0].key, statuses:map(.value)})
    | map(select(.statuses | length >= 3))) as $eligible |
  ([$eligible[] | select((.statuses | index("PASSED") != null) and
    ((.statuses | index("FAILED") != null) or (.statuses | index("BROKEN") != null))) | .id]
    | sort) as $candidates |
  {schemaVersion:1, method:"HISTORY_BASED", branch:$branch,
   windowRuns:($recent | length), windowLimit:10, minObservations:3,
   analyzedTests:($eligible | length), flakyCandidates:($candidates | length),
   flakyRate:(if ($eligible | length) == 0 then null
     else ((10000 * ($candidates | length) / ($eligible | length) | round) / 100) end),
   candidateTestIds:$candidates}
' > "$tmp_metrics"

mv "$tmp_history" "$history"
mv "$tmp_metrics" "$output"
jq -r '"Flaky candidates: \(.flakyCandidates) / \(.analyzedTests) eligible tests; rate=\(.flakyRate); branch=\(.branch)"' "$output"
