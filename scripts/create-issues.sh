#!/usr/bin/env bash
# Creates the mid-term GitHub issues from docs/planning/midterm-tickets.md.
#
#   ./scripts/create-issues.sh                 dry-run (default): only prints what it would do
#   ./scripts/create-issues.sh --run           really creates labels, milestone, board, issues
#   ./scripts/create-issues.sh --show-body T-01  prints the generated issue body of one ticket
#
# Labels: good-first-ticket is set only on 1-2 point tickets with "Depends on: none".
# Idempotent: everything that already exists is skipped, so it is safe to re-run after a
# failure. Needs: gh (logged in, scopes repo + project), jq, python3.
# Works with the macOS default bash 3.2 (no associative arrays).

set -euo pipefail

# ---- fixed targets (CLAUDE.md safety rules: only this repo and owner) ------------------
readonly REPO="PatriciuMina/GenAI-Project"
readonly OWNER="PatriciuMina"
readonly MILESTONE="Mid-term"
readonly PROJECT_TITLE="UBB Events Board"
readonly OLD_PROJECT_TITLE="GenAI Pro"          # existing empty board that gets renamed
readonly POINTS_FIELD="Points"
readonly EXPECTED_TICKETS=99
readonly BLOB_BASE="https://github.com/${REPO}/blob/main"

SLEEP_CREATE="${SLEEP_CREATE:-3}"   # seconds after each issue creation
SLEEP_WRITE="${SLEEP_WRITE:-1}"     # seconds after every other write call

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TICKETS_FILE="${ROOT_DIR}/docs/planning/midterm-tickets.md"

MODE="dry-run"
SHOW_BODY=""

usage() {
  cat <<EOF
Usage: $(basename "$0") [--dry-run | --run | --show-body T-nn | -h]

  --dry-run          (default) read GitHub state and print what would be created
  --run              really create labels, milestone "${MILESTONE}", project "${PROJECT_TITLE}"
                     and the issues T-01..T-${EXPECTED_TICKETS} in ${REPO}
  --show-body T-nn   print the issue body generated for one ticket and exit (offline)
  -h, --help         show this help

Environment: SLEEP_CREATE (default 3) and SLEEP_WRITE (default 1) seconds of pacing.
EOF
}

while [ $# -gt 0 ]; do
  case "$1" in
    --dry-run) MODE="dry-run" ;;
    --run) MODE="run" ;;
    --show-body)
      [ $# -ge 2 ] || { usage >&2; exit 2; }
      SHOW_BODY="$2"; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage >&2; exit 2 ;;
  esac
  shift
done

# ---- helpers ---------------------------------------------------------------------------
log()  { printf '%s\n' "$*"; }
warn() { printf 'WARNING: %s\n' "$*" >&2; }
die()  {
  printf '\nERROR: %s\n' "$*" >&2
  printf 'Nothing after this step was done. Fix the cause and re-run with --run;\n' >&2
  printf 'everything that already exists will be skipped.\n' >&2
  exit 1
}

for tool in jq python3; do
  command -v "$tool" >/dev/null 2>&1 || die "'$tool' is not installed."
done

WORK_DIR="$(mktemp -d "${TMPDIR:-/tmp}/create-issues.XXXXXX")"
trap 'rm -rf "$WORK_DIR"' EXIT

# Runs a read-only gh call; stops with a clear message on failure.
read_call() {
  local ctx="$1"; shift
  local out
  if ! out="$("$@" 2>"$WORK_DIR/err")"; then
    die "${ctx} failed: $(cat "$WORK_DIR/err")"
  fi
  printf '%s' "$out"
}

# Runs a write call (only in --run mode). Output goes to $WRITE_OUT.
# Retries once after 60 s on GitHub's secondary rate limit; any other error stops.
WRITE_OUT=""
write_call() {
  local ctx="$1" pause="$2"; shift 2
  WRITE_OUT=""
  if [ "$MODE" != "run" ]; then
    return 0
  fi
  local attempt
  for attempt in 1 2; do
    if WRITE_OUT="$("$@" 2>"$WORK_DIR/err")"; then
      sleep "$pause"
      return 0
    fi
    if [ "$attempt" = 1 ] && grep -qiE 'secondary rate limit|abuse|was submitted too quickly' "$WORK_DIR/err"; then
      warn "${ctx}: GitHub secondary rate limit hit, waiting 60 s and retrying once..."
      sleep 60
      continue
    fi
    break
  done
  die "${ctx} failed: $(cat "$WORK_DIR/err")"
}

# ---- 0. parse the ticket file ----------------------------------------------------------
[ -f "$TICKETS_FILE" ] || die "Ticket file not found: $TICKETS_FILE"

cat >"$WORK_DIR/parse.py" <<'PY'
import json, posixpath, re, sys

src, out, blob_base, expected = sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4])
SRC_DIR = "docs/planning"
HEADER = ("> Mid-term ticket from [`docs/planning/midterm-tickets.md`]"
          f"({blob_base}/docs/planning/midterm-tickets.md). Read the "
          f"[shared conventions]({blob_base}/docs/planning/midterm-tickets.md"
          "#shared-conventions-for-all-code-tickets) first.")

