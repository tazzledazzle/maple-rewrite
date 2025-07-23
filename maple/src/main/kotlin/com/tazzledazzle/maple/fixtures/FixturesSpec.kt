package com.tazzledazzle.maple.fixtures

import java.nio.file.Path

data class FixturesSpec(
    val remote: String,              // "local" | "github"
    val count: Int,
    val root: Path,                  // local root
    val org: String?,                // github org/user
    val repoPrefix: String,
    val version: String,
    val buildCmd: String
)