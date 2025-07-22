package com.tazzledazzle.maple.orchestrator

import com.tazzledazzle.maple.build.*
import com.tazzledazzle.maple.git.GitDriver
import com.tazzledazzle.maple.metrics.MapleMetrics
import com.tazzledazzle.maple.orchestrator.model.*
import io.prometheus.client.Histogram
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

class DefaultRepoExecutor(
    private val git: GitDriver,
    private val workRoot: Path,
    private val logRoot: Path
) : RepoExecutor {

    override fun execute(spec: RepoSpec): RepoActionStatus {
        var errorType: String? = null
        var errorMessage: String? = null

        val repoDir = workRoot.resolve(spec.name)
        val logsDir = logRoot.resolve(spec.name)
        Files.createDirectories(logsDir)

        val gitOps = GitOpsStatus()
        var checkoutSha: String? = null

        // Checkout
        val checkoutTimer = MapleMetrics.gitDuration.labels("checkout").startTimer()
        try {
            git.cloneOrFetch(spec.repoUrl, repoDir)
            git.checkout(repoDir, spec.bomVersion)
            checkoutSha = readHeadSha(repoDir)
        } catch (e: Exception) {
            checkoutTimer.observeDuration()
            errorType = "GIT_CHECKOUT_FAILED"
            errorMessage = e.message
            return baseFailure(
                spec,
                gitOps.copy(checkout = StepStatus.FAILED),
                checkoutSha,
                logsDir,
                errorType,
                errorMessage
            )
        }
        checkoutTimer.observeDuration()

        // Build
        val buildTimer = MapleMetrics.buildDuration.startTimer()
        val buildStatus = try {
            val specB = BuildRunnerFactory.detect(repoDir, spec.buildCmd, logsDir, enableScan = true, docker = false)
            val result = ShellBuildRunner().run(specB)
            when (result) {
                is BuildResult.Success -> BuildStepStatus(
                    StepStatus.SUCCEEDED,
                    0,
                    durationMs = durationFrom(start = buildTimer)
                )

                is BuildResult.Failure -> {
                    errorType = if (result.exitCode == -1) "BUILD_TIMEOUT" else "BUILD_FAILED"
                    errorMessage = result.message
                    BuildStepStatus(StepStatus.FAILED, result.exitCode, durationFrom(start = buildTimer))
                }
            }
        } finally {
            buildTimer.observeDuration()
        }

        if (buildStatus.status == StepStatus.FAILED) {
            return baseFailure(
                spec, gitOps.copy(checkout = StepStatus.SUCCEEDED), checkoutSha, logsDir, errorType, errorMessage,
                build = buildStatus
            )
        }

        // Branch / Tag / Push (simplified)
        val branchName = "release/${spec.bomVersion}"
        val tagName = spec.bomVersion
        try {
            val bTimer = MapleMetrics.gitDuration.labels("branch").startTimer()
            git.createBranch(repoDir, branchName)
            bTimer.observeDuration()

            val tTimer = MapleMetrics.gitDuration.labels("tag").startTimer()
            git.createTag(repoDir, tagName)
            tTimer.observeDuration()

            val pTimer = MapleMetrics.gitDuration.labels("push").startTimer()
            git.push(repoDir, branchName, tagName)
            pTimer.observeDuration()
        } catch (e: Exception) {
            errorType = "GIT_PUSH_FAILED"
            errorMessage = e.message
            return baseFailure(
                spec,
                gitOps.copy(
                    checkout = StepStatus.SUCCEEDED,
                    branch = StepStatus.SUCCEEDED,
                    tag = StepStatus.SUCCEEDED,
                    push = StepStatus.FAILED
                ),
                checkoutSha, logsDir, errorType, errorMessage, build = buildStatus, branch = branchName, tag = tagName
            )
        }

        // Success
        return RepoActionStatus(
            name = spec.name, repoUrl = spec.repoUrl, bomVersion = spec.bomVersion,
            checkoutSha = checkoutSha,
            branchCreated = branchName, tagCreated = tagName,
            build = buildStatus,
            gitOps = GitOpsStatus(
                StepStatus.SUCCEEDED,
                StepStatus.SUCCEEDED,
                StepStatus.SUCCEEDED,
                StepStatus.SUCCEEDED
            ),
            retries = 0, durationMs = 0,
            logsPath = logsDir.toString(),
            scanUrl = null,
            errorType = null, errorMessage = null
        )
    }

    private fun baseFailure(
        spec: RepoSpec,
        gitOps: GitOpsStatus,
        sha: String?,
        logsDir: Path,
        errorType: String?,
        errorMsg: String?,
        build: BuildStepStatus = BuildStepStatus(StepStatus.FAILED, null, 0),
        branch: String? = null,
        tag: String? = null
    ) = RepoActionStatus(
        name = spec.name, repoUrl = spec.repoUrl, bomVersion = spec.bomVersion,
        checkoutSha = sha, branchCreated = branch, tagCreated = tag,
        build = build, gitOps = gitOps, retries = 0, durationMs = 0,
        logsPath = logsDir.toString(), scanUrl = null,
        errorType = errorType, errorMessage = errorMsg
    )

    private fun readHeadSha(dir: Path): String =
        Files.readAllLines(dir.resolve(".git/HEAD")).firstOrNull().orEmpty()

    private fun durationFrom(start: Histogram.Timer): Long {
        // Timer already observes; approximate ms is fine
        return 0
    }
}