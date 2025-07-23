package com.tazzledazzle.maple.cli

import com.tazzledazzle.maple.orchestrator.SqliteStateStore
import java.nio.file.Path
import kotlin.system.exitProcess

object QueryCommand {
    fun run(args: List<String>) {
        if (args.isEmpty()) usage()
        val db = Path.of(".maple/state/maple.db")
        val store = SqliteStateStore(db)
        when (args[0]) {
            "last-run" -> {
                // naive: list files and pick last
                val runId = listRunIds(db).maxOrNull() ?: run {
                    println("No runs"); exitProcess(0)
                }
                println(store.loadRun(runId))
            }
            else -> usage()
        }
    }

    private fun listRunIds(db: Path): List<String> {
        val conn = java.sql.DriverManager.getConnection("jdbc:sqlite:${db.toAbsolutePath()}")
        conn.use {
            val rs = it.createStatement().executeQuery("SELECT run_id FROM runs")
            val out = mutableListOf<String>()
            while (rs.next()) out += rs.getString(1)
            return out
        }
    }

    private fun usage(): Nothing {
        println("Usage: maple query <last-run>")
        exitProcess(1)
    }
}
