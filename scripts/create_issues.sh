#!/usr/bin/env bash
set -euo pipefail

# Requires: gh CLI, GH_TOKEN or logged-in via gh auth login

REPO="tazzledazzle/maple-rewrite"
MILESTONE_TITLE="MVP"
# Create milestone if missing
MILESTONE_NUMBER=$(gh api repos/$REPO/milestones --jq ".[] | select(.title==\"$MILESTONE_TITLE\") | .number")
if [[ -z "${MILESTONE_NUMBER:-}" ]]; then
  MILESTONE_NUMBER=$(gh api repos/$REPO/milestones -f title="$MILESTONE_TITLE" -f state=open --jq '.number')
fi

create_issue () {
  local title="$1"
  local body="$2"
  local labels="$3"
  gh issue create --repo "$REPO" --title "$title" --body "$body" --label $labels --milestone "$MILESTONE_NUMBER" >/dev/null
  echo "Opened: $title"
}

## --- Epics ---
#create_issue "'Epic 0' – Repo Hygiene & Docs" "Goal: baseline project hygiene & documentation.\n\nAcceptance:\n- docs present\n- CI running\n- contribution guidelines in place" "epic,docs,infra"
#create_issue "'Epic 1' – CLI & Config" "Goal: usable CLI with config.\n\nAcceptance:\n- plan/run/resume commands\n- config precedence implemented" "epic"
#create_issue "'Epic 2' – BOM Parsing" "Goal: robust JSON BOM parser.\n\nAcceptance:\n- schema defined\n- parser + tests" "epic"
#create_issue "Epic 3 – Repo Manager" "Goal: reliable git operations.\n\nAcceptance:\n- clone/checkout/tag/push with retries" "epic"
#create_issue "Epic 4 – Build Runner" "Goal: build/test execution w/ logs.\n\nAcceptance:\n- timeout & log capture" "epic"
#create_issue "Epic 5 – Orchestrator & State" "Goal: parallel execution & resume.\n\nAcceptance:\n- JSON/SQLite state store\n- resume/dry-run" "epic"
#create_issue "Epic 6 – Reporting & Notifications" "Goal: structured reports & optional webhooks." "epic"
#create_issue "Epic 7 – Advanced Features" "Goal: DAG, multi-BOM, plugins, native image." "epic"
#create_issue "Epic 8 – Release & Adoption" "Goal: versioning, releases, demos." "epic,docs"
#
## --- Tasks (link them back to epics manually or with gh issue edit --add-project etc.) ---
#create_issue "Add CONTRIBUTING.md & CODE_OF_CONDUCT.md" "Docs task." "task,docs"
#create_issue "Enhance README with quickstart + architecture diagram" "" "task,docs"
#create_issue "Pick CLI library (picocli vs clikt) and scaffold" "" "task"
#create_issue "Implement 'maple plan' command" "" "task"
#create_issue "Implement .maple.yml config loader & precedence" "" "task"
#create_issue "Define JSON BOM schema" "" "task"
#create_issue "Implement JsonBomParser + validation" "" "task"
#create_issue "Parser unit tests (happy path/malformed)" "" "task,test"
#create_issue "Choose git integration (JGit vs shell) + abstraction" "" "task"
#create_issue "Implement clone/fetch/checkout/tag/push with retries" "" "task"
#create_issue "Integration tests with temp repos" "" "task,test"
#create_issue "Detect/override build commands per repo" "" "task"
#create_issue "Exec build with timeout, capture logs" "" "task"
#create_issue "Map exit codes to statuses" "" "task"
#create_issue "Dummy gradle project tests" "" "task,test"
#create_issue "Define state machine + JSON/SQLite store" "" "task"
#create_issue "Implement parallel executor & resume flag" "" "task"
#create_issue "Implement dry-run mode" "" "task"
#create_issue "JSON summary writer & pretty console table" "" "task"
#create_issue "Slack/Webhook notifier" "" "task"
#create_issue "Golden file tests for summary output" "" "task,test"
#create_issue "Dependency DAG builder (toposort)" "" "task"
#create_issue "Multi-BOM diff/merge command" "" "task"
#create_issue "Plugin API for custom steps" "" "task"
#create_issue "GraalVM native-image build" "" "task"
#create_issue "Semver & release workflow" "" "task,infra"
#create_issue "Changelog automation (release-please or similar)" "" "task,infra"
#create_issue "Sample repos + demo script" "" "task,docs"
#create_issue "Internal adoption guide" "" "task,docs"


# Append to your existing create_issues.sh
#create_issue "Epic 9 – Multi-Repo Sandbox Generator" "Goal: programmatic creation/destruction of test repos for Maple.\n\nAC:\n- CLI or scripts to init/destroy/list fixtures\n- BOM auto-generated\n- Works locally and on GitHub via gh" "epic,infra,test"
#
#create_issue "Spec doc for sandbox generator" "Add docs/feature-multi-repo-sandbox.md with requirements/design." "task,docs"
#create_issue "Implement local fixtures generator (bash or Kotlin)" "Create /scripts/fixtures_local.sh and ensure CI uses it." "task,test"
#create_issue "Implement GitHub fixtures generator" "Use gh CLI to create/delete repos; handle BOM output." "task,test,infra"
#create_issue "Integrate fixtures command into Maple CLI" "maple fixtures init/destroy/list wrappers around scripts or Kotlin impl." "task"
#create_issue "Auto-generate BOM from fixtures run" "Emit JSON with repoUrl/version/buildCmd." "task"
#create_issue "Add CI job to spin up local fixtures and run E2E test" "Use local mode to validate run end-to-end." "task,test,infra"
create_issue "Teardown safety checks & guards" "Prevent accidental deletion of non-fixture repos." "task,security"
