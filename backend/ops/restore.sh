#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${PV_BACKEND_URL:-http://localhost:8080}"

DRY_RUN=false
BACKUP_FILE=""

usage() {
  cat <<'EOF'
Usage: ops/restore.sh [--dry-run] <backup-file>

Restores cases from a backup JSON file (as written by ops/backup.sh) using
PUT /cases/{caseId} -- a raw overwrite that bypasses merge logic entirely.
Re-running with the same file is a no-op: PUT always sets the exact same
state, it never merges, so restoring twice leaves the case unchanged.

  --dry-run   Show what would be restored without making any changes
EOF
}

log() {
  echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] $*" >&2
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    log "Error: required command '$1' not found on PATH."
    exit 1
  fi
}

for arg in "$@"; do
  case "$arg" in
    --dry-run)
      DRY_RUN=true
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    -*)
      echo "Error: unknown option '$arg'." >&2
      usage
      exit 1
      ;;
    *)
      if [[ -n "$BACKUP_FILE" ]]; then
        echo "Error: too many arguments." >&2
        usage
        exit 1
      fi
      BACKUP_FILE="$arg"
      ;;
  esac
done

if [[ -z "$BACKUP_FILE" ]]; then
  echo "Error: missing required <backup-file> argument." >&2
  usage
  exit 1
fi

require_cmd curl
require_cmd jq

if [[ ! -f "$BACKUP_FILE" ]]; then
  log "Error: backup file '$BACKUP_FILE' not found."
  exit 1
fi

if ! jq empty "$BACKUP_FILE" >/dev/null 2>&1; then
  log "Error: '$BACKUP_FILE' is not valid JSON."
  exit 1
fi

if [[ "$(jq -r 'type' "$BACKUP_FILE")" != "array" ]]; then
  log "Error: '$BACKUP_FILE' does not contain a JSON array of cases."
  exit 1
fi

case_count="$(jq 'length' "$BACKUP_FILE")"

if [[ "$DRY_RUN" == true ]]; then
  log "Dry run: $case_count case(s) found in $BACKUP_FILE"
else
  log "Restoring $case_count case(s) from $BACKUP_FILE"
fi

restored=0
while IFS= read -r case_json; do
  case_id="$(jq -r '.case_id // empty' <<<"$case_json")"
  if [[ -z "$case_id" ]]; then
    log "Error: found a case in the backup with no case_id. Aborting."
    exit 1
  fi

  if [[ "$DRY_RUN" == true ]]; then
    log "[DRY RUN] would PUT $BASE_URL/cases/$case_id"
    restored=$((restored + 1))
    continue
  fi

  response="$(curl -sS -X PUT -H 'Content-Type: application/json' \
    --data-binary "$case_json" -w $'\n%{http_code}' "$BASE_URL/cases/$case_id" || true)"
  http_code="${response##*$'\n'}"
  body="${response%$'\n'*}"

  if [[ "$http_code" != "200" ]]; then
    log "Error: PUT $BASE_URL/cases/$case_id failed (HTTP ${http_code:-no response}): $body"
    exit 1
  fi

  log "Restored $case_id"
  restored=$((restored + 1))
done < <(jq -c '.[]' "$BACKUP_FILE")

if [[ "$DRY_RUN" == true ]]; then
  log "Dry run complete: $restored case(s) would be restored"
else
  log "Restore complete: $restored case(s) restored from $BACKUP_FILE"
fi
