.PHONY: help build start stop test logs clean backup restore

help:
	@echo "Usage: make <target>"
	@echo ""
	@echo "Targets:"
	@echo "  build                          Build the backend Docker image"
	@echo "  start                          Start the backend (docker compose up -d)"
	@echo "  stop                           Stop the backend (docker compose down)"
	@echo "  test                           Run the backend test suite (./gradlew test)"
	@echo "  logs                           Follow the backend container logs"
	@echo "  clean                          Stop containers, remove built images, and clean Gradle build output"
	@echo "  backup                         Snapshot all cases to backups/cases-<timestamp>.json"
	@echo "  restore FILE=...               Restore cases from a backup file (PUT, bypasses merge)"
	@echo "  restore FILE=... DRY_RUN=1     Show what restore would do without making changes"

build:
	./ops/run.sh build

start:
	./ops/run.sh start

stop:
	./ops/run.sh stop

test:
	./ops/run.sh test

logs:
	./ops/run.sh logs

clean:
	./ops/run.sh clean

backup:
	./ops/backup.sh

restore:
	./ops/restore.sh $(if $(DRY_RUN),--dry-run) $(FILE)
