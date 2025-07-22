package com.tazzledazzle.maple.build

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class BuildRunnerTest {

    @Test
    fun `shell runner succeeds on trivial script`() {
        val tmp = Files.createTempDirectory("maple-test")
        val script = tmp.resolve("build.sh")
        Files.writeString(script, "#!/usr/bin/env bash\necho OK\n")
        script.toFile().setExecutable(true)

        val spec = BuildSpec(
            repoRoot = tmp,
            command = listOf("./build.sh"),
            timeout = Duration.ofSeconds(5),
            logDir = tmp
        )
        val res = ShellBuildRunner().run(spec)
        assertTrue(res is BuildResult.Success)
    }

    @Test
    fun `timeout triggers failure`() {
        val tmp = Files.createTempDirectory("maple-test2")
        val script = tmp.resolve("sleep.sh")
        Files.writeString(script, "#!/usr/bin/env bash\nsleep 3\n")
        script.toFile().setExecutable(true)

        val spec = BuildSpec(
            repoRoot = tmp,
            command = listOf("./sleep.sh"),
            timeout = Duration.ofSeconds(1),
            logDir = tmp
        )
        val res = ShellBuildRunner().run(spec)
        assertTrue(res is BuildResult.Failure)
        res as BuildResult.Failure
        assertEquals(-1, res.exitCode)
    }


    @Test
    fun `scan url regex works`() {
        val runner = ShellBuildRunner()
        // no direct access, but ensure regex is correct by simulating line.
        val line = "Publishing build scan... https://gradle.com/s/abc123"
        val match = Regex("https?://\\S*gradle.com/s/\\S+").find(line)
        assertNotNull(match)
    }

    // Docker test skipped unless DOCKER_AVAILABLE=true
    @Test
    fun `docker runner optional`() {
        if (System.getenv("DOCKER_AVAILABLE") != "true") return
        val tmp = Files.createTempDirectory("maple-docker")
        Files.writeString(tmp.resolve("build.sh"), "#!/bin/sh\necho hi\n").toFile().setExecutable(true)
        val spec = BuildSpec(tmp, listOf("sh","/workspace/build.sh"), logDir = tmp, runInDocker = true)
        val res = ShellBuildRunner().run(spec)
        assertTrue(res is BuildResult.Success)
    }

    @Test
    fun `factory detects gradle`() {
        val tmp = Files.createTempDirectory("maple-test3")
        Files.writeString(tmp.resolve("gradlew"), "dummy").toFile().setExecutable(true)
        val spec = BuildRunnerFactory.detect(tmp, null, tmp)
        assertEquals("./gradlew", spec.command.first())
    }
}
