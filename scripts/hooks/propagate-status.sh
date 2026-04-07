#!/usr/bin/env bash
# Hook: propagate-status.sh
# Triggered on: git checkout -b, git switch -c (PostToolUse)
# Purpose: Set issue to "In Progress" and propagate to parent hierarchy
set -euo pipefail

# Extract branch name from tool input
BRANCH_NAME=$(echo "${TOOL_INPUT:-}" | grep -oP '(?:checkout -b|switch -c)\s+\K\S+' || true)

if [[ -z "$BRANCH_NAME" ]]; then
  exit 0  # Not a branch creation we can parse
fi

# Extract issue number from branch: <type>/<issue-number>-<description>
ISSUE_NUMBER=$(echo "$BRANCH_NAME" | grep -oP '^\w+/\K[0-9]+' || true)

if [[ -z "$ISSUE_NUMBER" ]]; then
  exit 0  # Branch doesn't match convention — silently skip
fi

# Check required environment variables
if [[ -z "${PROJECT_ID:-}" || -z "${STATUS_FIELD_ID:-}" ]]; then
  exit 0  # No project configured
fi

# Resolve workflow directory
CONFIG=".claude/project-config.json"
if [[ ! -f "$CONFIG" ]]; then
  exit 0
fi

WORKFLOW_DIR=$(python3 -c "import json; print(json.load(open('$CONFIG'))['github']['workflowDir'])" 2>/dev/null || true)
WORKFLOW_DIR="${WORKFLOW_DIR/#\~/$HOME}"

if [[ -z "$WORKFLOW_DIR" || ! -d "$WORKFLOW_DIR/scripts" ]]; then
  exit 0
fi

# Set issue to "In Progress"
"$WORKFLOW_DIR/scripts/project-set-status.sh" \
  "$PROJECT_ID" "$STATUS_FIELD_ID" "$ISSUE_NUMBER" "In Progress" >&2 2>/dev/null || true

# Propagate to parent hierarchy
"$WORKFLOW_DIR/scripts/evaluate-issue-status.sh" "$ISSUE_NUMBER" >&2 2>/dev/null || true
