# Orchestrator Schema (v2)

## Top-Level Objects

### `RunSummary`
```kotlin
data class RunSummary(
  val runId: String,
  val version: String,
  val start: Instant,
  val end: Instant?,
  val status: RunStatus,
  val repos: List<RepoActionStatus>,
  val metrics: RunMetrics
)
```

### `RepoActionStatus`
```kotlin
data class RepoActionStatus(
  val name: String,
  val repoUrl: String,
  val bomVersion: String,
  val checkoutSha: String?,
  val branchCreated: String?,     // branch name or null
  val tagCreated: String?,        // tag name or null
  val build: BuildStepStatus,
  val gitOps: GitOpsStatus,
  val retries: Int,
  val durationMs: Long,
  val logsPath: String?,
  val scanUrl: String?,
  val errorType: String?,         // e.g. "BUILD_TIMEOUT", "GIT_PUSH_FAILED"
  val errorMessage: String?
)
```

### `BuildStepStatus`
```kotlin
data class BuildStepStatus(
  val status: StepStatus,
  val exitCode: Int?,
  val durationMs: Long
)
```

### `GitOpsStatus`
```kotlin
data class GitOpsStatus(
  val checkout: StepStatus,
  val branch: StepStatus,
  val tag: StepStatus,
  val push: StepStatus
)
```

### Enums
```kotlin
enum class StepStatus { PENDING, RUNNING, SUCCEEDED, FAILED, SKIPPED }
enum class RunStatus { SUCCEEDED, FAILED, PARTIAL }
```

### `RunMetrics`
Rollups for Prometheus/Grafana and quick CLI summary.
```kotlin
data class RunMetrics(
  val totalRepos: Int,
  val succeeded: Int,
  val failed: Int,
  val skipped: Int,
  val totalDurationMs: Long
)
```

## Persistence
- **Default**: JSON file per run (`.maple/state/run-<runId>.json`).
- **Optional**: SQLite (`.maple/state/maple.db`) with tables:
    - `runs(run_id, version, start, end, status, metrics_json)`
    - `repo_status(run_id, name, json_blob)`

## JSON Schema
Stored at `src/main/resources/schema/run-summary.schema.json`
Validate in CI to ensure forward compatibility.

## Backwards Compatibility
Keep an adapter for old summaries; mark fields `@JsonIgnoreProperties(ignoreUnknown = true)`.
