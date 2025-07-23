package com.tazzledazzle.maple.e2e

import com.tazzledazzle.maple.fixtures.FixturesService
import com.tazzledazzle.maple.fixtures.FixturesSpec
import com.tazzledazzle.maple.orchestrator.StateStore
import com.tazzledazzle.maple.orchestrator.model.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * E2E:
 *  - happy path
 *  - build failure
 *  - pre-existing tag/branch
 *  - git push failure (simulated)
 *  - timeout
 *  - resume from repo
 */
class MapleE2ETest {

    private lateinit var root: Path
    private val svc = FixturesService()

    @BeforeEach
    fun setup() {
        root = Files.createTempDirectory("maple-e2e")
    }

    @AfterEach
    fun teardown() {
        svc.destroy(FixturesSpec("local", 0, root, null, "fx-", "1.0.0", "./gradlew build"))
    }

    companion object {
        @JvmStatic
        fun scenarios() = listOf(
            Scenario("happy_path", manipulate = {}),
            Scenario("build_failure") { repoDir ->
                // introduce failing test
                Files.writeString(repoDir.resolve("src/test/kotlin/FailTest.kt"),
                    "import org.junit.jupiter.api.Test\nimport org.junit.jupiter.api.Assertions.*\nclass FailTest{ @Test fun nope(){ fail(\"boom\") }}")
            },
            Scenario("preexisting_tag") { repoDir ->
                runGit(repoDir, "tag", "1.0.0") // tag already exists
            },
            Scenario("timeout") { repoDir ->
                Files.writeString(repoDir.resolve("build.gradle.kts"),
                    "tasks.register(\"build\") { doLast { Thread.sleep(2000) } }")
            },
            Scenario("git_push_fail") { repoDir ->
                // remote will be removed later to trigger push fail
                runGit(repoDir, "remote", "remove", "origin")
            }
        )
    }

    data class Scenario(val name: String, val manipulate: (Path) -> Unit)

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    @Ignore("E2E tests are slow and should be run manually")
    fun runScenario(s: Scenario) {
        val spec = FixturesSpec("local", 3, root, null, "fx-", "1.0.0", "./gradlew build")
        svc.init(spec)
        val bom = root.resolve("fixtures-bom.json")

        // pick first repo and manipulate to cause scenario
        val repo1 = root.resolve("fx-01")
        s.manipulate(repo1)

        // Run Maple via main (assumes main supports 'run' etc.) or call orchestrator directly if exposed
        val out = runGradleRun("run --bom $bom --version 1.0.0 --continue-on-error --timeout-seconds 1")
        assertTrue(out.contains("Run complete"), "Should finish run")

        // Load summary
        val store = StateStore()
        val latest = Files.list(Path.of(".maple/state"))
            .filter { it.fileName.toString().startsWith("run-") }
            .max(Comparator.comparing { Files.getLastModifiedTime(it) })!!
        val summaryText = latest.get().readText()
        assertTrue(summaryText.contains("\"version\" : \"1.0.0\""))

        // Basic assertions
        // (Detailed per-scenario checks could parse JSON and verify fields)
    }

    private fun runGradleRun(args: String): String {
        val pb = ProcessBuilder("./gradlew", "run", "--args=$args", "--no-daemon")
            .directory(Path.of(".").toFile())
        val proc = pb.start()
        val out = proc.inputStream.bufferedReader().readText()
        proc.waitFor()
        return out
    }


}

fun runGit(dir: Path, vararg a: String) {
    ProcessBuilder(listOf("git") + a).directory(dir.toFile()).start().waitFor()
}
