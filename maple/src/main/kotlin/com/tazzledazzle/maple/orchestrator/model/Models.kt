package com.tazzledazzle.maple.orchestrator.model

import java.time.Instant

data class RunSummary(
    val runId: String,
    val version: String,
    val start: Instant,
    val end: Instant?,
    val status: RunStatus,
    val repos: List<RepoActionStatus>,
    val metrics: RunMetrics
)

data class RepoActionStatus(
    val name: String,
    val repoUrl: String,
    val bomVersion: String,
    val checkoutSha: String? = null,
    val branchCreated: String? = null,
    val tagCreated: String? = null,
    val build: BuildStepStatus,
    val gitOps: GitOpsStatus,
    val retries: Int = 0,
    val durationMs: Long = 0,
    val logsPath: String? = null,
    val scanUrl: String? = null,
    val errorType: String? = null,
    val errorMessage: String? = null
)

data class BuildStepStatus(
    val status: StepStatus,
    val exitCode: Int? = null,
    val durationMs: Long = 0
)

data class GitOpsStatus(
    val checkout: StepStatus = StepStatus.PENDING,
    val branch: StepStatus = StepStatus.PENDING,
    val tag: StepStatus = StepStatus.PENDING,
    val push: StepStatus = StepStatus.PENDING
)

enum class StepStatus { PENDING, RUNNING, SUCCEEDED, FAILED, SKIPPED }
enum class RunStatus { SUCCEEDED, FAILED, PARTIAL }

data class RunMetrics(
    val totalRepos: Int,
    val succeeded: Int,
    val failed: Int,
    val skipped: Int,
    val totalDurationMs: Long
)
