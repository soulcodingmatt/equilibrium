#!/usr/bin/env bash
# Export environment variables from the project's .env.local to ~/.bashrc
# Reads project name and env file location from .claude/project-config.json
# Usage: bash scripts/export-env.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CONFIG="$PROJECT_ROOT/.claude/project-config.json"

if [ ! -f "$CONFIG" ]; then
  echo "ERROR: $CONFIG not found"
  exit 1
fi

PROJECT_NAME=$(python3 -c "import json; print(json.load(open('$CONFIG'))['project']['name'])")
ENV_FILE_REL=$(python3 -c "import json; print(json.load(open('$CONFIG'))['env']['envFileLocation'])")
ENV_FILE="$PROJECT_ROOT/$ENV_FILE_REL"

if [ ! -f "$ENV_FILE" ]; then
  echo "ERROR: $ENV_FILE not found"
  exit 1
fi

MARKER_START="# --- ${PROJECT_NAME} env start ---"
MARKER_END="# --- ${PROJECT_NAME} env end ---"

# Remove any previously added block
sed -i "/^${MARKER_START}$/,/^${MARKER_END}$/d" ~/.bashrc

# Append new block
echo "$MARKER_START" >> ~/.bashrc
while IFS= read -r line; do
  # Skip empty lines and comments
  [[ -z "$line" || "$line" =~ ^# ]] && continue
  key="${line%%=*}"
  value="${line#*=}"
  echo "export ${key}=${value}" >> ~/.bashrc
done < "$ENV_FILE"
echo "$MARKER_END" >> ~/.bashrc

set +u
source ~/.bashrc
set -u

echo "Exported variables from $ENV_FILE_REL to ~/.bashrc (marker: $PROJECT_NAME)"
