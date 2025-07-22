package com.tazzledazzle

import com.tazzledazzle.internal.BomParser
import io.kotest.core.spec.style.FunSpec
import kotlin.test.DefaultAsserter.fail
import kotlin.test.assertEquals

class BomParserTest: FunSpec({
    // This class is currently empty, but can be used for testing BOM parsing functionality in the future.
    // For example, you could add methods to test parsing of different BOM formats, validate dependencies, etc.
    // Placeholder for future BOM parsing code.

    // need to parse a BOM file
    // validate dependencies
    // check for missing dependencies
    // handle different BOM formats
    test("parseJsonBomFile") {
        // Example test case for parsing a BOM file
         val bomParser = BomParser()
         val dependencies = bomParser.parseJsonBomFile("path/to/bom.json")
         assertEquals(dependencies.size,0)

    }
})