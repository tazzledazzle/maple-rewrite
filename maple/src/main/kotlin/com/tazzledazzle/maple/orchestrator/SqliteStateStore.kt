package com.tazzledazzle.maple.orchestrator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tazzledazzle.maple.orchestrator.model.RunSummary
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class SqliteStateStore(private val dbPath: Path = Path.of(".maple/state/maple.db")) {
    private val mapper = jacksonObjectMapper()

    init {
        Files.createDirectories(dbPath.parent)
        connection().use { conn ->
            conn.createStatement().use { st ->
                st.execute("""CREATE TABLE IF NOT EXISTS runs(
                    run_id TEXT PRIMARY KEY,
                    version TEXT,
                    start TEXT,
                    end TEXT,
                    status TEXT,
                    metrics_json TEXT
                )""".trimIndent())

                st.execute("""CREATE TABLE IF NOT EXISTS repo_status(
                    run_id TEXT,
                    name TEXT,
                    json_blob TEXT,
                    PRIMARY KEY (run_id, name),
                    FOREIGN KEY(run_id) REFERENCES runs(run_id)
                )""".trimIndent())
            }
        }
    }

    fun saveRun(summary: RunSummary) = save(summary)
    
    fun save(summary: RunSummary) {
        connection().use { conn ->
            conn.autoCommit = false
            conn.prepareStatement(
                "INSERT OR REPLACE INTO runs(run_id,version,start,end,status,metrics_json) VALUES(?,?,?,?,?,?)"
            ).use { ps ->
                ps.setString(1, summary.runId)
                ps.setString(2, summary.version)
                ps.setString(3, summary.start.toString())
                ps.setString(4, summary.end?.toString())
                ps.setString(5, summary.status.name)
                ps.setString(6, mapper.writeValueAsString(summary.metrics))
                ps.executeUpdate()
            }
            conn.prepareStatement(
                "INSERT OR REPLACE INTO repo_status(run_id,name,json_blob) VALUES(?,?,?)"
            ).use { ps ->
                summary.repos.forEach {
                    ps.setString(1, summary.runId)
                    ps.setString(2, it.name)
                    ps.setString(3, mapper.writeValueAsString(it))
                    ps.addBatch()
                }
                ps.executeBatch()
            }
            conn.commit()
        }
    }

    fun loadRun(runId: String): RunSummary? {
        connection().use { conn ->
            val runRow = conn.prepareStatement("SELECT version,start,end,status,metrics_json FROM runs WHERE run_id=?")
                .apply { setString(1, runId) }
                .executeQuery()
            if (!runRow.next()) return null
            val version = runRow.getString(1)
            val start = java.time.Instant.parse(runRow.getString(2))
            val endStr = runRow.getString(3)
            val end = endStr?.let(java.time.Instant::parse)
            val status = com.tazzledazzle.maple.orchestrator.model.RunStatus.valueOf(runRow.getString(4))
            val metrics = mapper.readValue(runRow.getString(5), com.tazzledazzle.maple.orchestrator.model.RunMetrics::class.java)

            val repos = mutableListOf<com.tazzledazzle.maple.orchestrator.model.RepoActionStatus>()
            conn.prepareStatement("SELECT json_blob FROM repo_status WHERE run_id=?").use { ps ->
                ps.setString(1, runId)
                val rs = ps.executeQuery()
                while (rs.next()) {
                    repos += mapper.readValue(
                        rs.getString(1),
                        com.tazzledazzle.maple.orchestrator.model.RepoActionStatus::class.java
                    )
                }
            }
            return com.tazzledazzle.maple.orchestrator.model.RunSummary(runId, version, start, end, status, repos, metrics)
        }
    }

    private fun connection(): Connection =
        DriverManager.getConnection("jdbc:sqlite:${dbPath.toAbsolutePath()}")
}
