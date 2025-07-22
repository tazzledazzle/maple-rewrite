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
- Complex project templates.
- Cross-host (GitLab/Bitbucket).

## User Stories
1. As a release engineer, I can run `maple fixtures init --count 20 --remote github` and immediately have 20 repos to test.
2. As a dev, I can generate temp local repos under `/tmp/maple-fixtures` without touching GitHub.

## Functional Requirements
- FR1: `maple fixtures init|destroy|list` subcommands.
- FR2: GitHub mode uses `gh repo create/delete`; local mode uses bare repos on disk.
- FR3: Emit a BOM JSON describing generated repos.
- FR4: Deterministic versioning (single version or matrix).
- FR5: Optional build command injection.
- FR6: Clean teardown.

## NFR
- Linux/macOS portability.
- Offline local mode.
- No secrets printed.

## Design
### Components
- `fixtures` package:
  - FixtureConfig loader (YAML/JSON).
  - RepoScaffolder (files, gradle wrapper optional).
  - Drivers: LocalGitDriver, GitHubDriver (`gh` CLI).
  - BomWriter.

### Flow: init
1. Load config or synthesize defaults.
2. Scaffold files.
3. Init git repo, commit, tag, push (if remote).
4. Append to BOM JSON.

### Flow: destroy
1. Read state/config.
2. Delete local dirs or GitHub repos.

## CLI
```
maple fixtures init    --config fixtures.yml
maple fixtures destroy --config fixtures.yml
maple fixtures list    --config fixtures.yml
```

## Config (fixtures.yml)
```yaml
root: /tmp/maple-fixtures
remote: github        # or local
org: tazzledazzle
repo_prefix: maple-fixture-
count: 10
version: 1.0.0
build:
  command: "./gradlew build"
   ```

## Test Plan
- Unit: config parser, path utils.
- Integration: local mode in CI, assert repos & BOM.
- Manual/nightly: GitHub mode.

## Risks
- GH rate limits → throttle.
- Cleanup safety → guard with prefix checks.

## Future
- Templates per language.
- GitLab driver.