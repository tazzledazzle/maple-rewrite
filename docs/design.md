# Maple – Design

## 1. Architecture Overview

CLI → Orchestrator → Pluggable Components (BOM Parser, Repo Manager, Build Runner) → Reporter

```shell
+-------------------+
| CLI / Config      |
+---------+---------+
          v
+---------+---------+        +------------------+
  Orchestrator     | -----> | Reporter/Notifier|
---------+----+----+        +------------------+
          |    |
          |    +--> Build Runner
          +------> Repo Manager
                 -> BOM Parser & Graph
```

## 2. Modules

- **cli**: args, config, subcommands (`plan`, `run`, `resume`).
- **bom-parser**: `BOMParser` interface + `JsonBomParser` (MVP). Future: Gradle catalog, Maven, npm.
- **graph**: optional DAG builder for build order (stretch for v1.1).
- **repo-manager**: clone/fetch/checkout/branch/tag/push. JGit or git CLI wrapper.
- **build-runner**: run repo-defined build/test commands, capture logs/exit codes.
- **orchestrator**: schedules work, concurrency, retries, state persistence.
- **state-store**: JSON or SQLite for resume/audit.
- **reporter**: JSON summary + pretty console. Optional webhooks/Slack.

## 3. Data Model (Kotlin)

```kotlin
data class BomEntry(
    val name: String,
    val version: String,
    val firstParty: Boolean,
    val repoUrl: URI,
    val buildCmd: String? = null
     )

enum class StepStatus { PENDING, RUNNING, SUCCEEDED, FAILED, SKIPPED }

data class RepoActionStatus(
    val repo: String,
    val checkoutSha: String? = null,
    val branchCreated: Boolean = false,
    val tagCreated: Boolean = false,
    val buildStatus: StepStatus = StepStatus.PENDING,
    val logsPath: Path? = null,
    val error: String? = null
     )

data class RunSummary(
    val start: Instant,
    val end: Instant,
    val version: String,
    val entries: List<RepoActionStatus>
     )
 ```

## 4. Execution Flow

1. CLI parses flags & loads config/BOM.
2. Filter first-party modules → build worklist (or DAG).
3. For each repo (parallel within limit):
   - Clone/fetch → checkout.
   - Build/test.
   - On success: branch/tag/push.
   - Persist step status.
4. Generate final report & optionally notify.

## 5. Error & Retry

- Transient vs permanent errors.
- Exponential backoff for network/git ops.
- Flags: `--continue-on-error`, `--resume-from repo`.

## 6. Concurrency

- Kotlin coroutines or fixed thread pool.
- Separate semaphores for git ops vs builds if needed.

## 7. Config / Extensibility

- `.maple.yml` for defaults: repo map overrides, naming templates (`release/{version}`).
- SPI for BOM parsers & build runners.
- Hook points: pre-branch, post-tag scripts.

## 8. Observability

- JSON-structured logs.
- Debug mode increases verbosity.
- Optional OpenTelemetry spans wrapper.

## 9. Testing

- Unit: parsers, CLI, tag name generator.
- Integration: temp git repos w/ JGit harness.
- E2E: docker-compose + sample repos & gradle wrapper.
- Golden files for JSON reports.

## 10. Distribution

- Fat JAR (Shadow) or native image (GraalVM).
- Publish via GitHub Releases; Homebrew/SDKMAN optional.

## 11. Future

- Multi-BOM diff/merge.
- UI dashboard.
- Cross-VCS support.
- Automatic BOM generation helper.