def fix_link(m):
    text, target = m.group(1), m.group(2)
    if re.match(r"^[a-z]+:", target) or target.startswith("#") or target.startswith("/"):
        return m.group(0)
    path, sep, anchor = target.partition("#")
    resolved = posixpath.normpath(posixpath.join(SRC_DIR, path))
    if resolved.startswith(".."):
        sys.exit(f"link escapes the repository: {target}")
    return f"[{text}]({blob_base}/{resolved}{sep}{anchor})"

lines = open(src, encoding="utf-8").read().split("\n")
tickets, cur, in_fence = [], None, False
for line in lines:
    if line.lstrip().startswith("```"):
        in_fence = not in_fence
    if not in_fence:
        m = re.match(r"^## (T-\d{2}): (.+?)\s*$", line)
        if m:
            cur = {"id": m.group(1), "title": m.group(2), "lines": []}
            tickets.append(cur)
            continue
        if re.match(r"^#{1,2} ", line):
            cur = None
            continue
    if cur is not None:
        cur["lines"].append(line)

errors, result = [], []
ids = [t["id"] for t in tickets]
if ids != [f"T-{i:02d}" for i in range(1, expected + 1)]:
    errors.append(f"expected T-01..T-{expected:02d} in order, found {len(ids)} tickets")
for t in tickets:
    body_lines = t["lines"]
    while body_lines and body_lines[-1].strip() in ("", "---"):
        body_lines.pop()
    while body_lines and not body_lines[0].strip():
        body_lines.pop(0)
    meta = {}
    for l in body_lines[:6]:
        mm = re.match(r"^- (Points|Labels|Depends on): (.*)$", l)
        if mm:
            meta[mm.group(1)] = mm.group(2).strip()
    missing = {"Points", "Labels", "Depends on"} - meta.keys()
    if missing:
        errors.append(f"{t['id']}: missing {', '.join(sorted(missing))}")
        continue
    if not re.fullmatch(r"[1-5]", meta["Points"]):
        errors.append(f"{t['id']}: invalid points '{meta['Points']}'")
    labels = [x.strip() for x in meta["Labels"].split(",") if x.strip()]
    deps = [] if meta["Depends on"] == "none" else [x.strip() for x in meta["Depends on"].split(",")]
    # Rule: good-first-ticket only on 1-2 point tickets with "Depends on: none",
    # whatever the Labels line says (the label is added or removed here).
    labels = [x for x in labels if x != "good-first-ticket"]
    if meta["Points"] in ("1", "2") and not deps:
        labels.append("good-first-ticket")
    for d in deps:
        if d not in ids:
            errors.append(f"{t['id']}: unknown dependency {d}")
    body = "\n".join(body_lines)
    body = re.sub(r"\[([^\]]*)\]\(([^)\s]+)\)", fix_link, body)
    result.append({"id": t["id"], "title": f"[{t['id']}] {t['title']}",
                   "points": int(meta["Points"]), "labels": labels, "deps": deps,
                   "body": HEADER + "\n\n" + body + "\n"})
if errors:
    sys.exit("ticket file problems:\n  " + "\n  ".join(errors))
