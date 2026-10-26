#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
mkdir -p "$TMP/bin"
cat > "$TMP/bin/curl" <<'SH'
#!/usr/bin/env bash
set -euo pipefail
out=''
url=${*: -1}
while (($#)); do
  case "$1" in --output) out=$2; shift 2;; *) shift;; esac
done
if [[ "$url" == */graphql ]]; then
  cat > "$out" <<'JSON'
{"data":{"repository":{"pullRequests":{"pageInfo":{"hasNextPage":false,"endCursor":null},"nodes":[
{"number":1,"isDraft":false,"baseRefName":"main","reviewDecision":"REVIEW_REQUIRED","mergeStateStatus":"BLOCKED","latestReviews":{"nodes":[]}},
{"number":2,"isDraft":false,"baseRefName":"main","reviewDecision":"APPROVED","mergeStateStatus":"CLEAN","latestReviews":{"nodes":[{"state":"APPROVED"}]}},
{"number":3,"isDraft":false,"baseRefName":"main","reviewDecision":"APPROVED","mergeStateStatus":"BLOCKED","latestReviews":{"nodes":[{"state":"APPROVED"}]}},
{"number":4,"isDraft":true,"baseRefName":"main","reviewDecision":"REVIEW_REQUIRED","mergeStateStatus":"DRAFT","latestReviews":{"nodes":[]}},
{"number":5,"isDraft":false,"baseRefName":"main","reviewDecision":null,"mergeStateStatus":"CLEAN","latestReviews":{"nodes":[]}},
{"number":6,"isDraft":false,"baseRefName":"main","reviewDecision":null,"mergeStateStatus":"CLEAN","latestReviews":{"nodes":[{"state":"CHANGES_REQUESTED"}]}},
{"number":7,"isDraft":false,"baseRefName":"develop","reviewDecision":"APPROVED","mergeStateStatus":"CLEAN","latestReviews":{"nodes":[]}}
]}}}}
JSON
else
  case "$url" in
    */pulls\?*) printf '[]' > "$out";;
    *) echo "Unexpected REST URL $url" >&2; exit 1;;
  esac
fi
printf 200
SH
chmod +x "$TMP/bin/curl"
PATH="$TMP/bin:$PATH" GITHUB_TOKEN=test GITHUB_REPOSITORY=owner/repo \
  bash "$ROOT/infra/collect_pr_approval_metrics.sh" --output "$TMP/out.json" --base main >/dev/null
jq -e '.prsAwaitingApproval == 2 and .prsReadyToMerge == 1 and .averagePrApprovalMinutes == null' "$TMP/out.json" >/dev/null
echo "PR queue collector tests passed"
