#!/usr/bin/env bash
# Calendar time from PR readiness to first human approval in the last 30 days.
set -euo pipefail
for cmd in jq curl date mktemp; do command -v "$cmd" >/dev/null || { echo "Missing dependency: $cmd" >&2; exit 1; }; done
usage() { echo "Usage: $0 --output FILE [--days 30] [--base BRANCH]" >&2; exit 2; }
output='' days=30 base=''
while (($#)); do
  (($# >= 2)) || usage
  case "$1" in --output) output=$2;; --days) days=$2;; --base) base=$2;; *) usage;; esac
  shift 2
done
[[ -n "$output" && "$days" =~ ^[1-9][0-9]*$ ]] || usage
: "${GITHUB_TOKEN:?GITHUB_TOKEN required}" ":" # token is only passed as header
: "${GITHUB_REPOSITORY:?GITHUB_REPOSITORY required}"
[[ "$GITHUB_REPOSITORY" =~ ^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$ ]] || { echo 'Invalid GITHUB_REPOSITORY' >&2; exit 1; }
api=${GITHUB_API_URL:-https://api.github.com}
graphql_api=${GITHUB_GRAPHQL_URL:-${api%/}/graphql}
owner=${GITHUB_REPOSITORY%%/*}
repo=${GITHUB_REPOSITORY#*/}
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
api_get() {
  local path=$1 dest=$2 status
  status=$(curl --silent --show-error --location --retry 3 --retry-delay 1 \
    --connect-timeout 10 --max-time 45 \
    --header "Authorization: Bearer $GITHUB_TOKEN" \
    --header 'Accept: application/vnd.github+json' \
    --header 'X-GitHub-Api-Version: 2022-11-28' \
    --header 'User-Agent: openmrs-qa-metrics' \
    --output "$dest" --write-out '%{http_code}' "$api$path")
  [[ "$status" == 200 ]] || { echo "GitHub API HTTP $status for $path" >&2; exit 1; }
  jq -e 'type == "array"' "$dest" >/dev/null || { echo "Expected GitHub API array for $path" >&2; exit 1; }
}
api_pages() {
  local path=$1 dest=$2 page count piece sep
  : > "$work/pages.ndjson"
  for ((page=1; page<=20; page++)); do
    sep='?'; [[ "$path" == *\?* ]] && sep='&'
    piece="$work/page-$page.json"
    api_get "$path${sep}per_page=100&page=$page" "$piece"
    jq -c '.[]' "$piece" >> "$work/pages.ndjson"
    count=$(jq 'length' "$piece")
    if ((count < 100)); then jq -s '.' "$work/pages.ndjson" > "$dest"; return; fi
  done
  echo "GitHub API pagination exceeded 20 pages: $path" >&2; exit 1
}
now=$(date -u +%s)
cutoff=$((now - days * 86400))
api_pages "/repos/$GITHUB_REPOSITORY/pulls?state=all&sort=updated&direction=desc" "$work/pulls.json"
: > "$work/durations.ndjson"
while IFS= read -r pr; do
  number=$(jq -r '.number' <<< "$pr")
  updated=$(jq -r '.updated_at' <<< "$pr")
  updated_epoch=$(date -u -d "$updated" +%s)
  # PRs are sorted by updated_at descending; older PRs cannot have recent approvals.
  ((updated_epoch >= cutoff)) || break
  if [[ -n "$base" ]] && [[ "$(jq -r '.base.ref' <<< "$pr")" != "$base" ]]; then continue; fi
  [[ "$(jq -r '.user.type // ""' <<< "$pr")" != Bot ]] || continue
  author=$(jq -r '.user.login // ""' <<< "$pr")
  api_pages "/repos/$GITHUB_REPOSITORY/pulls/$number/reviews" "$work/reviews.json"
  approval=$(jq -r --arg author "$author" '
    [.[] | select(.state == "APPROVED" and .submitted_at != null and .user.type != "Bot" and .user.login != $author)]
    | sort_by(.submitted_at) | .[0].submitted_at // empty' "$work/reviews.json")
  [[ -n "$approval" ]] || continue
  approved_epoch=$(date -u -d "$approval" +%s)
  ((approved_epoch >= cutoff && approved_epoch <= now)) || continue
  api_pages "/repos/$GITHUB_REPOSITORY/issues/$number/timeline" "$work/timeline.json"
  # Latest ready_for_review preceding the first approval. Draft with unresolved readiness is excluded.
  ready=$(jq -r --arg approval "$approval" '[.[] | select(.event == "ready_for_review" and .created_at <= $approval) | .created_at] | sort | last // empty' "$work/timeline.json")
  has_draft=$(jq -r --arg approval "$approval" '[.[] | select(.event == "convert_to_draft" and .created_at <= $approval)] | length' "$work/timeline.json")
  draft_now=$(jq -r '.draft // false' <<< "$pr")
  if [[ -n "$ready" ]]; then start=$ready
  elif [[ "$draft_now" == true || "$has_draft" != 0 ]]; then continue
  else start=$(jq -r '.created_at' <<< "$pr")
  fi
  start_epoch=$(date -u -d "$start" +%s)
  if ((start_epoch <= approved_epoch)); then
    jq -n --argjson minutes "$((approved_epoch - start_epoch))" '{seconds:$minutes}' >> "$work/durations.ndjson"
  fi
done < <(jq -c '.[]' "$work/pulls.json")
# Current PR queue: use GitHub's effective review decision and merge state,
# not the presence of any historical APPROVED review.
# CLEAN means GitHub considers the PR mergeable with required checks satisfied.
: > "$work/queue.ndjson"
cursor=null
for ((gpage=1; gpage<=100; gpage++)); do
  query='query($owner:String!,$repo:String!,$cursor:String){repository(owner:$owner,name:$repo){pullRequests(first:100,after:$cursor,states:OPEN,orderBy:{field:CREATED_AT,direction:DESC}){pageInfo{hasNextPage endCursor} nodes{number isDraft baseRefName reviewDecision mergeStateStatus latestReviews(first:100){nodes{state author{login}}}}}}}'
  payload=$(jq -nc --arg query "$query" --arg owner "$owner" --arg repo "$repo" --argjson cursor "$cursor" '{query:$query,variables:{owner:$owner,repo:$repo,cursor:$cursor}}')
  status=$(curl --silent --show-error --retry 3 --retry-delay 1 \
    --connect-timeout 10 --max-time 45 \
    --header "Authorization: Bearer $GITHUB_TOKEN" \
    --header 'Accept: application/vnd.github+json' \
    --header 'Content-Type: application/json' \
    --header 'User-Agent: openmrs-qa-metrics' \
    --data "$payload" \
    --output "$work/graphql.json" --write-out '%{http_code}' "$graphql_api")
  [[ "$status" == 200 ]] || { echo "GitHub GraphQL HTTP $status" >&2; exit 1; }
  jq -e '.errors == null and (.data.repository.pullRequests.nodes | type == "array")' "$work/graphql.json" >/dev/null || {
    echo "GitHub GraphQL returned errors or unexpected payload:" >&2
    jq -c '{errors, data}' "$work/graphql.json" >&2
    exit 1
  }
  jq -c --arg base "$base" '.data.repository.pullRequests.nodes[] | select(.isDraft == false and ($base == "" or .baseRefName == $base))' "$work/graphql.json" >> "$work/queue.ndjson"
  more=$(jq -r '.data.repository.pullRequests.pageInfo.hasNextPage' "$work/graphql.json")
  [[ "$more" == true ]] || break
  cursor=$(jq -c '.data.repository.pullRequests.pageInfo.endCursor' "$work/graphql.json")
  [[ "$cursor" != null ]] || { echo 'Missing GraphQL pagination cursor' >&2; exit 1; }
  if ((gpage == 100)); then echo 'GraphQL pagination exceeded 100 pages' >&2; exit 1; fi
done
jq -s '{
  prsAwaitingApproval: ([.[] | select(.reviewDecision == "REVIEW_REQUIRED" or (.reviewDecision == null and ([.latestReviews.nodes[]? | select(.state == "APPROVED" or .state == "CHANGES_REQUESTED")] | length) == 0))] | length),
  prsReadyToMerge: ([.[] | select(.reviewDecision == "APPROVED" and .mergeStateStatus == "CLEAN")] | length)
}' "$work/queue.ndjson" > "$work/queue-counts.json"
mkdir -p "$(dirname "$output")"
# jq -s handles an empty NDJSON file as [].
jq -s --slurpfile queue "$work/queue-counts.json" --arg at "$(date -u -d "@$now" +%Y-%m-%dT%H:%M:%SZ)" --argjson days "$days" '
  {schemaVersion:1,windowDays:$days,
   averagePrApprovalMinutes:(if length > 0 then (((map(.seconds) | add) / length / 60) * 100 | round / 100) else null end),
   prsAwaitingApproval:$queue[0].prsAwaitingApproval,prsReadyToMerge:$queue[0].prsReadyToMerge,generatedAt:$at}
' "$work/durations.ndjson" > "$output"
cat "$output"
