#!/usr/bin/env bash
set -euo pipefail

# Build script that loads environment variables from .env.local before running Maven.
# Reads env file location and primary module from .claude/project-config.json
# Usage:
#   ./scripts/build.sh                  # runs: mvn clean verify
#   ./scripts/build.sh install          # runs: mvn clean install
#   ./scripts/build.sh test             # runs: mvn clean test
#   ./scripts/build.sh compile          # runs: mvn clean compile
#   ./scripts/build.sh spotless         # runs: mvn spotless:apply -pl <module>
#   ./scripts/build.sh <any mvn args>   # runs: mvn <args>

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CONFIG="$PROJECT_ROOT/.claude/project-config.json"

if [[ -f "$CONFIG" ]]; then
  ENV_FILE_REL=$(python3 -c "import json; print(json.load(open('$CONFIG'))['env']['envFileLocation'])")
  PRIMARY_MODULE=$(python3 -c "import json; print(json.load(open('$CONFIG'))['build']['primaryModule'])")
else
  # Fallback for projects without project-config.json
  ENV_FILE_REL="apps/backend/.env.local"
  PRIMARY_MODULE="apps/backend"
fi

ENV_FILE="$PROJECT_ROOT/$ENV_FILE_REL"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERROR: $ENV_FILE not found. Copy .env.example to .env.local and fill in the values." >&2
  exit 1
fi

# Export all non-comment, non-empty lines from .env.local
set -a
while IFS= read -r line; do
  [[ -z "$line" || "$line" =~ ^# ]] && continue
  eval "export $line"
done < "$ENV_FILE"
set +a

cd "$PROJECT_ROOT"

if [[ $# -eq 0 ]]; then
  # Default: full reactor build
  echo "Running: mvn clean verify (from $PROJECT_ROOT)"
  mvn spotless:apply -pl "$PRIMARY_MODULE" -q
  exec mvn clean verify
elif [[ "$1" == "spotless" ]]; then
  echo "Running: mvn spotless:apply -pl $PRIMARY_MODULE"
  exec mvn spotless:apply -pl "$PRIMARY_MODULE"
elif [[ "$1" == "verify" || "$1" == "install" || "$1" == "test" || "$1" == "compile" ]]; then
  echo "Running: mvn clean $1 (from $PROJECT_ROOT)"
  mvn spotless:apply -pl "$PRIMARY_MODULE" -q
  exec mvn clean "$1"
else
  # Pass through any custom Maven arguments
  echo "Running: mvn $* (from $PROJECT_ROOT)"
  exec mvn "$@"
fi
