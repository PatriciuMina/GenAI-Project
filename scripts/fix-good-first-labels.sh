#!/usr/bin/env bash
# Applies the good-first-ticket rule to the existing "[T-nn]" issues:
#   good-first-ticket  <=>  1-2 points (label points:1 / points:2)  AND  no dependencies
# "No dependencies" = the body says "Depends on: none" (or has no Depends line) AND the
# issue has no native "blocked by" relations.
#
#   ./scripts/fix-good-first-labels.sh          dry-run (default): only prints the changes
#   ./scripts/fix-good-first-labels.sh --run    really changes the labels
#
# Idempotent: issues that already follow the rule are left alone. Works with bash 3.2.
# Needs: gh (logged in, scope repo) and jq.

set -euo pipefail

readonly REPO="PatriciuMina/GenAI-Project"   # CLAUDE.md: the only repository we touch
readonly LABEL="good-first-ticket"
readonly LABEL_COLOR="7057ff"
readonly LABEL_DESC="1-2 points and no dependencies: can start right away"
SLEEP_WRITE="${SLEEP_WRITE:-1}"

MODE="dry-run"
usage() {
  cat <<EOF
Usage: $(basename "$0") [--dry-run | --run | -h]

  --dry-run   (default) print which issues would get / lose the "${LABEL}" label
  --run       really change the labels in ${REPO}
  -h, --help  show this help

Environment: SLEEP_WRITE (default 1) seconds between write calls.
EOF
}
while [ $# -gt 0 ]; do
  case "$1" in
    --dry-run) MODE="dry-run" ;;
    --run) MODE="run" ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage >&2; exit 2 ;;
  esac
  shift
done

log()  { printf '%s\n' "$*"; }
warn() { printf 'WARNING: %s\n' "$*" >&2; }
die()  {
  printf '\nERROR: %s\n' "$*" >&2
  printf 'Fix the cause and re-run; issues that are already correct are skipped.\n' >&2
  exit 1
}

for tool in gh jq; do
  command -v "$tool" >/dev/null 2>&1 || die "'$tool' is not installed."
done
gh auth status >/dev/null 2>&1 || die "gh auth status failed (not logged in, or a network problem). Check: gh auth status; log in with: gh auth login"

WORK_DIR="$(mktemp -d "${TMPDIR:-/tmp}/fix-good-first.XXXXXX")"
trap 'rm -rf "$WORK_DIR"' EXIT

# Write call: only in --run; one retry after 60 s on a secondary rate limit.
write_call() {
  local ctx="$1"; shift
  [ "$MODE" = "run" ] || return 0
  local attempt
  for attempt in 1 2; do
    if "$@" >/dev/null 2>"$WORK_DIR/err"; then
      sleep "$SLEEP_WRITE"
      return 0
    fi
    if [ "$attempt" = 1 ] && grep -qiE 'secondary rate limit|abuse|was submitted too quickly' "$WORK_DIR/err"; then
      warn "${ctx}: secondary rate limit, waiting 60 s and retrying once..."
      sleep 60
      continue
    fi
    break
  done
  die "${ctx} failed: $(cat "$WORK_DIR/err")"
}

do_or_would() { [ "$MODE" = run ] && echo "$1" || echo "WOULD $1"; }

log "== Mode: ${MODE}  (repo ${REPO})"
[ "$MODE" = run ] || log "== Dry-run: nothing will be changed. Use --run to apply."
log "== Rule: ${LABEL} only on issues with points:1/points:2 AND no dependencies"
log ""

# ---- label description -----------------------------------------------------------------
label_desc="$(gh label list --repo "$REPO" --search "$LABEL" --limit 50 --json name,description \
  --jq ".[] | select(.name == \"${LABEL}\") | .description" 2>"$WORK_DIR/err")" \
  || die "Listing labels failed: $(cat "$WORK_DIR/err")"
label_changed=0
if [ "$label_desc" != "$LABEL_DESC" ]; then
  log "$(do_or_would UPDATE) label description: \"${label_desc:-<label missing>}\" -> \"${LABEL_DESC}\""
  if [ -z "$label_desc" ] && ! gh label list --repo "$REPO" --search "$LABEL" --json name --jq '.[].name' | grep -qxF "$LABEL"; then
    write_call "Creating label" gh label create "$LABEL" --repo "$REPO" --color "$LABEL_COLOR" --description "$LABEL_DESC"
  else
    write_call "Updating label" gh label edit "$LABEL" --repo "$REPO" --description "$LABEL_DESC"
  fi
  label_changed=1
fi

# ---- issues ----------------------------------------------------------------------------
gh issue list --repo "$REPO" --state all --limit 1000 --json number,title,state,body,labels \
  >"$WORK_DIR/issues.json" 2>"$WORK_DIR/err" || die "Listing issues failed: $(cat "$WORK_DIR/err")"
jq -c '[.[] | select(.title | test("^\\[T-[0-9]+\\]"))] | sort_by(.title) | .[]' "$WORK_DIR/issues.json" \
  >"$WORK_DIR/tickets.jsonl"
total="$(wc -l <"$WORK_DIR/tickets.jsonl" | tr -d ' ')"
log "Checking ${total} [T-nn] issues (reads native \"blocked by\" for each, takes a moment)..."
log ""

added=0; removed=0; bodies=0; unchanged=0; skipped=0; native="available"

