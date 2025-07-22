# Build Runner v2

## Goals
1. Support Gradle Build Scans (auto `--scan`, parse scan URL).
2. Optional containerized execution using Docker (via `docker-java`).
3. Structured result (duration, log path, exit code, scanUrl, stdout tail).
4. Pluggable runners (Shell, Gradle/Maven detection, Dockerized wrapper).

## API
```kotlin
interface BuildRunner {
  fun run(spec: BuildSpec): BuildResult
}

data class BuildSpec(
  val repoRoot: Path,
  val command: List<String>,
  val env: Map<String, String> = emptyMap(),
  val timeout: Duration = Duration.ofMinutes(15),
  val logDir: Path,
  val enableGradleScan: Boolean = false,
  val runInDocker: Boolean = false,
  val dockerImage: String = "gradle:8.9-jdk21", // default, adjust as needed
  val mounts: List<DockerMount> = emptyList()
)

data class DockerMount(val host: Path, val container: Path, val readOnly: Boolean = false)

sealed class BuildResult {
  data class Success(
    val duration: Duration,
    val logFile: Path,
    val scanUrl: String? = null
  ) : BuildResult()
  data class Failure(
    val exitCode: Int,
    val duration: Duration,
    val logFile: Path,
    val message: String? = null,
    val scanUrl: String? = null
  ) : BuildResult()
}
```

## Gradle Scan Detection
- If `enableGradleScan`, append `--scan` to command when `gradlew` detected.
- Regex match on output:
  ```
  https?://.*gradle.com/s/[A-Za-z0-9]+
  ```
- Store first match as `scanUrl`.

## Docker Mode
- If `runInDocker`:
    - Use `docker-java` to:
        1. Pull (if missing) `spec.dockerImage`.
        2. Create container with bind mounts.
        3. Exec build command inside container.
    - Repo root mounted at `/workspace` (container working dir).

## NFR
- Timeouts enforced both host-side and (optionally) docker exec timeout.
- Logs streamed to file with redaction.
- Simple secret redaction (env keys containing TOKEN|PASS|SECRET|KEY).

## Tests
- Dummy script success/failure.
- Timeout test.
- Regex scan url test.
- (Optional) docker test guarded by env var (skip in CI if Docker not available).