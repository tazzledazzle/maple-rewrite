package com.tazzledazzle.maple.orchestrator

import com.tazzledazzle.maple.metrics.MapleMetrics
import com.tazzledazzle.maple.orchestrator.model.*
import java.time.Duration
import java.time.Instant

class Orchestrator(
    private val executor: RepoExecutor
) {
    fun run(version: String, repos: List<RepoSpec>): RunSummary {
        MapleMetrics.startIfEnabled()
        val runId = java.util.UUID.randomUUID().toString()
        val start = Instant.now()
        val results = mutableListOf<RepoActionStatus>()

        repos.parallelStream().forEach { spec ->
            val begin = Instant.now()
            val status = executor.execute(spec)
            val durationMs = Duration.between(begin, Instant.now()).toMillis()

            results += status.copy(durationMs = durationMs)
            MapleMetrics.incRepo(status.build.status.name, status.errorType)
        }

        val succeeded = results.count { it.build.status == StepStatus.SUCCEEDED && it.errorType == null }
        val failed = results.size - succeeded
        val runStatus = when {
            failed == 0 -> RunStatus.SUCCEEDED
            succeeded == 0 -> RunStatus.FAILED
            else -> RunStatus.PARTIAL
        }

        val end = Instant.now()
        val summary = RunSummary(
            runId, version, start, end, runStatus, results,
            RunMetrics(results.size, succeeded, failed, 0, Duration.between(start, end).toMillis())
        )

        MapleMetrics.incRun(runStatus.name)
        return summary
    }
}

data class RepoSpec(
    val name: String,
    val repoUrl: String,
    val bomVersion: String,
    val buildCmd: String?
)

interface RepoExecutor {
    fun execute(spec: RepoSpec): RepoActionStatus
}
