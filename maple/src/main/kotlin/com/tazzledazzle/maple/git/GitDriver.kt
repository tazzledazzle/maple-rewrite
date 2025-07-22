package com.tazzledazzle.maple.git

import java.nio.file.Path

interface GitDriver {
    fun cloneOrFetch(url: String, dir: Path)
    fun checkout(dir: Path, ref: String)
    fun createBranch(dir: Path, branch: String)
    fun createTag(dir: Path, tag: String)
    fun push(dir: Path, branch: String?, tag: String?)
}

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

/**
 * KGit-backed driver. Replace import path / calls with your chosen kgit lib.
 * Adjust groupId/version in build.gradle.kts.
 */
class KGitDriver(/* inject kgit client */) : GitDriver {
    override fun cloneOrFetch(url: String, dir: Path) {
        // TODO: implement using kgit API
        // placeholder: call shell
        ShellGitDriver().cloneOrFetch(url, dir)
    }
    override fun checkout(dir: Path, ref: String) = ShellGitDriver().checkout(dir, ref)
    override fun createBranch(dir: Path, branch: String) = ShellGitDriver().createBranch(dir, branch)
    override fun createTag(dir: Path, tag: String) = ShellGitDriver().createTag(dir, tag)
    override fun push(dir: Path, branch: String?, tag: String?) = ShellGitDriver().push(dir, branch, tag)
}
