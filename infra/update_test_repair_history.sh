#!/usr/bin/env bash
# Track @Disabled("BUG: ...") -> enabled + PASSED using persistent CI history.
set -euo pipefail
for cmd in jq date mktemp; do command -v "$cmd" >/dev/null || { echo "Missing dependency: $cmd" >&2; exit 1; }; done
usage() { echo "Usage: $0 --history FILE --inventory FILE --test-run FILE --output FILE --branch NAME --run-id ID --attempt N --completed-at ISO8601" >&2; exit 2; }
history='' inventory='' test_run='' output='' branch='' run_id='' attempt='' completed_at=''
while (($#)); do
  (($# >= 2)) || usage
  case "$1" in
    --history) history=$2;; --inventory) inventory=$2;; --test-run) test_run=$2;;
    --output) output=$2;; --branch) branch=$2;; --run-id) run_id=$2;;
    --attempt) attempt=$2;; --completed-at) completed_at=$2;; *) usage;;
  esac
  shift 2
done
for v in "$history" "$inventory" "$test_run" "$output" "$branch" "$run_id" "$attempt" "$completed_at"; do [[ -n "$v" ]] || usage; done
[[ -f "$inventory" && -f "$test_run" ]] || { echo 'Inventory and test-run JSON are required' >&2; exit 1; }
# Ubuntu GNU date; reject missing/invalid timezone by requiring explicit Z or offset.
[[ "$completed_at" =~ (Z|[+-][0-9]{2}:[0-9]{2})$ ]] || { echo 'completed-at must have timezone' >&2; exit 1; }
at_epoch=$(date -u -d "$completed_at" +%s)
at_iso=$(date -u -d "@$at_epoch" +%Y-%m-%dT%H:%M:%SZ)
mkdir -p "$(dirname "$history")" "$(dirname "$output")"
[[ -f "$history" ]] || printf '{"schemaVersion":1,"cycles":[],"processedRuns":[]}\n' > "$history"
run_key="$run_id/$attempt"
# Use a single jq transformation so a retried CI run is idempotent.
tmp_history=$(mktemp "$(dirname "$history")/.repair-history.XXXXXX")
tmp_metrics=$(mktemp "$(dirname "$output")/.repair-metrics.XXXXXX")
trap 'rm -f "$tmp_history" "$tmp_metrics"' EXIT
jq -n \
  --slurpfile hist "$history" --slurpfile inv "$inventory" --slurpfile execution "$test_run" \
  --arg branch "$branch" --arg runKey "$run_key" --arg at "$at_iso" --argjson now "$at_epoch" '
  def bug($t): ($t.disabled == true) and (($t.disabledReason // "") | type == "string" and (ascii_upcase | startswith("BUG:")));
  def iso_epoch: fromdateiso8601;
  ($hist[0]) as $h | ($inv[0]) as $inventory | ($execution[0]) as $run |
  if (($h.cycles // []) | type) != "array" or ($inventory.tests | type) != "array" or ($run.tests | type) != "array"
  then error("Invalid repair history, inventory or test-run schema") else
  ($h + {cycles: ($h.cycles // []), processedRuns: ($h.processedRuns // [])}) as $base |
  if ($base.processedRuns | index($runKey)) != null then $base
  else
    (reduce $inventory.tests[] as $t ($base.cycles;
      ($t.id) as $id |
      (map(select(.testId == $id and .branch == $branch and .status == "OPEN")) | length) as $openCount |
      if bug($t) and $openCount == 0 then
        . + [{testId:$id,reason:($t.disabledReason | sub("^[Bb][Uu][Gg]:"; "") | ltrimstr(" ") | rtrimstr(" ")),status:"OPEN",detectedAt:$at,detectedRun:$runKey,branch:$branch}]
      elif (bug($t) | not) and $openCount > 0 and $t.disabled == false and
           (any($run.tests[]; .id == $id and .status == "PASSED")) then
        map(if .testId == $id and .branch == $branch and .status == "OPEN" and ($now >= (.detectedAt | iso_epoch))
            then . + {status:"REPAIRED",repairedAt:$at,repairedRun:$runKey,repairDurationMinutes:((($now - (.detectedAt | iso_epoch)) / 60) * 100 | round / 100)}
            else . end)
      else . end
    )) as $cycles |
    $base + {cycles:$cycles,processedRuns:(($base.processedRuns + [$runKey]) | .[-1000:])}
  end end
' > "$tmp_history"

jq --arg branch "$branch" --arg at "$at_iso" --argjson now "$at_epoch" '
  [.cycles[] | select(.branch == $branch and .status == "REPAIRED" and ((.repairedAt | fromdateiso8601) >= ($now - 30 * 86400)))] as $repaired |
  [.cycles[] | select(.branch == $branch and .status == "OPEN")] as $opened |
  {schemaVersion:1,branch:$branch,windowDays:30,
   averageTestRepairMinutes:(if ($repaired | length) > 0 then ((($repaired | map(.repairDurationMinutes) | add) / ($repaired | length)) * 100 | round / 100) else null end),
   repairedTests:($repaired | length),testsUnderRepair:($opened | length),generatedAt:$at}
' "$tmp_history" > "$tmp_metrics"
# Write metrics first; history last to avoid persisting processedRun without metrics.
mv "$tmp_metrics" "$output"
mv "$tmp_history" "$history"
cat "$output"