json.dump(result, open(out, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
PY

TICKETS_JSON="$WORK_DIR/tickets.json"
if ! python3 -I "$WORK_DIR/parse.py" "$TICKETS_FILE" "$TICKETS_JSON" "$BLOB_BASE" "$EXPECTED_TICKETS" 2>"$WORK_DIR/err"; then
  die "Could not parse $TICKETS_FILE: $(cat "$WORK_DIR/err")"
fi
TICKET_COUNT="$(jq length "$TICKETS_JSON")"

if [ -n "$SHOW_BODY" ]; then
  jq -er --arg id "$SHOW_BODY" '.[] | select(.id == $id) | "TITLE:  \(.title)\nLABELS: \(.labels + ["points:\(.points)"] | join(", "))\n\n\(.body)"' \
    "$TICKETS_JSON" || die "Ticket $SHOW_BODY not found."
  exit 0
fi

command -v gh >/dev/null 2>&1 || die "'gh' (GitHub CLI) is not installed."
gh auth status >/dev/null 2>&1 || die "gh auth status failed (not logged in, or a network problem). Check: gh auth status; log in with: gh auth login (scopes: repo, project)."

log "== Mode: ${MODE}  (repo ${REPO}, owner ${OWNER})"
[ "$MODE" = "run" ] || log "== Dry-run: nothing will be changed. Use --run to really create everything."
log "== Parsed ${TICKET_COUNT} tickets from docs/planning/midterm-tickets.md"

# counters
labels_created=0; labels_skipped=0
milestone_state="skipped"; project_state="skipped"
issues_created=0; issues_skipped=0
items_added=0; items_skipped=0; fields_set=0
bodies_updated=0; bodies_skipped=0
deps_created=0; deps_skipped=0; native_deps="enabled"

# ---- 1. labels -------------------------------------------------------------------------
# name|color|description
label_meta() {
  case "$1" in
    type:feature)      echo "1d76db|New functionality" ;;
    type:test)         echo "0e8a16|Automated tests" ;;
    type:design)       echo "d4c5f9|Visual / UX design" ;;
    type:diagram)      echo "c5def5|Diagrams (UML, Mermaid)" ;;
    type:research)     echo "fbca04|User research and interviews" ;;
    type:docs)         echo "0075ca|Documentation" ;;
    type:report)       echo "bfdadc|Mid-term report sections" ;;
    type:qa)           echo "5319e7|Manual testing / QA" ;;
    type:bug)          echo "d73a4a|Something is broken" ;;
    area:backend)      echo "006b75|Spring Boot backend" ;;
    area:frontend)     echo "e99695|React frontend" ;;
    area:docs)         echo "c2e0c6|Files under /docs" ;;
    workflow:W1)       echo "f9d0c4|W1 Organizations and membership" ;;
    workflow:W2)       echo "f9d0c4|W2 Event creation and lifecycle" ;;
    workflow:W3)       echo "f9d0c4|W3 Event discovery and search" ;;
    workflow:W4)       echo "f9d0c4|W4 Participant registration" ;;
    workflow:W5)       echo "f9d0c4|W5 Event organization" ;;
    points:1)          echo "fef2c0|1 story point (under 2 h)" ;;
    points:2)          echo "fbe08a|2 story points" ;;
    points:3)          echo "f9c74f|3 story points" ;;
    points:4)          echo "f4a259|4 story points" ;;
    points:5)          echo "e76f51|5 story points (several days)" ;;
    good-first-ticket) echo "7057ff|1-2 points and no dependencies: can start right away" ;;
    no-genai)          echo "b60205|Must be done without GenAI tools" ;;
    blocked)           echo "000000|Waiting on another ticket or decision" ;;
    *)                 echo "ededed|Label used in the mid-term tickets" ;;
  esac
}

log ""
log "== 1. Labels"
REQUIRED_LABELS="type:feature type:test type:design type:diagram type:research type:docs type:report type:qa type:bug area:backend area:frontend area:docs workflow:W1 workflow:W2 workflow:W3 workflow:W4 workflow:W5 points:1 points:2 points:3 points:4 points:5 good-first-ticket no-genai blocked"
ALL_LABELS="$( { printf '%s\n' $REQUIRED_LABELS; jq -r '.[].labels[], (.[] | "points:\(.points)")' "$TICKETS_JSON"; } | awk '!seen[$0]++')"
EXISTING_LABELS="$(read_call "Listing labels" gh label list --repo "$REPO" --limit 500 --json name --jq '.[].name')"

