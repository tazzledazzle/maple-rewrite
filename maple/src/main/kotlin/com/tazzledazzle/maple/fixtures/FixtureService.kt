package com.tazzledazzle.maple.fixtures

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class FixturesService(
    private val local: LocalFixtureDriver = LocalFixtureDriver(),
    private val gh: GitHubFixtureDriver = GitHubFixtureDriver()
){
    private val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()

    fun init(spec: FixturesSpec) {
        val repos = when (spec.remote) {
            "local" -> local.create(spec)
            "github" -> {
                requireNotNull(spec.org) { "--org required for github mode" }
                gh.create(spec)
            }
            else -> error("Unknown remote ${spec.remote}")
        }
        writeBom(repos, spec.root)
        println("Created ${repos.size} repos. BOM at ${spec.root.resolve(BOM_NAME)}")
    }

    fun destroy(spec: FixturesSpec) {
        val bomFile = spec.root.resolve(BOM_NAME)
        if (!bomFile.exists()) {
            println("No BOM at $bomFile; skipping.")
            return
        }
        val entries: List<BomEntry> =
            mapper.readValue(
                bomFile.toFile(),
                mapper.typeFactory.constructCollectionType(
                    List::class.java,
                    BomEntry::class.java
                )
            )
        when (spec.remote) {
            "local" -> local.destroy(entries, spec)
            "github" -> gh.destroy(entries, spec)
        }
        Files.deleteIfExists(bomFile)
        println("Destroyed fixtures for ${entries.size} repos.")
    }

    fun list(spec: FixturesSpec): String {
        val bom = spec.root.resolve(BOM_NAME)
        return if (bom.exists()) bom.toAbsolutePath().toString() else "No BOM at $bom"
    }

    private fun writeBom(entries: List<BomEntry>, root: Path) {
        Files.createDirectories(root)
        mapper.writerWithDefaultPrettyPrinter()
            .writeValue(root.resolve(BOM_NAME).toFile(), entries)
    }

    companion object {
        const val BOM_NAME = "fixtures-bom.json"
    }
}

