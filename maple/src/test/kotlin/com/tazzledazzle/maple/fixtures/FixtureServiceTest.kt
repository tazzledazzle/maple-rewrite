package com.tazzledazzle.maple.fixtures

import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.test.assertTrue

class FixturesServiceTest {
    @Test
    fun `local fixtures lifecycle`() {
        val tmp = Files.createTempDirectory("maple-fixtures")
        val spec = FixturesSpec(
            "local",
            2,
            tmp,
            null,
            "fx-",
            "1.0.0",
            "./gradlew build"
        )
        val svc = FixturesService()
        svc.init(spec)
        val bomPath = tmp.resolve(FixturesService.BOM_NAME)
        assertTrue(Files.exists(bomPath))
        svc.destroy(spec)
        assertTrue(!Files.exists(bomPath))
    }
}