while IFS= read -r issue; do
  number="$(printf '%s' "$issue" | jq -r '.number')"
  title="$(printf '%s' "$issue" | jq -r '.title')"
  tid="$(printf '%s' "$title" | sed -E 's/^\[(T-[0-9]+)\].*/\1/')"
  points="$(printf '%s' "$issue" | jq -r '[.labels[].name | select(startswith("points:")) | ltrimstr("points:")][0] // empty')"
  has="$(printf '%s' "$issue" | jq -r --arg l "$LABEL" '[.labels[].name] | index($l) != null')"
  dep_line="$(printf '%s' "$issue" | jq -r '.body' | tr -d '\r' | grep -m1 -E '^- Depends on:' | sed -E 's/^- Depends on:[[:space:]]*//' || true)"

  if [ -z "$points" ]; then
    warn "#${number} ${tid}: no points:<n> label, skipped"
    skipped=$((skipped + 1))
    continue
  fi

  blockers=0
  if [ "$native" = available ]; then
    out=""
    for attempt in 1 2 3; do
      if out="$(gh api "repos/${REPO}/issues/${number}/dependencies/blocked_by?per_page=100" --jq 'length' 2>"$WORK_DIR/err")"; then
        break
      fi
      out=""
      # The feature itself is missing: fall back to the Depends on line for all issues.
      if grep -qE 'HTTP 40[4]|HTTP 410|Not Found' "$WORK_DIR/err"; then
        warn "Native \"blocked by\" not available ($(head -n 1 "$WORK_DIR/err")); using only the Depends on line."
        native="unavailable"
        break
      fi
      [ "$attempt" = 3 ] && die "Reading \"blocked by\" of #${number} failed 3 times: $(cat "$WORK_DIR/err")"
      warn "#${number}: reading \"blocked by\" failed ($(head -n 1 "$WORK_DIR/err")), retrying in 5 s..."
      sleep 5
    done
    [ -n "$out" ] && blockers="$out"
  fi

  if { [ -z "$dep_line" ] || [ "$dep_line" = "none" ]; } && [ "$blockers" -eq 0 ]; then
    has_deps="no"; why="no dependencies"
  else
    has_deps="yes"; why="depends on: ${dep_line:-<none in body>}, ${blockers} native blocker(s)"
  fi

  if [ "$has_deps" = no ] && { [ "$points" = 1 ] || [ "$points" = 2 ]; }; then want=true; else want=false; fi

  # The body repeats the labels ("- Labels: ..."); keep that line consistent with the rule.
  printf '%s' "$issue" | jq -r '.body' | tr -d '\r' >"$WORK_DIR/old_body.md"
  if [ "$want" = true ]; then
    sed -E "/^- Labels: /{/(^|, |: )${LABEL}(,|\$)/!s/\$/, ${LABEL}/;}" "$WORK_DIR/old_body.md" >"$WORK_DIR/new_body.md"
  else
    sed -E "/^- Labels: /{s/, ${LABEL}(,|\$)/\\1/;}" "$WORK_DIR/old_body.md" >"$WORK_DIR/new_body.md"
  fi
  body_args=()
  if ! cmp -s "$WORK_DIR/old_body.md" "$WORK_DIR/new_body.md"; then
    body_args=(--body-file "$WORK_DIR/new_body.md")
  fi

  if [ "$has" = "$want" ] && [ ${#body_args[@]} -eq 0 ]; then
    unchanged=$((unchanged + 1))
    continue
  fi
  body_note="$([ ${#body_args[@]} -gt 0 ] && echo ' + fix "- Labels:" line in body' || true)"
  if [ "$has" = "$want" ]; then
    log "  $(do_or_would FIX) body \"- Labels:\" line of #${number} ${tid} (label already correct)"
    write_call "Updating body of #${number}" gh issue edit "$number" --repo "$REPO" "${body_args[@]}"
    bodies=$((bodies + 1))
  elif [ "$want" = true ]; then
    log "  $(do_or_would ADD)    ${LABEL} on #${number} ${tid} (${points} pt, ${why})${body_note}"
    write_call "Adding label to #${number}" gh issue edit "$number" --repo "$REPO" --add-label "$LABEL" ${body_args[@]+"${body_args[@]}"}
    added=$((added + 1))
    [ -z "$body_note" ] || bodies=$((bodies + 1))
  else
    log "  $(do_or_would REMOVE) ${LABEL} from #${number} ${tid} (${points} pt, ${why})${body_note}"
    write_call "Removing label from #${number}" gh issue edit "$number" --repo "$REPO" --remove-label "$LABEL" ${body_args[@]+"${body_args[@]}"}
    removed=$((removed + 1))
    [ -z "$body_note" ] || bodies=$((bodies + 1))
  fi
done <"$WORK_DIR/tickets.jsonl"

log ""
log "================ Summary (${MODE}) ================"
log "Issues checked:      ${total}"
log "Label added:         ${added}$([ "$MODE" = run ] || echo ' (would add)')"
log "Label removed:       ${removed}$([ "$MODE" = run ] || echo ' (would remove)')"
log "Body Labels line:    ${bodies}$([ "$MODE" = run ] || echo ' (would fix)')"
log "Already correct:     ${unchanged}"
log "Skipped (no points): ${skipped}"
log "Label description:   $([ "$label_changed" = 1 ] && { [ "$MODE" = run ] && echo updated || echo "would update"; } || echo 'already correct')"
log "Native blocked-by:   ${native}"
log "With ${LABEL} afterwards: $(( $(jq '[.[] | select(.title | test("^\\[T-[0-9]+\\]")) | select([.labels[].name] | index("good-first-ticket"))] | length' "$WORK_DIR/issues.json") + added - removed ))"
[ "$MODE" = run ] || log "Dry-run only. Re-run with --run to apply."
