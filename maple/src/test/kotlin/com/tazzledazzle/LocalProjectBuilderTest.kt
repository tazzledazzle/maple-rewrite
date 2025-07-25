package com.tazzledazzle

import com.tazzledazzle.maple.build.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.writeText

class LocalProjectBuilderTest: FunSpec({
    
    test("detect gradle project") {
        val tempDir = Files.createTempDirectory("maple-gradle-test")
        try {
            // Create gradlew file
            val gradlew = tempDir.resolve("gradlew")
            gradlew.writeText("#!/bin/bash\necho 'gradle wrapper'")
            gradlew.toFile().setExecutable(true)
            
            val logDir = Files.createTempDirectory("logs")
            val spec = BuildRunnerFactory.detect(tempDir, null, logDir, enableScan = false, docker = false)
            
            spec.command shouldBe listOf("./gradlew", "build")
            spec.repoRoot shouldBe tempDir
            spec.runInDocker shouldBe false
            
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
    
    test("detect maven project") {
        val tempDir = Files.createTempDirectory("maple-maven-test")
        try {
            // Create pom.xml
            tempDir.resolve("pom.xml").writeText("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test</artifactId>
                    <version>1.0.0</version>
                </project>
            """.trimIndent())
            
            val logDir = Files.createTempDirectory("logs")
            val spec = BuildRunnerFactory.detect(tempDir, null, logDir, enableScan = false, docker = false)
            
            spec.command shouldBe listOf("./mvnw", "-B", "verify")
            
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
    
    test("use explicit build command") {
        val tempDir = Files.createTempDirectory("maple-explicit-test")
        try {
            val logDir = Files.createTempDirectory("logs")
            val spec = BuildRunnerFactory.detect(tempDir, "make build", logDir, enableScan = false, docker = false)
            
            spec.command shouldBe listOf("make", "build")
            
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
    
    test("shell build runner success") {
        val tempDir = Files.createTempDirectory("maple-build-test")
        val logDir = Files.createTempDirectory("maple-logs")
        
        try {
            val spec = BuildSpec(
                repoRoot = tempDir,
                command = listOf("echo", "build successful"),
                timeout = Duration.ofMinutes(1),
                logDir = logDir
            )
            
            val runner = ShellBuildRunner()
            val result = runner.run(spec)
            
            result.shouldBeInstanceOf<BuildResult.Success>()
            result as BuildResult.Success
            result.logFile shouldNotBe null
            Files.exists(result.logFile) shouldBe true
            
        } finally {
            tempDir.toFile().deleteRecursively()
            logDir.toFile().deleteRecursively()
        }
    }
    
    test("shell build runner failure") {
        val tempDir = Files.createTempDirectory("maple-build-fail-test")
        val logDir = Files.createTempDirectory("maple-logs")
        
        try {
            val spec = BuildSpec(
                repoRoot = tempDir,
                command = listOf("false"), // Command that always fails
                timeout = Duration.ofMinutes(1),
                logDir = logDir
            )
            
            val runner = ShellBuildRunner()
            val result = runner.run(spec)
            
            result.shouldBeInstanceOf<BuildResult.Failure>()
            result as BuildResult.Failure
            result.exitCode shouldNotBe 0
            result.logFile shouldNotBe null
            Files.exists(result.logFile) shouldBe true
            
        } finally {
            tempDir.toFile().deleteRecursively()
            logDir.toFile().deleteRecursively()
        }
    }
    
    test("build spec with gradle scan enabled") {
        val tempDir = Files.createTempDirectory("maple-scan-test")
        try {
            val gradlew = tempDir.resolve("gradlew")
            gradlew.writeText("#!/bin/bash\necho 'gradle wrapper'")
            gradlew.toFile().setExecutable(true)
            
            val logDir = Files.createTempDirectory("logs")
            val spec = BuildRunnerFactory.detect(tempDir, null, logDir, enableScan = true, docker = false)
            
            spec.command shouldBe listOf("./gradlew", "build", "--scan")
            spec.enableGradleScan shouldBe true
            
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})