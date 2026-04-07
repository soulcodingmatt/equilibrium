#!/usr/bin/env bash
# issue-hygiene.sh — GitHub issue hygiene automation
#
# Subcommands:
#   on-sub-issue-create <parent-number> <child-number>
#   on-pre-close <issue-number>
#   on-post-merge <issue-number>
#   on-follow-up-closed <issue-number>
#   sweep [--thorough] [--no-fix]
set -euo pipefail

# --- Dependency checks ---

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) required" >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq required" >&2
  exit 1
fi

# --- Resolve configuration ---

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
CONFIG="$PROJECT_ROOT/.claude/project-config.json"

REPO=$(python3 -c "import json; print(json.load(open('$CONFIG'))['project']['repo'])")

resolve_workflow_dir() {
  local raw_dir
  raw_dir=$(python3 -c "import json; print(json.load(open('$CONFIG'))['github']['workflowDir'])" 2>/dev/null || true)
  echo "${raw_dir/#\~/$HOME}"
}

WORKFLOW_DIR=$(resolve_workflow_dir)

if [[ -z "$WORKFLOW_DIR" || ! -d "$WORKFLOW_DIR/scripts" ]]; then
  echo "Workflow directory not found: $WORKFLOW_DIR" >&2
  exit 1
fi

# --- Shared helpers ---

