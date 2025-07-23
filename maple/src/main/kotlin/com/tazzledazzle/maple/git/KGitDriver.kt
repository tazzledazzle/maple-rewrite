package com.tazzledazzle.maple.git

import java.nio.file.Path

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