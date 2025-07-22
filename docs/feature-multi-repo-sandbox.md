# Feature: Multi-Repo Sandbox Generator

## Summary
Provide a first-class way to create, update, and tear down a fleet of small git repositories for exercising Maple’s release-branching logic. Supports:
- **Local-only** repos (fast CI/integration testing).
- **Remote GitHub** repos (via `gh` CLI) to verify push/branch/tag flows.

## Goals
- One command (or script) produces N repos with predictable structure, versions, and optional inter-dependencies.
- Repeatable + idempotent: safe to recreate/destroy.
- Works headless in CI (no manual GitHub clicks).

## Non-Goals (v1)
- Complex project templates (keep skeletons tiny).
- Cross-host (GitLab/Bitbucket). GitHub + local only.

## User Stories
1. *As a release engineer*, I can run `maple fixtures init --count 20 --remote github` and immediately have 20 repos for a dry-run or real run.
2. *As a dev*, I can run the same command locally to get temp repos under `/tmp/maple-fixtures`, without touching GitHub.

## Functional Requirements
- FR1: CLI command(s) to generate fixture repos:
  - `maple fixtures init [--config fixtures.yml] [--count N] [--remote github|local]`
  - `maple fixtures destroy [--config fixtures.yml]`
  - `maple fixtures list`
- FR2: For GitHub mode, use `gh repo create` (private by default) and push initial commits/tags.
- FR3: For local mode, create bare repos + working dirs in a temp root.
- FR4: Emit a BOM file describing these repos (names, versions, URLs).
- FR5: Allow deterministic versioning (e.g., all repos at v1.2.3) or a version matrix.
- FR6: Support optional build cmd injection (e.g., Gradle wrapper vs dummy script).
- FR7: Provide a clean teardown (delete local dirs or call `gh repo delete`).

## Non-Functional Requirements
- Portable to Linux/macOS.
- Runs offline for local mode.
- No secrets printed (GitHub token handled by `gh`).

## High-Level Design
### Components
- `fixtures` module (or package):
  - **FixtureConfigLoader**: parse YAML/JSON config.
  - **RepoScaffolder**: generate file tree (build files, trivial source).
  - **GitDriver**: thin wrapper (JGit or `git` CLI) for local repos.
  - **GitHubDriver**: wrapper around `gh` CLI.
  - **BomWriter**: outputs a BOM JSON for Maple runs.

### Flow: `init`
1. Load config or synthesize default N repos.
2. For each repo:
   - Scaffold files (LICENSE, README, build script).
   - Init git repo, commit, tag (e.g., 1.0.0).
   - Push to GitHub (if remote).
3. Aggregate metadata → write `fixtures-bom.json`.

### Flow: `destroy`
1. Read config/state file to know what to delete.
2. For local repos: remove dirs.
3. For GitHub: call `gh repo delete -y`.

### Config Schema (fixtures.yml)
```yaml
root: /tmp/maple-fixtures          # local path root
remote: github                     # or local
org: tazzledazzle                  # for GitHub (or username)
repo_prefix: maple-fixture-
count: 10
version: 1.0.0
build:
  type: gradle                     # or "dummy"
  command: "./gradlew build"
dependencies:
  # optional: map repo -> list of deps
  maple-fixture-03: [maple-fixture-01, maple-fixture-02]
```

### CLI Surface (proposed)

```
maple fixtures init    --config fixtures.yml
maple fixtures destroy --config fixtures.yml
maple fixtures list    --config fixtures.yml
```

### Test Plan

- Unit: config parser, scaffolder path logic.
- Integration: local mode in CI, assert repos & BOM exist.
- Manual/optional: GitHub mode (nightly builds or guarded)
- E2E: run Maple against generated BOM and ensure branches/tags appear.

### Risks

- GitHub rate limits (mitigate via throttling).
- Cleanup mistakes (ensure we only delete what we created).
- `gh` CLI availability (detect & bail with helpful message).

### Future

- Support repo template (Java/Go/Rust variants).
- Seed inter-repo Gradle composite builds.
- GitLab driver.

----

## 2. Patch to add the doc + simple scripts

Add this on a new branch, e.g. `feature/fixtures-sandbox`:

```bash
git checkout -b feature/fixtures-sandbox
```


