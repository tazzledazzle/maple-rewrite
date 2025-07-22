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
        val entries: List<BomEntry> = mapper.readValue(bomFile.toFile())
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

data class BomEntry(
    val name: String,
    val version: String,
    val repoUrl: String,
    val firstParty: Boolean = true,
    val buildCmd: String
)

interface FixtureDriver {
    fun create(spec: FixturesSpec): List<BomEntry>
    fun destroy(entries: List<BomEntry>, spec: FixturesSpec)
}

class GitHubFixtureDriver : FixtureDriver {
    override fun create(spec: FixturesSpec): List<BomEntry> {
        require(spec.org != null)
        val tmpRoot = spec.root.resolve(".tmp-gh")
        Files.createDirectories(tmpRoot)
        val list = mutableListOf<BomEntry>()
        for (i in 1..spec.count) {
            val name = "%s%02d".format(spec.repoPrefix, i)
            val full = "${spec.org}/$name"
            runGh("repo", "create", full, "--private", "--disable-issues", "--disable-wiki", "-y")

            val dir = tmpRoot.resolve(name)
            Files.createDirectories(dir)
            LocalFixtureDriver().scaffoldGradle(dir)
            run("git", "init", dir)
            run("git", "remote", "add", "origin", "git@github.com:$full.git", dir)
            run("git", "add", ".", dir)
            run("git", "commit", "-m", "init $name", dir)
            run("git", "tag", spec.version, dir)
            run("git", "push", "origin", "HEAD:main", "--tags", dir)

            list += BomEntry(
                name, spec.version, "git@github.com:$full.git",
                buildCmd = spec.buildCmd
            )
        }
        return list
    }

    override fun destroy(entries: List<BomEntry>, spec: FixturesSpec) {
        require(spec.org != null)
        val prefix = spec.repoPrefix
        entries.filter { it.name.startsWith(prefix) }.forEach {
            runGh("repo", "delete", "${spec.org}/${it.name}", "-y")
        }
    }

    private fun runGh(vararg args: String) {
        val p = ProcessBuilder(listOf("gh") + args).inheritIO().start()
        val code = p.waitFor()
        if (code != 0) error("gh ${args.joinToString(" ")} failed with $code")
    }

    private fun run(vararg a: String, dir: Path) {
        val p = ProcessBuilder(a.toList()).directory(dir.toFile()).inheritIO().start()
        if (p.waitFor() != 0) error("Command ${a.joinToString(" ")} failed")
    }
}
