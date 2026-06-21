#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_DIR="$ROOT_DIR/backups"

BASE_URL="${PV_BACKEND_URL:-http://localhost:8080}"

log() {
  echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] $*" >&2
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    log "Error: required command '$1' not found on PATH."
    exit 1
  fi
}

require_cmd curl
require_cmd jq

mkdir -p "$BACKUP_DIR"

timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
tmp_file="$(mktemp "$BACKUP_DIR/.cases-tmp.XXXXXXXX")"
unique="${tmp_file##*.cases-tmp.}"
dest="$BACKUP_DIR/cases-${timestamp}-${unique}.json"

cleanup() {
  rm -f "$tmp_file"
}
trap cleanup EXIT

log "Starting backup from $BASE_URL/cases"

http_code="$(curl -sS -w '%{http_code}' -o "$tmp_file" "$BASE_URL/cases" || true)"

if [[ "$http_code" != "200" ]]; then
  log "Error: GET $BASE_URL/cases failed (HTTP ${http_code:-no response}). Is the backend running?"
  exit 1
fi

if ! jq empty "$tmp_file" >/dev/null 2>&1; then
  log "Error: response body is not valid JSON. Aborting backup."
  exit 1
fi

case_count="$(jq 'length' "$tmp_file")"
mv "$tmp_file" "$dest"
trap - EXIT

log "Backup complete: ${case_count} case(s) written to $dest"
