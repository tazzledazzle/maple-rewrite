package com.tazzledazzle.maple.fixtures

data class BomEntry(
    val name: String,
    val version: String,
    val repoUrl: String,
    val firstParty: Boolean = true,
    val buildCmd: String
)