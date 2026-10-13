#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
cat > "$TMP/inventory-bug.json" <<'JSON'
{"tests":[{"id":"ui.PatientTest#shouldCreatePatient()","disabled":true,"disabledReason":"BUG: locator"},{"id":"ui.OtherTest#test()","disabled":true,"disabledReason":"Feature not implemented"}]}
JSON
cat > "$TMP/inventory-fixed.json" <<'JSON'
{"tests":[{"id":"ui.PatientTest#shouldCreatePatient()","disabled":false,"disabledReason":null},{"id":"ui.OtherTest#test()","disabled":true,"disabledReason":"Feature not implemented"}]}
JSON
cat > "$TMP/test-run-failed.json" <<'JSON'
{"tests":[{"id":"ui.PatientTest#shouldCreatePatient()","status":"FAILED"}]}
JSON
cat > "$TMP/test-run-passed.json" <<'JSON'
{"tests":[{"id":"ui.PatientTest#shouldCreatePatient()","status":"PASSED"}]}
JSON
repair() {
  bash "$ROOT/infra/update_test_repair_history.sh" \
    --history "$TMP/history.json" --inventory "$1" --test-run "$2" \
    --output "$TMP/metrics.json" --branch main --run-id "$3" --attempt 1 --completed-at "$4" >/dev/null
}
repair "$TMP/inventory-bug.json" "$TMP/test-run-failed.json" 1 '2026-10-15T08:00:00Z'
jq -e '.testsUnderRepair == 1 and .repairedTests == 0' "$TMP/metrics.json" >/dev/null
repair "$TMP/inventory-bug.json" "$TMP/test-run-failed.json" 1 '2026-10-15T09:00:00Z'
jq -e '(.cycles | length) == 1' "$TMP/history.json" >/dev/null
repair "$TMP/inventory-fixed.json" "$TMP/test-run-failed.json" 2 '2026-10-15T09:00:00Z'
jq -e '.testsUnderRepair == 1' "$TMP/metrics.json" >/dev/null
repair "$TMP/inventory-fixed.json" "$TMP/test-run-passed.json" 3 '2026-10-15T10:30:00Z'
jq -e '.testsUnderRepair == 0 and .repairedTests == 1 and .averageTestRepairMinutes == 150' "$TMP/metrics.json" >/dev/null
repair "$TMP/inventory-bug.json" "$TMP/test-run-failed.json" 4 '2026-10-15T11:00:00Z'
jq -e '.testsUnderRepair == 1 and (.cycles | length) == 2' "$TMP/history.json" >/dev/null 2>&1 || jq -e '([.cycles[] | select(.status == "OPEN")] | length) == 1 and (.cycles | length) == 2' "$TMP/history.json" >/dev/null
# PR API fixture via a curl shim: one approved PR with no draft history.
mkdir -p "$TMP/bin"
cat > "$TMP/bin/curl" <<'SH'
#!/usr/bin/env bash
url=${*: -1}
output=''
while (($#)); do if [[ "$1" == '--output' ]]; then output=$2; shift 2; else shift; fi; done
case "$url" in
  */pulls\?*) printf '[{"number":42,"updated_at":"2026-10-15T11:00:00Z","created_at":"2026-10-15T08:00:00Z","user":{"login":"alice","type":"User"},"base":{"ref":"main"},"draft":false}]' > "$output";;
  */pulls/42/reviews*) printf '[{"state":"APPROVED","submitted_at":"2026-10-15T10:00:00Z","user":{"login":"bob","type":"User"}}]' > "$output";;
  */issues/42/timeline*) printf '[]' > "$output";;
  *) echo "Unknown mocked endpoint: $url" >&2; exit 1;;
esac
printf 200
SH
chmod +x "$TMP/bin/curl"
PATH="$TMP/bin:$PATH" GITHUB_TOKEN=dummy GITHUB_REPOSITORY=owner/repo GITHUB_API_URL=https://api.github.com \
  bash "$ROOT/infra/collect_pr_approval_metrics.sh" --output "$TMP/pr.json" --days 30 --base main >/dev/null
jq -e '.approvedPrs == 1 and .averagePrApprovalMinutes == 120' "$TMP/pr.json" >/dev/null
echo 'All Bash collector tests passed.'
