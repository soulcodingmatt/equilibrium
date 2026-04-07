#!/bin/bash
# =============================================================================
# validate-naming.sh — Claude Code PreToolUse hook
# Enforces /conventional-naming rules on git commits, branches, issues, and PRs.
# Uses python3 for JSON parsing (no jq dependency).
# =============================================================================

set -euo pipefail

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('command',''))" 2>/dev/null || echo "")

if [ -z "$COMMAND" ]; then
  exit 0
fi

# ---------------------------------------------------------------------------
# Only enforce rules when operating on the project's own git repo
# ---------------------------------------------------------------------------
CWD=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('cwd',''))" 2>/dev/null || echo "")
PROJECT_DIR="${CWD:-${CLAUDE_PROJECT_DIR:-.}}"
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
# Allowed conventional types
# ---------------------------------------------------------------------------
TYPES="feat|fix|refactor|test|docs|chore|perf|style|ci|build"
EXTENDED_TYPES="${TYPES}|proposal|epic|migration"

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
# 1. Validate git commit messages
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'git commit\b'; then

  # Extract the first line of the commit message using python3 for reliable parsing
  MESSAGE=$(echo "$COMMAND" | python3 -c "
import sys, re
cmd = sys.stdin.read()
# Try heredoc: cat <<'EOF'\nmessage\n...\nEOF
m = re.search(r\"cat <<['\\\"]?EOF['\\\"]?\\n(.+?)\\n\", cmd)
if m:
    print(m.group(1).strip())
    sys.exit(0)
# Try -m 'message' or -m \"message\" (single line, no heredoc)
m = re.search(r'-m [\"\\x27]([^\"\\x27]*)[\"\\x27]', cmd)
if m and '\$(cat' not in m.group(1):
    print(m.group(1).strip())
    sys.exit(0)
# Try -m \"message\" where message doesn't contain heredoc
m = re.search(r'-m \"([^\"]+)\"', cmd)
if m and '\$(cat' not in m.group(1):
    print(m.group(1).strip())
    sys.exit(0)
" 2>/dev/null || echo "")

  if [ -n "$MESSAGE" ]; then
    # Trim leading whitespace
    MESSAGE=$(echo "$MESSAGE" | sed 's/^[[:space:]]*//')

    # Must start with a valid type prefix followed by colon
    if ! echo "$MESSAGE" | grep -qE "^(${TYPES})(\\(.+\\))?: "; then
      deny "Commit message must start with a conventional type prefix (e.g. 'feat: ', 'fix: '). Got: '${MESSAGE:0:60}'. See /conventional-naming for rules."
    fi

    # Description after type must start with lowercase
    DESCRIPTION=$(echo "$MESSAGE" | sed -E "s/^(${TYPES})(\\(.+\\))?: //")
    FIRST_CHAR=$(echo "$DESCRIPTION" | cut -c1)
    if echo "$FIRST_CHAR" | grep -qE '[A-Z]'; then
      deny "Commit description must start with a lowercase imperative verb. Got: '${DESCRIPTION:0:40}'. See /conventional-naming for rules."
    fi

    # Must contain an issue reference
    if ! echo "$MESSAGE" | grep -qE '(refs|closes|fixes) #[0-9]+'; then
      deny "Commit message must include an issue reference (e.g. ', closes #123'). Got: '${MESSAGE:0:60}'. See /conventional-naming for rules."
    fi
  fi
fi

# ---------------------------------------------------------------------------
# 2. Validate branch names (git checkout -b, git switch -c)
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE '(git checkout -b|git switch -c) '; then
  BRANCH=$(echo "$COMMAND" | grep -oE '(git checkout -b|git switch -c) [^ ]+' | rev | cut -d' ' -f1 | rev)

  if [ -n "$BRANCH" ]; then
    if ! echo "$BRANCH" | grep -qE "^(${TYPES})/[0-9]+-[a-z0-9-]+$"; then
      deny "Branch name must follow '<type>/<issue-number>-<description>' format (lowercase, hyphens). Got: '${BRANCH}'. See /conventional-naming for rules."
    fi
  fi
fi

# ---------------------------------------------------------------------------
# 3. Validate issue titles (gh issue create --title "...")
#
# Title rules differ by issue type (detected via --label):
#   - task:  must start with <type>: <lowercase description>
#   - epic:  human-readable, lowercase start, no type prefix required
#   - story: human-readable, lowercase start, no type prefix required
#   - (no hierarchy label): treated as task (requires type prefix)
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'gh issue create\b'; then
  # Extract title — handle both direct strings and variable interpolation
  TITLE=""
  if echo "$COMMAND" | grep -qE -- '--title ["\x27]'; then
    TITLE=$(echo "$COMMAND" | sed -n "s/.*--title ['\"]\\([^'\"]*\\)['\"].*/\\1/p" | head -1)
  fi

  # Skip validation if title is empty or contains unresolved variables
  if [ -n "$TITLE" ] && ! echo "$TITLE" | grep -qE '^\$'; then
    # Detect issue type from labels
    LABELS=""
    if echo "$COMMAND" | grep -qE -- '--label ["\x27]'; then
      LABELS=$(echo "$COMMAND" | sed -n "s/.*--label ['\"]\\([^'\"]*\\)['\"].*/\\1/p" | head -1)
    fi

    IS_EPIC=false
    IS_STORY=false
    if echo "$LABELS" | grep -qw "epic"; then
      IS_EPIC=true
    fi
    if echo "$LABELS" | grep -qw "story"; then
      IS_STORY=true
    fi

    if [ "$IS_EPIC" = true ] || [ "$IS_STORY" = true ]; then
      # Epic/Story: human-readable title, must start with lowercase
      FIRST_CHAR=$(echo "$TITLE" | cut -c1)
      if echo "$FIRST_CHAR" | grep -qE '[A-Z]'; then
        deny "Epic/Story title must start with lowercase. Got: '${TITLE:0:60}'. See /conventional-naming and /create-gh-tree for rules."
      fi
    else
      # Task or untyped issue: must start with type prefix
      if ! echo "$TITLE" | grep -qE "^(${EXTENDED_TYPES}): "; then
        deny "Issue title must start with a type prefix (e.g. 'fix: ', 'feat: '). Got: '${TITLE:0:60}'. See /conventional-naming for rules."
      fi

      DESCRIPTION=$(echo "$TITLE" | sed -E "s/^(${EXTENDED_TYPES}): //")
      FIRST_CHAR=$(echo "$DESCRIPTION" | cut -c1)
      if echo "$FIRST_CHAR" | grep -qE '[A-Z]'; then
        deny "Issue title description must start with lowercase. Got: '${TITLE:0:60}'. See /conventional-naming for rules."
      fi
    fi
  fi
fi

# ---------------------------------------------------------------------------
# 4. Validate PR titles (gh pr create --title "...")
# ---------------------------------------------------------------------------
if echo "$COMMAND" | grep -qE 'gh pr create\b'; then
  TITLE=""
  if echo "$COMMAND" | grep -qE -- '--title ["\x27]'; then
    TITLE=$(echo "$COMMAND" | sed -n "s/.*--title ['\"]\\([^'\"]*\\)['\"].*/\\1/p" | head -1)
  fi

  if [ -n "$TITLE" ]; then
    if ! echo "$TITLE" | grep -qE "^(${TYPES}): "; then
      deny "PR title must start with a type prefix (e.g. 'fix: ', 'feat: '). Got: '${TITLE:0:60}'. See /conventional-naming for rules."
    fi

    DESCRIPTION=$(echo "$TITLE" | sed -E "s/^(${TYPES}): //")
    FIRST_CHAR=$(echo "$DESCRIPTION" | cut -c1)
    if echo "$FIRST_CHAR" | grep -qE '[A-Z]'; then
      deny "PR title description must start with lowercase. Got: '${TITLE:0:60}'. See /conventional-naming for rules."
    fi

    if [ ${#TITLE} -gt 70 ]; then
      deny "PR title must be under 70 characters (got ${#TITLE}). See /conventional-naming for rules."
    fi
  fi
fi

# All checks passed
exit 0
