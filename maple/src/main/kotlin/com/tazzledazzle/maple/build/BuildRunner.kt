package com.tazzledazzle.maple.build

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

interface BuildRunner {
    fun run(spec: BuildSpec): BuildResult
}

data class BuildSpec(
    val repoRoot: Path,
    val command: List<String>,
    val env: Map<String, String> = emptyMap(),
    val timeout: Duration = Duration.ofMinutes(15),
    val logDir: Path
)

sealed class BuildResult {
    data class Success(val duration: Duration, val logFile: Path) : BuildResult()
    data class Failure(
        val exitCode: Int,
        val duration: Duration,
        val logFile: Path,
        val message: String? = null
    ) : BuildResult()
}

object BuildRunnerFactory {
    fun detect(repoRoot: Path, explicit: String?, logDir: Path): BuildSpec {
        if (explicit != null) {
            return BuildSpec(repoRoot, explicit.split(" "), logDir = logDir)
        }
        val gradlew = repoRoot.resolve("gradlew")
        val mvnw = repoRoot.resolve("mvnw")
        return when {
            Files.exists(gradlew) -> BuildSpec(repoRoot, listOf("./gradlew", "build"), logDir = logDir)
            Files.exists(mvnw) || Files.exists(repoRoot.resolve("pom.xml")) ->
                BuildSpec(repoRoot, listOf("./mvnw", "-B", "verify"), logDir = logDir)
            else -> throw IllegalArgumentException("No build command found for $repoRoot")
        }
    }
}

class ShellBuildRunner : BuildRunner {
    override fun run(spec: BuildSpec): BuildResult {
        Files.createDirectories(spec.logDir)
        val logFile = spec.logDir.resolve("build-${Instant.now().toEpochMilli()}.log")

        val start = Instant.now()
        val pb = ProcessBuilder(spec.command)
            .directory(spec.repoRoot.toFile())
            .redirectErrorStream(true)

        val env = pb.environment()
        spec.env.forEach { (k, v) -> env[k] = v }

        val proc = pb.start()

        // stream logs to file
        val out = Files.newBufferedWriter(logFile)
        val t = Thread {
            proc.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    out.write(redact(line))
                    out.newLine()
                }
            }
        }
        t.start()

        val finished = proc.waitFor(spec.timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
        if (!finished) {
            proc.destroyForcibly()
            t.join()
            out.close()
            return BuildResult.Failure(
                exitCode = -1,
                duration = Duration.between(start, Instant.now()),
                logFile = logFile,
                message = "Timeout after ${spec.timeout.toMinutes()}m"
            )
        }
        t.join()
        out.close()

        val duration = Duration.between(start, Instant.now())
        val code = proc.exitValue()
        return if (code == 0) {
            BuildResult.Success(duration, logFile)
        } else {
            BuildResult.Failure(code, duration, logFile, "Exited $code")
        }
    }

    private val redactKeys = listOf("TOKEN", "PASS", "SECRET", "KEY")
    private fun redact(line: String): String {
        // naive approach
        var out = line
        redactKeys.forEach { k ->
            // TOKEN=abc123 -> TOKEN=****, also ...token... patterns
            out = out.replace(Regex("(?i)$k=\\S+"), "$k=****")
        }
        return out
    }
}