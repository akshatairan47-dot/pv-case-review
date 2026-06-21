#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"

usage() {
  cat <<'EOF'
Usage: ops/run.sh <command>

Commands:
  build    Build the backend Docker image
  start    Start the backend (docker compose up -d)
  stop     Stop the backend (docker compose down)
  test     Run the backend test suite (./gradlew test)
  logs     Follow the backend container logs
  clean    Stop containers, remove built images, and clean Gradle build output
  --help   Show this help message
EOF
}

require_docker() {
  if ! docker info >/dev/null 2>&1; then
    echo "Error: Docker doesn't seem to be running. Start Docker and try again." >&2
    exit 1
  fi
}

cmd_build() {
  require_docker
  (cd "$ROOT_DIR" && docker compose build)
}

cmd_start() {
  require_docker
  (cd "$ROOT_DIR" && docker compose up -d)
}

cmd_stop() {
  require_docker
  (cd "$ROOT_DIR" && docker compose down)
}

cmd_test() {
  (cd "$BACKEND_DIR" && ./gradlew test)
}

cmd_logs() {
  require_docker
  (cd "$ROOT_DIR" && docker compose logs -f backend)
}

cmd_clean() {
  require_docker
  (cd "$ROOT_DIR" && docker compose down --rmi local --volumes --remove-orphans)
  (cd "$BACKEND_DIR" && ./gradlew clean)
}

case "${1:-}" in
  --help|-h)
    usage
    ;;
  build)
    cmd_build
    ;;
  start)
    cmd_start
    ;;
  stop)
    cmd_stop
    ;;
  test)
    cmd_test
    ;;
  logs)
    cmd_logs
    ;;
  clean)
    cmd_clean
    ;;
  "")
    echo "Error: no command given." >&2
    usage
    exit 1
    ;;
  *)
    echo "Error: unknown command '$1'." >&2
    usage
    exit 1
    ;;
esac
