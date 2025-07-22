package com.tazzledazzle.maple.fixtures

interface FixtureDriver {
    fun create(spec: FixturesSpec): List<BomEntry>
    fun destroy(entries: List<BomEntry>, spec: FixturesSpec)
}