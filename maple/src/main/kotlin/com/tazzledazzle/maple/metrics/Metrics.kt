package com.tazzledazzle.maple.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import io.prometheus.client.exporter.HTTPServer

object MapleMetrics {
    private val runs = Counter.build()
        .name("maple_runs_total").labelNames("status")
        .help("Total runs by status")
        .register()

    private val repos = Counter.build()
        .name("maple_repo_processed_total").labelNames("status","errorType")
        .help("Repos processed by status and error type")
        .register()

    val buildDuration = Histogram.build()
        .name("maple_build_duration_seconds")
        .help("Build duration distribution")
        .register()

    val gitDuration = Histogram.build()
        .name("maple_git_op_duration_seconds").labelNames("op")
        .help("Git op duration")
        .register()

    val retries = Counter.build()
        .name("maple_retries_total")
        .help("Total retries")
        .register()

    private var httpServer: HTTPServer? = null

    fun startIfEnabled() {
        if (System.getenv("MAPLE_METRICS_ENABLED") != "true") return
        if (httpServer != null) return
        val port = System.getenv("MAPLE_METRICS_PORT")?.toIntOrNull() ?: 9404
        httpServer = HTTPServer(port)
        // hotspot metrics
        io.prometheus.client.hotspot.DefaultExports.initialize()
    }

    fun incRun(status: String) = runs.labels(status).inc()

    fun incRepo(status: String, errorType: String?) =
        repos.labels(status, errorType ?: "none").inc()
}