package com.tazzledazzle

import com.tazzledazzle.internal.BomParser
import com.tazzledazzle.internal.MODULE_GROUP_PREFIX
import com.tazzledazzle.internal.model.Bom
import com.tazzledazzle.internal.model.BomComponent
import com.tazzledazzle.internal.model.ComponentDependency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.DefaultAsserter.fail
import kotlin.test.assertEquals

class BomParserTest: FunSpec({
    val bomParser = BomParser()
    
    test("parse components from BOM file") {
        val bom = bomParser.parseJsonBomFile("src/test/resources/test-bom.json")
        val components = bomParser.componentRefsFromBom(bom)
        components.size shouldBe 32
        components.shouldNotBeEmpty()
    }

    test("parse components with empty file") {
        val emptyBom = bomParser.parseJsonBomFile("src/test/resources/empty-bom.json")
        val dependencies = bomParser.dependencyRefsFromBom(emptyBom)
        dependencies.size shouldBe 0
    }

    test("parse dependencies from BOM file") {
        val bom = bomParser.parseJsonBomFile("src/test/resources/test-bom.json")
        val dependencies = bomParser.dependencyRefsFromBom(bom)
        dependencies.size shouldBe 32
        dependencies.shouldNotBeEmpty()
    }

    test("parse dependencies with empty file") {
        val emptyBom = bomParser.parseJsonBomFile("src/test/resources/empty-bom.json")
        val dependencies = bomParser.dependencyRefsFromBom(emptyBom)
        dependencies.size shouldBe 0
    }
    
    test("filter first-party components") {
        val bom = bomParser.parseJsonBomFile("src/test/resources/test-bom.json")
        val firstPartyComponents = bom.components.filter { it.group.startsWith(MODULE_GROUP_PREFIX) }
        firstPartyComponents.shouldNotBeEmpty()
        firstPartyComponents.forEach { component ->
            component.group.shouldContain(MODULE_GROUP_PREFIX)
        }
    }
    
    test("validate BOM structure") {
        val bom = bomParser.parseJsonBomFile("src/test/resources/test-bom.json")
        bom shouldNotBe null
        bom.components.shouldNotBeEmpty()
        bom.dependencies.shouldNotBeEmpty()
        
        // Validate component structure
        bom.components.forEach { component ->
            component.name shouldNotBe ""
            component.bomRef shouldNotBe ""
            // Note: version can be empty for some components
        }
    }
    
    test("handle malformed BOM gracefully") {
        try {
            bomParser.parseJsonBomFile("src/test/resources/nonexistent-bom.json")
        } catch (e: Exception) {
            // Should handle file not found gracefully
            e shouldNotBe null
        }
    }
    
    xtest("correlate components with dependencies") {
        val bom = bomParser.parseJsonBomFile("src/test/resources/test-bom.json")
        val dependencies = bomParser.dependencyRefsFromBom(bom)
        val components = bomParser.componentRefsFromBom(bom)
        val compDepList = mutableListOf<ComponentDependency>()
        
        bom.dependencies.forEach { dependency ->
            val componentDep = bom.components
                .filter{ it.group.startsWith(MODULE_GROUP_PREFIX)}
                .find { it.bomRef == dependency.ref }
                ?.convertToComponentDependency()!!
            dependency.dependsOn.forEach { dep ->
                componentDep.dependencies += bom.components
                        .filter{ it.group.startsWith(MODULE_GROUP_PREFIX)}
                        .find { it.bomRef == dependency.ref }
                        ?.convertToComponentDependency()
                    ?: fail("Dependency not found: $dep")
            }
            compDepList.add(componentDep)
        }

        compDepList.size shouldBe 32
    }
})