get_project_status() {
  local issue_number="$1"

  if [[ -z "${PROJECT_ID:-}" || -z "${STATUS_FIELD_ID:-}" ]]; then
    echo "Todo"
    return
  fi

  local response
  response=$(gh api graphql --repo "$REPO" -f query='
query($project: ID!, $statusField: ID!) {
  node(id: $project) {
    ... on ProjectV2 {
      items(first: 100) {
        nodes {
          content {
            ... on Issue {
              number
            }
          }
          fieldValues(first: 20) {
            nodes {
              ... on ProjectV2ItemFieldSingleSelectValue {
                name
                field {
                  ... on ProjectV2SingleSelectField {
                    id
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}' -f project="$PROJECT_ID" -f statusField="$STATUS_FIELD_ID" 2>/dev/null) || {
    echo "Todo"
    return
  }

  local status
  status=$(echo "$response" | jq -r --argjson num "$issue_number" \
    '[.data.node.items.nodes[] | select(.content.number != null) | select(.content.number == $num) | [.fieldValues.nodes[] | select(.field.id != null) | select(.field.id == "'"$STATUS_FIELD_ID"'")] | first | .name // "Todo"] | first // "Todo"')

  echo "${status:-Todo}"
}

get_open_followup_children() {
  local issue_number="$1"

  local hierarchy
  hierarchy=$("$WORKFLOW_DIR/scripts/find-issue-hierarchy.sh" --no-cache "$issue_number") || {
    echo "[]"
    return
  }

  local children
  children=$(echo "$hierarchy" | jq -c '.children // []')
  local children_count
  children_count=$(echo "$children" | jq 'length')

  if [[ "$children_count" -eq 0 ]]; then
    echo "[]"
    return
  fi

  local followup_children="[]"

  while IFS= read -r child_number; do
    [[ -z "$child_number" ]] && continue

    local child_data
    child_data=$(gh api "repos/${REPO}/issues/${child_number}" \
      --jq '{state: .state, labels: [.labels[].name]}' 2>/dev/null) || continue

    local state
    state=$(echo "$child_data" | jq -r '.state')
    local has_followup
    has_followup=$(echo "$child_data" | jq 'any(.labels[]; . == "follow-up")')

    if [[ "$state" == "open" && "$has_followup" == "true" ]]; then
      followup_children=$(echo "$followup_children" | jq --argjson n "$child_number" '. + [$n]')
    fi
  done < <(echo "$children" | jq -r '.[]')

  echo "$followup_children"
}

parse_blocker_refs() {
  local body="$1"
  echo "$body" | grep -oiP '(?:blocked by|depends on|is blocked by)\s+#\K[0-9]+' | sort -u || true
}

# --- Subcommands ---

cmd_on_sub_issue_create() {
  if [[ $# -ne 2 ]]; then
    echo "Usage: $0 on-sub-issue-create <parent-number> <child-number>" >&2
    exit 1
  fi

  local parent_number="$1"
  local child_number="$2"

  local status
  status=$(get_project_status "$parent_number")

  if [[ "$status" != "In Progress" ]]; then
    exit 0
  fi

  echo "Parent #$parent_number is In Progress, adding follow-up labels" >&2
  gh issue edit "$child_number" --repo "$REPO" --add-label "follow-up" >&2
  gh issue edit "$parent_number" --repo "$REPO" --add-label "needs-follow-up" >&2
}

cmd_on_pre_close() {
  if [[ $# -ne 1 ]]; then
    echo "Usage: $0 on-pre-close <issue-number>" >&2
    exit 1
  fi

  local issue_number="$1"

  local open_followups
  open_followups=$(get_open_followup_children "$issue_number")
  local count
  count=$(echo "$open_followups" | jq 'length')

  if [[ "$count" -gt 0 ]]; then
    echo "Cannot close #$issue_number: $count open follow-up sub-issue(s) remain:" >&2
    echo "$open_followups" | jq -r '.[] | "  - #\(.)"' >&2
    echo "Resolve follow-up issues before closing." >&2
    exit 1
  fi

  exit 0
}

cmd_on_post_merge() {
  if [[ $# -ne 1 ]]; then
    echo "Usage: $0 on-post-merge <issue-number>" >&2
    exit 1
  fi

  local issue_number="$1"

  local open_followups
  open_followups=$(get_open_followup_children "$issue_number")
  local count
  count=$(echo "$open_followups" | jq 'length')

  if [[ "$count" -gt 0 ]]; then
    gh issue edit "$issue_number" --repo "$REPO" --add-label "needs-follow-up" >&2
    echo "Issue #$issue_number has $count open follow-up sub-issue(s) — do NOT close this issue yet." >&2
    echo "FOLLOW_UP_PENDING"
  else
    echo "NO_FOLLOW_UPS"
  fi
}

cmd_on_follow_up_closed() {
  if [[ $# -ne 1 ]]; then
    echo "Usage: $0 on-follow-up-closed <issue-number>" >&2
    exit 1
  fi

  local issue_number="$1"

  # Check if the closed issue has follow-up label
  local labels
  labels=$(gh api "repos/${REPO}/issues/${issue_number}" \
    --jq '[.labels[].name]' 2>/dev/null) || {
    echo "Failed to fetch issue #$issue_number" >&2
    exit 1
  }

  local has_followup
  has_followup=$(echo "$labels" | jq 'any(.[]; . == "follow-up")')

  if [[ "$has_followup" != "true" ]]; then
    exit 0
  fi

  # Find parent
  local hierarchy
  hierarchy=$("$WORKFLOW_DIR/scripts/find-issue-hierarchy.sh" --no-cache "$issue_number") || {
    echo "Failed to get hierarchy for #$issue_number" >&2
    exit 1
  }

  local parent
  parent=$(echo "$hierarchy" | jq -r '.parent // empty')

  if [[ -z "$parent" || "$parent" == "null" ]]; then
    echo "Follow-up #$issue_number closed but has no parent" >&2
    exit 0
  fi

  # Check remaining open follow-ups on parent
  local remaining
  remaining=$(get_open_followup_children "$parent")
  local remaining_count
  remaining_count=$(echo "$remaining" | jq 'length')

  if [[ "$remaining_count" -eq 0 ]]; then
    echo "All follow-up sub-issues of #$parent are resolved" >&2
    gh issue edit "$parent" --repo "$REPO" --remove-label "needs-follow-up" >&2 || true
    "$WORKFLOW_DIR/scripts/evaluate-issue-status.sh" "$parent" >&2 || true
  else
    echo "#$parent still has $remaining_count open follow-up sub-issue(s)" >&2
  fi
}

# --- Autofix helpers ---

infer_hierarchy_label() {
  local title="$1"
  if echo "$title" | grep -qiP '^epic:\s'; then
    echo "epic"
  elif echo "$title" | grep -qiP '^story:\s'; then
    echo "story"
  elif echo "$title" | grep -qP '^\w+:\s'; then
    echo "task"
  else
    echo ""
  fi
}

autofix_labels() {
  local number="$1" label_names="$2" title="$3"
  local fixed=()

  # Fix missing hierarchy label
  local has_task has_story has_epic
  has_task=$(echo "$label_names" | jq 'any(.[]; . == "task")')
  has_story=$(echo "$label_names" | jq 'any(.[]; . == "story")')
  has_epic=$(echo "$label_names" | jq 'any(.[]; . == "epic")')

  if [[ "$has_task" != "true" && "$has_story" != "true" && "$has_epic" != "true" ]]; then
    local inferred
    inferred=$(infer_hierarchy_label "$title")
    if [[ -n "$inferred" ]]; then
      gh issue edit "$number" --repo "$REPO" --add-label "$inferred" >&2
      fixed+=("added '$inferred' label")
    fi
  fi

  # Fix missing area label — default to backend for this project
  local has_area
  has_area=$(echo "$label_names" | jq 'any(.[]; . == "backend" or . == "frontend" or . == "infrastructure" or . == "devops" or . == "database")')
  if [[ "$has_area" != "true" ]]; then
    local default_area
    default_area=$(python3 -c "import json; print(json.load(open('$CONFIG')).get('github',{}).get('defaultAreaLabel','backend'))" 2>/dev/null || echo "backend")
    gh issue edit "$number" --repo "$REPO" --add-label "$default_area" >&2
    fixed+=("added '$default_area' area label")
  fi

  if [[ ${#fixed[@]} -gt 0 ]]; then
    local joined
    joined=$(IFS=", "; echo "${fixed[*]}")
    echo "$joined"
  fi
}

autofix_body() {
  local number="$1" body="$2" has_epic="$3" has_story="$4"
  local appended=""

  if [[ "$has_epic" == "true" ]]; then
    if ! echo "$body" | grep -qiP '(^|\n)\s*(goal|objective)\s*:'; then
      appended+="\n\n## Goal\n\n_TODO: Define the goal of this epic._"
    fi
    if ! echo "$body" | grep -qiP '(^|\n)\s*success\s*criteria\s*:'; then
      appended+="\n\n## Success Criteria\n\n_TODO: Define success criteria for this epic._"
    fi
  elif [[ "$has_story" == "true" ]]; then
    if ! echo "$body" | grep -qiP '(^|\n)\s*acceptance\s*criteria\s*:'; then
      appended+="\n\n## Acceptance Criteria\n\n_TODO: Define acceptance criteria for this story._"
    fi
  fi

  if [[ -n "$appended" ]]; then
    local new_body="${body}$(echo -e "$appended")"
    gh issue edit "$number" --repo "$REPO" --body "$new_body" >&2
    echo "appended missing body sections"
  fi
}

cmd_sweep() {
  local issues_found=false

  # --- Step 1: Unblock issues whose blockers are all closed ---

  echo "Step 1: Checking blocked issues..." >&2

  local blocked_issues
  blocked_issues=$(gh issue list --repo "$REPO" --state all --label "blocked" \
    --json number,body --limit 200 2>/dev/null) || blocked_issues="[]"

  local blocked_count
  blocked_count=$(echo "$blocked_issues" | jq 'length')

  if [[ "$blocked_count" -gt 0 ]]; then
    while IFS= read -r entry; do
      local number
      number=$(echo "$entry" | jq -r '.number')
      local body
      body=$(echo "$entry" | jq -r '.body // ""')

      local blocker_refs
      blocker_refs=$(parse_blocker_refs "$body")

      # If no blocker refs in body, check issue state — stale label on closed issues
      if [[ -z "$blocker_refs" ]]; then
        local issue_state
        issue_state=$(gh api "repos/${REPO}/issues/${number}" --jq '.state' 2>/dev/null) || continue
        if [[ "$issue_state" == "closed" ]]; then
          echo "  Removing stale 'blocked' from closed #$number (no blocker references)" >&2
          gh issue edit "$number" --repo "$REPO" --remove-label "blocked" >&2 || true
        fi
        continue
      fi

      local all_closed=true
      while IFS= read -r ref; do
        [[ -z "$ref" ]] && continue
        local ref_state
        ref_state=$(gh api "repos/${REPO}/issues/${ref}" --jq '.state' 2>/dev/null) || {
          all_closed=false
          break
        }
        if [[ "$ref_state" != "closed" ]]; then
          all_closed=false
          break
        fi
      done <<< "$blocker_refs"

      if [[ "$all_closed" == true ]]; then
        echo "  Unblocking #$number — all blockers are closed" >&2
        gh issue edit "$number" --repo "$REPO" --remove-label "blocked" >&2 || true
      fi
    done < <(echo "$blocked_issues" | jq -c '.[]')
  fi

  echo "  Checked $blocked_count blocked issue(s)" >&2

  # --- Step 2: Clean needs-follow-up on open issues ---

  echo "Step 2: Checking open issues with needs-follow-up..." >&2

  local needs_followup_open
  needs_followup_open=$(gh issue list --repo "$REPO" --state open --label "needs-follow-up" \
    --json number --limit 200 2>/dev/null) || needs_followup_open="[]"

  local nfo_count
  nfo_count=$(echo "$needs_followup_open" | jq 'length')

  while IFS= read -r entry; do
    [[ -z "$entry" || "$entry" == "null" ]] && continue
    local number
    number=$(echo "$entry" | jq -r '.number')

    local open_followups
    open_followups=$(get_open_followup_children "$number")
    local count
    count=$(echo "$open_followups" | jq 'length')

    if [[ "$count" -eq 0 ]]; then
      echo "  Removing needs-follow-up from #$number — all follow-ups resolved" >&2
      gh issue edit "$number" --repo "$REPO" --remove-label "needs-follow-up" >&2 || true
    fi
  done < <(echo "$needs_followup_open" | jq -c '.[]')

  echo "  Checked $nfo_count open issue(s) with needs-follow-up" >&2

  # --- Step 3: Clean needs-follow-up on closed issues ---

  echo "Step 3: Checking closed issues with needs-follow-up..." >&2

  local needs_followup_closed
  needs_followup_closed=$(gh issue list --repo "$REPO" --state closed --label "needs-follow-up" \
    --json number --limit 200 2>/dev/null) || needs_followup_closed="[]"

  local nfc_count
  nfc_count=$(echo "$needs_followup_closed" | jq 'length')

  while IFS= read -r entry; do
    [[ -z "$entry" || "$entry" == "null" ]] && continue
    local number
    number=$(echo "$entry" | jq -r '.number')

    local open_followups
    open_followups=$(get_open_followup_children "$number")
    local count
    count=$(echo "$open_followups" | jq 'length')

    if [[ "$count" -gt 0 ]]; then
      echo "  WARNING: Closed #$number still has $count open follow-up sub-issue(s)" >&2
    else
      echo "  Removing stale needs-follow-up from closed #$number" >&2
      gh issue edit "$number" --repo "$REPO" --remove-label "needs-follow-up" >&2 || true
    fi
  done < <(echo "$needs_followup_closed" | jq -c '.[]')

  echo "  Checked $nfc_count closed issue(s) with needs-follow-up" >&2

  # --- Step 4: Comprehensive compliance check (with optional autofix) ---

  local thorough="${1:-false}"
  local autofix="${2:-true}"

  if [[ "$thorough" == "true" ]]; then
    echo "Step 4: Thorough compliance check (all open issues)..." >&2
  else
    echo "Step 4: Checking unchecked issues for compliance..." >&2
  fi

  if [[ "$autofix" == "true" ]]; then
    echo "  Autofix enabled — will attempt to fix non-compliant issues" >&2
  fi

  local all_open_issues
  all_open_issues=$(gh issue list --repo "$REPO" --state open \
    --json number,title,labels,body --limit 500 2>/dev/null) || all_open_issues="[]"

  local non_compliant=()
  local auto_fixed=()
  local needs_refinement=()
  local checked_count=0
  local skipped_count=0

  while IFS= read -r entry; do
    [[ -z "$entry" || "$entry" == "null" ]] && continue
    local number title body label_names
    number=$(echo "$entry" | jq -r '.number')
    title=$(echo "$entry" | jq -r '.title')
    body=$(echo "$entry" | jq -r '.body // ""')
    label_names=$(echo "$entry" | jq -c '[.labels[].name]')

    # Skip checked issues in normal mode
    local is_checked
    is_checked=$(echo "$label_names" | jq 'any(.[]; . == "checked")')
    if [[ "$thorough" != "true" && "$is_checked" == "true" ]]; then
      skipped_count=$((skipped_count + 1))
      continue
    fi
    checked_count=$((checked_count + 1))

    local issues_for_this=()
    local fixes_for_this=()

    # --- Check 1: Hierarchy label ---
    local has_task has_story has_epic
    has_task=$(echo "$label_names" | jq 'any(.[]; . == "task")')
    has_story=$(echo "$label_names" | jq 'any(.[]; . == "story")')
    has_epic=$(echo "$label_names" | jq 'any(.[]; . == "epic")')

    if [[ "$has_task" != "true" && "$has_story" != "true" && "$has_epic" != "true" ]]; then
      issues_for_this+=("missing hierarchy label")
    fi

    # --- Check 2: Task title format ---
    if [[ "$has_task" == "true" ]]; then
      if ! echo "$title" | grep -qP '^\w+:\s'; then
        issues_for_this+=("task missing type prefix")
      fi
    fi

    # --- Check 3: Area label ---
    local has_area
    has_area=$(echo "$label_names" | jq 'any(.[]; . == "backend" or . == "frontend" or . == "infrastructure" or . == "devops" or . == "database")')
    if [[ "$has_area" != "true" ]]; then
      issues_for_this+=("missing area label")
    fi

    # --- Check 4: Body completeness ---
    local body_len=${#body}

    if [[ "$has_epic" == "true" ]]; then
      if ! echo "$body" | grep -qiP '(^|\n)\s*(goal|objective)\s*:'; then
        issues_for_this+=("epic missing Goal section")
      fi
      if ! echo "$body" | grep -qiP '(^|\n)\s*success\s*criteria\s*:'; then
        issues_for_this+=("epic missing Success Criteria")
      fi
    elif [[ "$has_story" == "true" ]]; then
      if ! echo "$body" | grep -qiP 'as\s+a\s'; then
        issues_for_this+=("story missing 'As a' user statement")
      fi
      if ! echo "$body" | grep -qiP '(^|\n)\s*acceptance\s*criteria\s*:'; then
        issues_for_this+=("story missing Acceptance Criteria")
      fi
    elif [[ "$has_task" == "true" ]]; then
      if [[ "$body_len" -lt 50 ]]; then
        issues_for_this+=("task has empty or too short description")
      fi
    fi

    if [[ "$has_task" != "true" && "$has_story" != "true" && "$has_epic" != "true" && "$body_len" -lt 20 ]]; then
      issues_for_this+=("empty or near-empty body")
    fi

    # --- Detect content gaps needing AI refinement ---
    local needs_content_refinement=false

    if [[ "$has_epic" == "true" ]]; then
      # Epic with TODO stubs or missing substantive content
      if echo "$body" | grep -qiP '_TODO:'; then
        needs_content_refinement=true
      fi
    elif [[ "$has_story" == "true" ]]; then
      # Story with TODO stubs or missing substantive content
      if echo "$body" | grep -qiP '_TODO:'; then
        needs_content_refinement=true
      fi
    elif [[ "$has_task" == "true" ]]; then
      # Task with thin description
      if [[ "$body_len" -lt 50 ]]; then
        needs_content_refinement=true
      fi
    fi

    # Also flag issues with no hierarchy (can't assess body) and very thin content
    if [[ "$has_task" != "true" && "$has_story" != "true" && "$has_epic" != "true" && "$body_len" -lt 100 ]]; then
      needs_content_refinement=true
    fi

    # --- Autofix if enabled ---
    if [[ "$autofix" == "true" && ${#issues_for_this[@]} -gt 0 ]]; then
      # Fix labels
      local label_fix
      label_fix=$(autofix_labels "$number" "$label_names" "$title")
      if [[ -n "$label_fix" ]]; then
        fixes_for_this+=("$label_fix")
        # Re-read labels after fix to get updated hierarchy for body fix
        label_names=$(gh api "repos/${REPO}/issues/${number}" --jq '[.labels[].name]' 2>/dev/null || echo "$label_names")
        has_task=$(echo "$label_names" | jq 'any(.[]; . == "task")')
        has_story=$(echo "$label_names" | jq 'any(.[]; . == "story")')
        has_epic=$(echo "$label_names" | jq 'any(.[]; . == "epic")')
      fi

      # Fix body
      local body_fix
      body_fix=$(autofix_body "$number" "$body" "$has_epic" "$has_story")
      if [[ -n "$body_fix" ]]; then
        fixes_for_this+=("$body_fix")
      fi
    fi

    # --- Track issues needing content refinement ---
    # Body stubs just added by autofix also need refinement
    if [[ "$autofix" == "true" && -n "${body_fix:-}" ]]; then
      needs_content_refinement=true
    fi

    if [[ "$needs_content_refinement" == true ]]; then
      needs_refinement+=("$number")
    fi

    # --- Collect results ---
    if [[ ${#fixes_for_this[@]} -gt 0 ]]; then
      local fix_joined
      fix_joined=$(IFS=", "; echo "${fixes_for_this[*]}")
      auto_fixed+=("#$number [fixed: $fix_joined]: $title")

      # Re-check remaining issues after fix — remove issues that were resolved
      local remaining_issues=()
      for issue in "${issues_for_this[@]}"; do
        case "$issue" in
          "missing hierarchy label")
            if [[ "$has_task" != "true" && "$has_story" != "true" && "$has_epic" != "true" ]]; then
              remaining_issues+=("$issue")
            fi
            ;;
          "missing area label")
            local area_now
            area_now=$(echo "$label_names" | jq 'any(.[]; . == "backend" or . == "frontend" or . == "infrastructure" or . == "devops" or . == "database")')
            if [[ "$area_now" != "true" ]]; then
              remaining_issues+=("$issue")
            fi
            ;;
          *"Goal"*|*"Success Criteria"*|*"Acceptance Criteria"*)
            # Body sections were appended if autofix_body ran
            if [[ -z "$body_fix" ]]; then
              remaining_issues+=("$issue")
            fi
            ;;
          *)
            remaining_issues+=("$issue")
            ;;
        esac
      done

      if [[ ${#remaining_issues[@]} -gt 0 ]]; then
        local joined
        joined=$(IFS="; "; echo "${remaining_issues[*]}")
        non_compliant+=("#$number [$joined]: $title")
      fi
    elif [[ ${#issues_for_this[@]} -gt 0 ]]; then
      local joined
      joined=$(IFS="; "; echo "${issues_for_this[*]}")
      non_compliant+=("#$number [$joined]: $title")
    fi
  done < <(echo "$all_open_issues" | jq -c '.[]')

  if [[ "$skipped_count" -gt 0 ]]; then
    echo "  Skipped $skipped_count already-checked issue(s)" >&2
  fi

  if [[ ${#auto_fixed[@]} -gt 0 ]]; then
    echo "  Auto-fixed ${#auto_fixed[@]} issue(s):" >&2
    for item in "${auto_fixed[@]}"; do
      echo "    - $item" >&2
    done
  fi

  if [[ ${#non_compliant[@]} -gt 0 ]]; then
    echo "  Found ${#non_compliant[@]} non-compliant issue(s) out of $checked_count checked (require manual fix):" >&2
    for item in "${non_compliant[@]}"; do
      echo "    - $item" >&2
    done
    echo "ISSUE_HYGIENE_REQUIRED"
  else
    echo "  All $checked_count checked issue(s) are compliant" >&2
  fi

  # --- Signal issues needing AI-powered content refinement ---
  if [[ ${#needs_refinement[@]} -gt 0 ]]; then
    local refinement_list
    refinement_list=$(IFS=","; echo "${needs_refinement[*]}")
    echo "  ${#needs_refinement[@]} issue(s) have thin or placeholder content and need refinement" >&2
    echo "ISSUE_REFINEMENT_NEEDED:$refinement_list"
  fi
}

# --- Main dispatch ---

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <subcommand> [args...]" >&2
  echo "" >&2
  echo "Subcommands:" >&2
  echo "  on-sub-issue-create <parent-number> <child-number>" >&2
  echo "  on-pre-close <issue-number>" >&2
  echo "  on-post-merge <issue-number>" >&2
  echo "  on-follow-up-closed <issue-number>" >&2
  echo "  sweep [--thorough] [--no-fix]" >&2
  exit 1
fi

SUBCOMMAND="$1"
shift

case "$SUBCOMMAND" in
  on-sub-issue-create)
    cmd_on_sub_issue_create "$@"
    ;;
  on-pre-close)
    cmd_on_pre_close "$@"
    ;;
  on-post-merge)
    cmd_on_post_merge "$@"
    ;;
  on-follow-up-closed)
    cmd_on_follow_up_closed "$@"
    ;;
  sweep)
    thorough_flag="false"
    autofix_flag="true"
    for arg in "$@"; do
      case "$arg" in
        --thorough) thorough_flag="true" ;;
        --no-fix)   autofix_flag="false" ;;
      esac
    done
    cmd_sweep "$thorough_flag" "$autofix_flag"
    ;;
  *)
    echo "Unknown subcommand: $SUBCOMMAND" >&2
    echo "Run '$0' without arguments for usage." >&2
    exit 1
    ;;
esac
