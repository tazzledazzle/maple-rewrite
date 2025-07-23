package com.tazzledazzle.maple.orchestrator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tazzledazzle.maple.orchestrator.model.RunSummary
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

class StateStore(private val root: Path = Path.of(".maple/state")) {
    private val mapper = jacksonObjectMapper()

    init { Files.createDirectories(root) }

    fun newRunId(): String = UUID.randomUUID().toString()

    fun save(summary: RunSummary) {
        val file = root.resolve("run-${summary.runId}.json")
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), summary)
    }

    fun load(runId: String): RunSummary? {
        val file = root.resolve("run-$runId.json")
        return if (Files.exists(file)) mapper.readValue(file.toFile(), RunSummary::class.java) else null
    }
}
