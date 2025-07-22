package com.tazzledazzle.maple.build

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists

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
    val dockerImage: String = "gradle:8.9-jdk21",
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

object BuildRunnerFactory {
    fun detect(repoRoot: Path, explicit: String?, logDir: Path, enableScan: Boolean, docker: Boolean): BuildSpec {
        if (explicit != null) {
            return BuildSpec(repoRoot, explicit.split(" "), logDir = logDir, enableGradleScan = enableScan, runInDocker = docker)
        }
        val gradlew = repoRoot.resolve("gradlew")
        val mvnw = repoRoot.resolve("mvnw")
        return when {
            gradlew.exists() -> {
                val cmd = mutableListOf("./gradlew", "build")
                if (enableScan) cmd += "--scan"
                BuildSpec(repoRoot, cmd, logDir = logDir, enableGradleScan = enableScan, runInDocker = docker)
            }
            mvnw.exists() || repoRoot.resolve("pom.xml").exists() -> BuildSpec(
                repoRoot,
                listOf("./mvnw", "-B", "verify"),
                logDir = logDir,
                runInDocker = docker
            )
            else -> throw IllegalArgumentException("No build command found for $repoRoot")
        }
    }
}

class ShellBuildRunner : BuildRunner {
    private val scanRegex = Regex("https?://\\S*gradle.com/s/\\S+")
    private val redactKeys = listOf("TOKEN", "PASS", "SECRET", "KEY")

    override fun run(spec: BuildSpec): BuildResult {
        return if (spec.runInDocker) DockerizedRunner(this).run(spec) else runLocal(spec)
    }

    private fun runLocal(spec: BuildSpec): BuildResult {
        Files.createDirectories(spec.logDir)
        val logFile = spec.logDir.resolve("build-${Instant.now().toEpochMilli()}.log")

        val start = Instant.now()
        val pb = ProcessBuilder(spec.command)
            .directory(spec.repoRoot.toFile())
            .redirectErrorStream(true)

        val env = pb.environment()
        spec.env.forEach { (k, v) -> env[k] = v }

        val proc = pb.start()

        var scanUrl: String? = null
        Files.newBufferedWriter(logFile).use { out ->
            proc.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val redacted = redact(line)
                    out.write(redacted)
                    out.newLine()
                    if (scanUrl == null) {
                        val match = scanRegex.find(redacted)
                        if (match != null) scanUrl = match.value
                    }
                }
            }
        }

        val finished = proc.waitFor(spec.timeout.toMillis(), TimeUnit.MILLISECONDS)
        if (!finished) {
            proc.destroyForcibly()
            return BuildResult.Failure(
                exitCode = -1,
                duration = Duration.between(start, Instant.now()),
                logFile = logFile,
                message = "Timeout after ${spec.timeout.toMinutes()}m",
                scanUrl = scanUrl
            )
        }

        val duration = Duration.between(start, Instant.now())
        val code = proc.exitValue()
        return if (code == 0) {
            BuildResult.Success(duration, logFile, scanUrl)
        } else {
            BuildResult.Failure(code, duration, logFile, "Exited $code", scanUrl)
        }
    }

    private fun redact(line: String): String {
        var out = line
        redactKeys.forEach { k ->
            out = out.replace(Regex("(?i)$k=\\S+"), "$k=****")
        }
        return out
    }
}

/**
 * Wrap ShellBuildRunner, but execute inside Docker.
 */
class DockerizedRunner(private val delegate: ShellBuildRunner) : BuildRunner {
    override fun run(spec: BuildSpec): BuildResult {
        val client = Docker.client()
        val image = spec.dockerImage
        Docker.ensureImage(client, image)

        val repoMount = DockerMount(spec.repoRoot, Path.of("/workspace"), readOnly = false)
        val allMounts = listOf(repoMount) + spec.mounts

        val containerId = Docker.createContainer(client, image, "/bin/sh", listOf("-c", spec.command.joinToString(" ")), allMounts)
        try {
            val startedAt = Instant.now()
            Docker.start(client, containerId)
            val logsPath = spec.logDir.resolve("build-${Instant.now().toEpochMilli()}.log")
            val scanRegex = Regex("https?://\\S*gradle.com/s/\\S+")
            var scanUrl: String? = null

            Files.createDirectories(spec.logDir)
            Files.newBufferedWriter(logsPath).use { out ->
                Docker.streamLogs(client, containerId, spec.timeout) { line ->
                    out.write(line)
                    out.newLine()
                    if (scanUrl == null) {
                        val m = scanRegex.find(line)
                        if (m != null) scanUrl = m.value
                    }
                }
            }

            val exit = Docker.wait(client, containerId, spec.timeout)
            val dur = Duration.between(startedAt, Instant.now())
            return if (exit == 0L) {
                BuildResult.Success(dur, logsPath, scanUrl)
            } else {
                BuildResult.Failure(exit.toInt(), dur, logsPath, "Exited $exit", scanUrl)
            }
        } finally {
            Docker.cleanup(client, containerId)
            client.close()
        }
    }
}
