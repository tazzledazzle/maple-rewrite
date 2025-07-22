package com.tazzledazzle.maple.fixtures

import java.nio.file.Files
import java.nio.file.Path

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