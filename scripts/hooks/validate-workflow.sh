#!/bin/bash
# =============================================================================
# validate-workflow.sh — Claude Code PreToolUse hook
# Enforces /git-workflow rules: protected branches, PR targets, merge strategy.
# Uses python3 for JSON parsing (no jq dependency).
# =============================================================================

set -euo pipefail

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('command',''))" 2>/dev/null || echo "")
CWD=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('cwd',''))" 2>/dev/null || echo "")

if [ -z "$COMMAND" ]; then
  exit 0
fi

PROJECT_DIR="${CWD:-${CLAUDE_PROJECT_DIR:-.}}"

# ---------------------------------------------------------------------------
# Only enforce rules when operating on the project's own git repo
# ---------------------------------------------------------------------------
if [ ! -f "$PROJECT_DIR/.claude/project-config.json" ]; then
  exit 0
fi

# Determine which git repo the command actually targets.
# Skip validation when:
#   - Command starts with "cd <dir> &&" (simple prefix)
#   - Command contains "cd <dir> &&" mid-chain (agentrepo sync patterns)
#   - Command uses "git -C <dir>" targeting a non-project repo
#   - Command operates in /tmp/ (temp clones for sync/backup)
#   - Command is wrapped in "bash <script>" (delegated to a script)
if echo "$COMMAND" | grep -qE '(cd|git -C) (/tmp/|"\$|"\{)'; then
  exit 0
fi
if echo "$COMMAND" | grep -qE '^bash /tmp/'; then
  exit 0
fi
CMD_DIR="$PROJECT_DIR"
if echo "$COMMAND" | grep -qE '^cd [^ ]+ &&'; then
  CMD_DIR=$(echo "$COMMAND" | grep -oE '^cd [^ ]+' | sed 's/^cd //')
fi
CMD_GIT_ROOT=$(git -C "$CMD_DIR" rev-parse --show-toplevel 2>/dev/null || echo "")
PROJECT_GIT_ROOT=$(git -C "$PROJECT_DIR" rev-parse --show-toplevel 2>/dev/null || echo "")
if [ -n "$CMD_GIT_ROOT" ] && [ "$CMD_GIT_ROOT" != "$PROJECT_GIT_ROOT" ]; then
  exit 0
fi

# ---------------------------------------------------------------------------
# Helper: deny with reason (JSON output for Claude Code)
# ---------------------------------------------------------------------------
deny() {
  local reason="$1"
  python3 -c "
import json, sys
print(json.dumps({
    'hookSpecificOutput': {
        'hookEventName': 'PreToolUse',
        'permissionDecision': 'deny',
        'permissionDecisionReason': sys.argv[1]
    }
}))
" "$reason"
  exit 0
}

# ---------------------------------------------------------------------------
# Helper: get current branch
# ---------------------------------------------------------------------------
current_branch() {
  git -C "$PROJECT_DIR" rev-parse --abbrev-ref HEAD 2>/dev/null || echo ""
}

# ---------------------------------------------------------------------------
# 1. No direct commits to main or develop
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'git commit\b'; then
  BRANCH=$(current_branch)
  if [ "$BRANCH" = "main" ] || [ "$BRANCH" = "master" ]; then
    deny "Direct commits to '$BRANCH' are forbidden. Create a feature branch first. See /git-workflow for rules."
  fi
  if [ "$BRANCH" = "develop" ]; then
    deny "Direct commits to 'develop' are forbidden. Create a feature branch first. See /git-workflow for rules."
  fi
fi

# ---------------------------------------------------------------------------
# 2. No --no-verify on commits
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'git commit\b.*--no-verify'; then
  deny "Bypassing the pre-commit hook ('--no-verify') is forbidden. Fix the issue instead. See /git-workflow for rules."
fi

# ---------------------------------------------------------------------------
# 3. No force-push to main or develop; feature branches use --force-with-lease
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'git push\b.*(-f\b|--force\b|--force-with-lease)'; then
  if echo "$COMMAND" | grep -qE '(origin|upstream)\s+(main|master|develop)\b'; then
    deny "Force-pushing to main/develop is forbidden. See /git-workflow for rules."
  fi
  BRANCH=$(current_branch)
  if [ "$BRANCH" = "main" ] || [ "$BRANCH" = "master" ] || [ "$BRANCH" = "develop" ]; then
    deny "Force-pushing while on '$BRANCH' is forbidden. See /git-workflow for rules."
  fi
  if echo "$COMMAND" | grep -qE -- '(-f\b|--force\b)' && ! echo "$COMMAND" | grep -q -- '--force-with-lease'; then
    deny "Use '--force-with-lease' instead of '--force' for feature branches. See /git-workflow for rules."
  fi
fi

# ---------------------------------------------------------------------------
# 4. No direct push to main/develop
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'git push\b'; then
  # Explicit push to protected branch
  if echo "$COMMAND" | grep -qE '(origin|upstream)\s+(main|master|develop)\b'; then
    deny "Pushing directly to main/develop is forbidden. Push to a feature branch and create a PR. See /git-workflow for rules."
  fi
  # Implicit push while on protected branch (no explicit refspec)
  BRANCH=$(current_branch)
  if [ "$BRANCH" = "main" ] || [ "$BRANCH" = "master" ] || [ "$BRANCH" = "develop" ]; then
    # Allow "git push -u origin feat/123-..." (explicit feature branch ref)
    if ! echo "$COMMAND" | grep -qE 'origin\s+[a-z]+/[0-9]'; then
      # Allow "git push --delete" (deleting remote branches)
      if ! echo "$COMMAND" | grep -q -- '--delete'; then
        deny "Pushing while on '$BRANCH' is forbidden. Switch to a feature branch first. See /git-workflow for rules."
      fi
    fi
  fi
fi

# ---------------------------------------------------------------------------
# 5. PRs must target develop (not main) and be explicit about --base
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'gh pr create\b'; then
  if echo "$COMMAND" | grep -qE -- '--base\s+(main|master)\b'; then
    deny "PRs must target 'develop', not 'main'. See /git-workflow for rules."
  fi
  if ! echo "$COMMAND" | grep -qE -- '--base\b'; then
    deny "PRs must explicitly specify '--base develop'. See /git-workflow for rules."
  fi
fi

# ---------------------------------------------------------------------------
# 6. Merges must use --squash
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'gh pr merge\b'; then
  if ! echo "$COMMAND" | grep -q -- '--squash'; then
    deny "PRs must be squash-merged ('--squash'). See /git-workflow for rules."
  fi
fi

# ---------------------------------------------------------------------------
# 7. No rebase of protected branches (except pulling develop onto itself)
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'git rebase\b'; then
  BRANCH=$(current_branch)
  if [ "$BRANCH" = "main" ] || [ "$BRANCH" = "master" ]; then
    deny "Rebasing '$BRANCH' is forbidden. See /git-workflow for rules."
  fi
  if [ "$BRANCH" = "develop" ]; then
    if ! echo "$COMMAND" | grep -qE 'rebase origin/develop'; then
      deny "Rebasing 'develop' is only allowed with 'origin/develop'. See /git-workflow for rules."
    fi
  fi
fi

# ---------------------------------------------------------------------------
# 8. Branch creation must not be based on main
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE '(git checkout -b|git switch -c) '; then
  if echo "$COMMAND" | grep -qE '(git checkout -b|git switch -c) [^ ]+ (main|master)$'; then
    deny "Branches must be based on 'develop', not 'main'. See /git-workflow for rules."
  fi
fi

# All checks passed
exit 0
