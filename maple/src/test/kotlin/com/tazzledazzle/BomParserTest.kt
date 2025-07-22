package com.tazzledazzle

import com.tazzledazzle.internal.BomParser
import io.kotest.core.spec.style.FunSpec
import kotlin.test.DefaultAsserter.fail
import kotlin.test.assertEquals

class BomParserTest: FunSpec({
    // This class is currently empty, but can be used for testing BOM parsing functionality in the future.
    // For example, you could add methods to test parsing of different BOM formats, validate dependencies, etc.
    // Placeholder for future BOM parsing code.
    val bomParser = BomParser()
    val bom = bomParser.parseJsonBomFile("src/test/resources/test-bom.json")

    // need to parse a BOM file
    // validate dependencies
    // check for missing dependencies
    // handle different BOM formats
    test("parse components from BOM file") {
        // Example test case for parsing a BOM file
        val dependencies = bomParser.componentRefsFromBom(bom)
        assertEquals(dependencies.size,32)
    }

    test("parse components with empty file") {
        // Example test case for parsing an empty BOM file
        val bomParser = BomParser()
        val emptyBom = bomParser.parseJsonBomFile("src/test/resources/empty-bom.json")
        val dependencies = bomParser.dependencyRefsFromBom(emptyBom)
        assertEquals(dependencies.size, 0)
    }

    test("parse dependencies from BOM file") {
        // Example test case for parsing a BOM file
        val dependencies = bomParser.componentRefsFromBom(bom)
        assertEquals(dependencies.size,32)
    }

    test("parse dependencies with empty file") {
        // Example test case for parsing an empty BOM file
        val bomParser = BomParser()
        val emptyBom = bomParser.parseJsonBomFile("src/test/resources/empty-bom.json")
        val dependencies = bomParser.dependencyRefsFromBom(emptyBom)
        assertEquals(dependencies.size, 0)
    }
})