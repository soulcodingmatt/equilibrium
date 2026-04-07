#!/usr/bin/env bash
# Hook: propagate-status-on-issue-create.sh
# Triggered on: gh issue create (PostToolUse)
# Purpose: Re-evaluate parent status when a new issue is created with a parent reference
set -euo pipefail

# Check required environment variables
if [[ -z "${PROJECT_ID:-}" || -z "${STATUS_FIELD_ID:-}" ]]; then
  exit 0
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

# Extract new issue number from tool output (URL like https://github.com/owner/repo/issues/123)
NEW_ISSUE=$(echo "${TOOL_OUTPUT:-}" | grep -oP 'issues/\K[0-9]+' | head -1 || true)

if [[ -z "$NEW_ISSUE" ]]; then
  exit 0
fi

# Check if the new issue has a parent
HIERARCHY=$("$WORKFLOW_DIR/scripts/find-issue-hierarchy.sh" "$NEW_ISSUE" 2>/dev/null || true)

if [[ -z "$HIERARCHY" ]]; then
  exit 0
fi

PARENT=$(echo "$HIERARCHY" | jq -r '.parent // empty' 2>/dev/null || true)

if [[ -z "$PARENT" || "$PARENT" == "null" ]]; then
  exit 0
fi

# Re-evaluate the parent's status
"$WORKFLOW_DIR/scripts/evaluate-issue-status.sh" "$PARENT" >&2 2>/dev/null || true
