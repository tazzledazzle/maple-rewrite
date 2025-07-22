package com.tazzledazzle.maple.fixtures

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class LocalFixtureDriver : FixtureDriver {
    override fun create(spec: FixturesSpec): List<BomEntry> {
        val list = mutableListOf<BomEntry>()
        Files.createDirectories(spec.root)

        for (i in 1..spec.count) {
            val name = "%s%02d".format(spec.repoPrefix, i)
            val dir = spec.root.resolve(name)
            scaffoldGradle(dir)
            gitInit(dir, spec.version)
            list += BomEntry(
                name, spec.version, "file://${dir.toAbsolutePath()}",
                buildCmd = spec.buildCmd
            )
        }
        return list
    }

    override fun destroy(entries: List<BomEntry>, spec: FixturesSpec) {
        if (spec.root.exists() && spec.root.isDirectory()) {
            Files.walk(spec.root)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
    }

    fun scaffoldGradle(dir: Path) {
        Files.createDirectories(dir.resolve("src/main/kotlin"))
        Files.createDirectories(dir.resolve("src/test/kotlin"))
        Files.writeString(dir.resolve("build.gradle.kts"), """
            plugins { kotlin("jvm") version "1.9.25" }
            repositories { mavenCentral() }
            dependencies { testImplementation(kotlin("test")) }
            tasks.test { useJUnitPlatform() }
        """.trimIndent())
        Files.writeString(dir.resolve("settings.gradle.kts"), "rootProject.name = \"${dir.fileName}\"")
        Files.writeString(dir.resolve("src/main/kotlin/Main.kt"), "fun main() = println(\"Hello ${dir.fileName}\")")
        Files.writeString(dir.resolve("src/test/kotlin/DummyTest.kt"), "class DummyTest { @org.junit.jupiter.api.Test fun ok() {} }")
    }

    private fun gitInit(dir: Path, version: String) {
        ProcessBuilder("git", "init").directory(dir.toFile()).start().waitFor()
        ProcessBuilder("git", "add", ".").directory(dir.toFile()).start().waitFor()
        ProcessBuilder("git", "commit", "-m", "init").directory(dir.toFile()).start().waitFor()
        ProcessBuilder("git", "tag", version).directory(dir.toFile()).start().waitFor()
    }
}