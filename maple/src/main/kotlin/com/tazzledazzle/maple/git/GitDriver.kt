package com.tazzledazzle.maple.git

import java.nio.file.Path

interface GitDriver {
    fun cloneOrFetch(url: String, dir: Path)
    fun checkout(dir: Path, ref: String)
    fun createBranch(dir: Path, branch: String)
    fun createTag(dir: Path, tag: String)
    fun push(dir: Path, branch: String?, tag: String?)
}
