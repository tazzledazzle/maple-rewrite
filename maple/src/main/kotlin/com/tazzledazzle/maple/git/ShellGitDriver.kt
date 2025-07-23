package com.tazzledazzle.maple.git

import java.nio.file.Path

/**
 * Default shell-based driver (fallback).
 */
class ShellGitDriver : GitDriver {
    override fun cloneOrFetch(url: String, dir: Path) {
        if (dir.toFile().exists()) {
            run("git", "fetch", "--all", "--tags", dir = dir)
        } else {
            run("git", "clone", url, dir.toString(), dir = dir.parent)
        }
    }

    override fun checkout(dir: Path, ref: String) = run("git", "checkout", ref, dir = dir)
    override fun createBranch(dir: Path, branch: String) = run("git", "switch", "-c", branch, dir = dir)
    override fun createTag(dir: Path, tag: String) = run("git", "tag", tag, dir = dir)
    override fun push(dir: Path, branch: String?, tag: String?) {
        if (branch != null) run("git", "push", "origin", branch, dir = dir)
        if (tag != null) run("git", "push", "origin", tag, dir = dir)
    }

    private fun run(vararg a: String, dir: Path) {
        val p = ProcessBuilder(a.toList()).directory(dir.toFile()).inheritIO().start()
        if (p.waitFor() != 0) error("git cmd failed: ${a.joinToString(" ")}")
    }
}