while IFS= read -r label; do
  [ -n "$label" ] || continue
  if printf '%s\n' "$EXISTING_LABELS" | grep -qxF "$label"; then
    labels_skipped=$((labels_skipped + 1))
    continue
  fi
  meta="$(label_meta "$label")"
  color="${meta%%|*}"; desc="${meta#*|}"
  log "  $([ "$MODE" = run ] && echo CREATE || echo 'WOULD CREATE') label ${label} (#${color}) - ${desc}"
  write_call "Creating label ${label}" "$SLEEP_WRITE" \
    gh label create "$label" --repo "$REPO" --color "$color" --description "$desc"
  labels_created=$((labels_created + 1))
done <<EOF
$ALL_LABELS
EOF
log "  labels: ${labels_created} to create/created, ${labels_skipped} already exist"

# ---- 2. milestone ----------------------------------------------------------------------
log ""
log "== 2. Milestone \"${MILESTONE}\""
MS_EXISTS="$(read_call "Listing milestones" gh api "repos/${REPO}/milestones?state=all&per_page=100" \
  --jq "[.[] | select(.title == \"${MILESTONE}\")] | length")"
if [ "$MS_EXISTS" -gt 0 ]; then
  log "  SKIP milestone exists"
else
  log "  $([ "$MODE" = run ] && echo CREATE || echo 'WOULD CREATE') milestone \"${MILESTONE}\""
  write_call "Creating milestone" "$SLEEP_WRITE" \
    gh api --method POST "repos/${REPO}/milestones" -f title="$MILESTONE" \
      -f description="All tickets of the mid-term deliverable (docs/planning/midterm-tickets.md)"
  milestone_state="created"
fi

# ---- 3. project ------------------------------------------------------------------------
log ""
log "== 3. Project \"${PROJECT_TITLE}\" (owner ${OWNER})"
PROJECTS="$(read_call "Listing projects" gh project list --owner "$OWNER" --closed --limit 100 --format json)"
PROJECT_NUMBER="$(printf '%s' "$PROJECTS" | jq -r --arg t "$PROJECT_TITLE" '[.projects[] | select(.title == $t)][0].number // empty')"
if [ -n "$PROJECT_NUMBER" ]; then
  log "  SKIP project exists (#${PROJECT_NUMBER})"
else
  OLD_NUMBER="$(printf '%s' "$PROJECTS" | jq -r --arg t "$OLD_PROJECT_TITLE" '[.projects[] | select(.title == $t)][0].number // empty')"
  if [ -n "$OLD_NUMBER" ]; then
    log "  $([ "$MODE" = run ] && echo RENAME || echo 'WOULD RENAME') project #${OLD_NUMBER} \"${OLD_PROJECT_TITLE}\" -> \"${PROJECT_TITLE}\""
    write_call "Renaming project" "$SLEEP_WRITE" \
      gh project edit "$OLD_NUMBER" --owner "$OWNER" --title "$PROJECT_TITLE"
    PROJECT_NUMBER="$OLD_NUMBER"
    project_state="renamed"
  else
    log "  $([ "$MODE" = run ] && echo CREATE || echo 'WOULD CREATE') project \"${PROJECT_TITLE}\""
    write_call "Creating project" "$SLEEP_WRITE" \
      gh project create --owner "$OWNER" --title "$PROJECT_TITLE" --format json
    project_state="created"
    if [ "$MODE" = run ]; then
      PROJECT_NUMBER="$(printf '%s' "$WRITE_OUT" | jq -r '.number')"
    fi
  fi
fi

PROJECT_ID=""; PROJECT_URL="(not created in dry-run)"
POINTS_FIELD_ID=""; STATUS_FIELD_ID=""; TODO_OPTION_ID=""

load_fields() {
  local fields
  fields="$(read_call "Listing project fields" gh project field-list "$PROJECT_NUMBER" --owner "$OWNER" --limit 100 --format json)"
  POINTS_FIELD_ID="$(printf '%s' "$fields" | jq -r --arg n "$POINTS_FIELD" '[.fields[] | select(.name == $n)][0].id // empty')"
  STATUS_FIELD_ID="$(printf '%s' "$fields" | jq -r '[.fields[] | select(.name == "Status")][0].id // empty')"
  TODO_OPTION_ID="$(printf '%s' "$fields" | jq -r '[.fields[] | select(.name == "Status")][0].options // [] | map(select(.name == "Todo"))[0].id // empty')"
}

if [ -n "$PROJECT_NUMBER" ]; then
  PROJECT_VIEW="$(read_call "Reading project" gh project view "$PROJECT_NUMBER" --owner "$OWNER" --format json)"
  PROJECT_ID="$(printf '%s' "$PROJECT_VIEW" | jq -r '.id')"
  PROJECT_URL="$(printf '%s' "$PROJECT_VIEW" | jq -r '.url')"

  LINKED="$(read_call "Checking project repositories" gh api graphql \
    -f query='query($login:String!,$n:Int!){user(login:$login){projectV2(number:$n){repositories(first:100){nodes{nameWithOwner}}}}}' \
    -f login="$OWNER" -F n="$PROJECT_NUMBER" \
    --jq "[.data.user.projectV2.repositories.nodes[] | select(.nameWithOwner == \"${REPO}\")] | length")"
  if [ "$LINKED" -gt 0 ]; then
    log "  SKIP project already linked to ${REPO}"
  else
    log "  $([ "$MODE" = run ] && echo LINK || echo 'WOULD LINK') project to ${REPO}"
    write_call "Linking project to repo" "$SLEEP_WRITE" \
      gh project link "$PROJECT_NUMBER" --owner "$OWNER" --repo "$REPO"
  fi

  load_fields
  if [ -n "$POINTS_FIELD_ID" ]; then
    log "  SKIP field \"${POINTS_FIELD}\" exists"
  else
    log "  $([ "$MODE" = run ] && echo CREATE || echo 'WOULD CREATE') number field \"${POINTS_FIELD}\""
    write_call "Creating Points field" "$SLEEP_WRITE" \
      gh project field-create "$PROJECT_NUMBER" --owner "$OWNER" --name "$POINTS_FIELD" --data-type NUMBER
  fi

  if [ -z "$STATUS_FIELD_ID" ]; then
    die "Project #${PROJECT_NUMBER} has no Status field."
  elif [ -n "$TODO_OPTION_ID" ]; then
    log "  SKIP Status already has a \"Todo\" option"
  else
    log "  $([ "$MODE" = run ] && echo SET || echo 'WOULD SET') Status options to Todo / In Progress / In Review / Done (CONTRIBUTING.md §1)"
    write_call "Setting Status options" "$SLEEP_WRITE" \
      gh api graphql -f fieldId="$STATUS_FIELD_ID" -f query='
mutation($fieldId:ID!){updateProjectV2Field(input:{fieldId:$fieldId, singleSelectOptions:[
  {name:"Todo", color:GRAY, description:"Ready to pick (check dependencies)"},
  {name:"In Progress", color:YELLOW, description:"Someone is working on it"},
  {name:"In Review", color:BLUE, description:"PR open, waiting for review"},
  {name:"Done", color:GREEN, description:"Merged / finished"}]}){projectV2Field{... on ProjectV2SingleSelectField{id}}}}'
  fi
  if [ "$MODE" = run ]; then load_fields; fi
fi

# ---- 4. issues -------------------------------------------------------------------------
log ""
log "== 4. Issues T-01..T-$(printf '%02d' "$TICKET_COUNT")"

ISSUES_FILE="$WORK_DIR/issues.json"
fetch_issues() {
  read_call "Listing issues" gh issue list --repo "$REPO" --state all --limit 1000 \
    --json number,title,body,url >"$ISSUES_FILE"
}
issue_number_for() {  # T-nn -> issue number (empty if missing)
  jq -r --arg p "[$1]" '[.[] | select(.title | startswith($p))] | sort_by(.number) | .[0].number // empty' "$ISSUES_FILE"
}
fetch_issues

ITEMS_FILE="$WORK_DIR/items.json"
echo '{"items":[]}' >"$ITEMS_FILE"
if [ -n "$PROJECT_NUMBER" ]; then
  read_call "Listing project items" gh project item-list "$PROJECT_NUMBER" --owner "$OWNER" --limit 1000 --format json >"$ITEMS_FILE"
fi
points_key="$(printf '%s' "$POINTS_FIELD" | tr '[:upper:]' '[:lower:]')"

i=0
while [ "$i" -lt "$TICKET_COUNT" ]; do
  tid="$(jq -r ".[$i].id" "$TICKETS_JSON")"
  title="$(jq -r ".[$i].title" "$TICKETS_JSON")"
  points="$(jq -r ".[$i].points" "$TICKETS_JSON")"
  jq -r ".[$i].body" "$TICKETS_JSON" >"$WORK_DIR/body.md"
  label_args=()
  while IFS= read -r l; do label_args+=(--label "$l"); done <<EOF
$(jq -r ".[$i].labels[], \"points:\(.[$i].points)\"" "$TICKETS_JSON")
EOF
  i=$((i + 1))

  number="$(issue_number_for "$tid")"
  if [ -n "$number" ]; then
    issues_skipped=$((issues_skipped + 1))
    log "  SKIP   ${tid} exists as #${number}"
    url="https://github.com/${REPO}/issues/${number}"
  else
    log "  $([ "$MODE" = run ] && echo CREATE || echo 'WOULD CREATE') ${title}  [${points} pt]"
    write_call "Creating issue ${tid}" "$SLEEP_CREATE" \
      gh issue create --repo "$REPO" --title "$title" --body-file "$WORK_DIR/body.md" \
        --milestone "$MILESTONE" "${label_args[@]}"
    issues_created=$((issues_created + 1))
    if [ "$MODE" != run ]; then
      if [ -n "$PROJECT_NUMBER" ]; then items_added=$((items_added + 1)); fields_set=$((fields_set + 2)); fi
      continue
    fi
    url="$(printf '%s' "$WRITE_OUT" | tail -n 1)"
    number="${url##*/}"
    log "         -> #${number}"
  fi

  [ -n "$PROJECT_NUMBER" ] || continue
  item="$(jq -c --arg u "$url" '[.items[] | select(.content.url == $u)][0] // empty' "$ITEMS_FILE")"
  if [ -n "$item" ]; then
    items_skipped=$((items_skipped + 1))
    item_id="$(printf '%s' "$item" | jq -r '.id')"
    cur_points="$(printf '%s' "$item" | jq -r --arg k "$points_key" '.[$k] // empty')"
    cur_status="$(printf '%s' "$item" | jq -r '.status // empty')"
  else
    log "         $([ "$MODE" = run ] && echo 'add to board' || echo 'would add to board'), Points=${points}, Status=Todo"
    write_call "Adding ${tid} to the project" "$SLEEP_WRITE" \
      gh project item-add "$PROJECT_NUMBER" --owner "$OWNER" --url "$url" --format json
    items_added=$((items_added + 1))
    [ "$MODE" = run ] || continue
    item_id="$(printf '%s' "$WRITE_OUT" | jq -r '.id')"
    cur_points=""; cur_status=""
  fi
  if [ -z "$cur_points" ]; then
    [ -n "$item" ] && log "         $([ "$MODE" = run ] && echo set || echo 'would set') Points=${points}"
    if [ "$MODE" = run ]; then
      [ -n "$POINTS_FIELD_ID" ] || die "Points field not found on project #${PROJECT_NUMBER}."
      write_call "Setting Points on ${tid}" "$SLEEP_WRITE" \
        gh project item-edit --id "$item_id" --project-id "$PROJECT_ID" --field-id "$POINTS_FIELD_ID" --number "$points"
    fi
    fields_set=$((fields_set + 1))
  fi
  if [ -z "$cur_status" ]; then
    [ -n "$item" ] && log "         $([ "$MODE" = run ] && echo set || echo 'would set') Status=Todo"
    if [ "$MODE" = run ]; then
      [ -n "$TODO_OPTION_ID" ] || die "Status option \"Todo\" not found on project #${PROJECT_NUMBER}."
      write_call "Setting Status on ${tid}" "$SLEEP_WRITE" \
        gh project item-edit --id "$item_id" --project-id "$PROJECT_ID" --field-id "$STATUS_FIELD_ID" --single-select-option-id "$TODO_OPTION_ID"
    fi
    fields_set=$((fields_set + 1))
  fi
done

# ---- 5. dependency links ---------------------------------------------------------------
log ""
log "== 5. Dependencies (clickable #links in the body + native \"blocked by\")"
fetch_issues

i=0
while [ "$i" -lt "$TICKET_COUNT" ]; do
  tid="$(jq -r ".[$i].id" "$TICKETS_JSON")"
  deps="$(jq -r ".[$i].deps[]" "$TICKETS_JSON")"
  i=$((i + 1))
  [ -n "$deps" ] || continue

  number="$(issue_number_for "$tid")"
  dep_text=""; dep_numbers=""
  for d in $deps; do
    dn="$(issue_number_for "$d")"
    dep_text="${dep_text:+${dep_text}, }${d} (#${dn:-?})"
    dep_numbers="${dep_numbers} ${dn}"
  done

  if [ -z "$number" ]; then
    log "  WOULD UPDATE ${tid} body: Depends on: ${dep_text}"
    bodies_updated=$((bodies_updated + 1))
    deps_created=$((deps_created + $(printf '%s\n' $deps | wc -l)))
    continue
  fi

  # Only the "- Depends on:" line of the CURRENT issue body is replaced, so edits made on
  # GitHub (description, checklists, ...) are never overwritten by a re-run.
  cur_body="$(jq -r --argjson n "$number" '.[] | select(.number == $n) | .body' "$ISSUES_FILE" | tr -d '\r')"
  new_body="$(printf '%s' "$cur_body" | python3 -I -c '
import re, sys
print(re.sub(r"(?m)^- Depends on: .*$", lambda m: "- Depends on: " + sys.argv[1], sys.stdin.read(), count=1), end="")
' "$dep_text")"
  if [ "$cur_body" = "$new_body" ]; then
    bodies_skipped=$((bodies_skipped + 1))
  else
    log "  $([ "$MODE" = run ] && echo UPDATE || echo 'WOULD UPDATE') #${number} (${tid}) body: Depends on: ${dep_text}"
    printf '%s\n' "$new_body" >"$WORK_DIR/body.md"
    write_call "Updating body of ${tid}" "$SLEEP_WRITE" \
      gh issue edit "$number" --repo "$REPO" --body-file "$WORK_DIR/body.md"
    bodies_updated=$((bodies_updated + 1))
  fi

  [ "$native_deps" = "enabled" ] || continue
  if ! existing="$(gh api "repos/${REPO}/issues/${number}/dependencies/blocked_by?per_page=100" --jq '.[].number' 2>"$WORK_DIR/err")"; then
    warn "Native issue dependencies are not available ($(head -n 1 "$WORK_DIR/err")); only the body links are used."
    native_deps="unavailable"
    continue
  fi
  for dn in $dep_numbers; do
    if printf '%s\n' "$existing" | grep -qxF "$dn"; then
      deps_skipped=$((deps_skipped + 1))
      continue
    fi
    log "  $([ "$MODE" = run ] && echo LINK || echo 'WOULD LINK') #${number} (${tid}) blocked by #${dn}"
    deps_created=$((deps_created + 1))
    [ "$MODE" = run ] || continue
    blocker_id="$(read_call "Reading issue #${dn}" gh api "repos/${REPO}/issues/${dn}" --jq '.id')"
    if ! gh api --method POST "repos/${REPO}/issues/${number}/dependencies/blocked_by" \
         -F issue_id="$blocker_id" >/dev/null 2>"$WORK_DIR/err"; then
      if grep -qE '404|410|Not Found' "$WORK_DIR/err"; then
        warn "Native issue dependencies are not available ($(head -n 1 "$WORK_DIR/err")); only the body links are used."
        native_deps="unavailable"; deps_created=$((deps_created - 1))
        break
      fi
      die "Adding dependency #${number} blocked by #${dn} failed: $(cat "$WORK_DIR/err")"
    fi
    sleep "$SLEEP_WRITE"
  done
done
if [ "$MODE" != run ] && [ "$issues_created" -gt 0 ]; then
  log "  (dry-run: issues do not exist yet, so issue numbers show as #?)"
fi

# ---- 6. summary ------------------------------------------------------------------------
verb="$([ "$MODE" = run ] && echo created || echo 'would create')"
log ""
log "================ Summary (${MODE}) ================"
log "Labels:            ${labels_created} ${verb}, ${labels_skipped} skipped (exist)"
log "Milestone:         $([ "$milestone_state" = created ] && echo "1 ${verb}" || echo "skipped (exists)")"
log "Project:           $([ "$MODE" = run ] || [ "$project_state" = skipped ] || printf "would be ")${project_state} - ${PROJECT_URL}"
log "Issues:            ${issues_created} ${verb}, ${issues_skipped} skipped (exist)"
log "Board items:       ${items_added} $([ "$MODE" = run ] && echo added || echo "would add"), ${items_skipped} already on the board, ${fields_set} field values set"
log "Body dep links:    ${bodies_updated} $([ "$MODE" = run ] && echo updated || echo "would update"), ${bodies_skipped} already up to date"
log "Native blocked-by: ${deps_created} ${verb}, ${deps_skipped} skipped (exist), API ${native_deps}"
[ "$MODE" = run ] || log "Dry-run only. Re-run with --run to apply."
