package com.tazzledazzle.internal

import com.google.gson.Gson
import com.tazzledazzle.internal.model.Bom
import java.io.File
val MODULE_GROUP_PREFIX = "com.tableau"
class BomParser {

    // parse the BOM file and return a list of dependencies
    fun parseJsonBomFile(bomFilePath: String): Bom {
        // Placeholder for BOM parsing logic
        // In a real implementation, you would read the BOM file and extract dependencies
        val bom = Gson().fromJson(File(bomFilePath).readText(), Bom::class.java)
            ?: Bom()
        println(componentRefsFromBom(bom))
        println(dependencyRefsFromBom(bom))
        return bom
    }

    fun dependencyRefsFromBom(bom: Bom): List<String> = bom.dependencies.map { it.ref }.toList()

    fun componentRefsFromBom(bom: Bom): List<String> = bom.components.map { it.bomRef }.toList()

    companion object {
        // Placeholder for future static methods or constants
        // For example, you could add a method to validate BOM files or handle different formats
/*
*     {
      "type": "library",
      "bom-ref": "com.tableau.modules:admin-insights-service-protobuf:1.9.0",
      "group": "com.tableau.modules",
      "name": "admin-insights-service-protobuf",
      "version": "1.9.0",
      "purl": "pkg:maven/com.tableau.modules/admin-insights-service-protobuf@1.9.0"
    },
    *  {
      "ref": "com.tableau.modules:admin-insights-service-protobuf:1.9.0",
      "dependsOn": [
        "org.antlr:antlr4-runtime", -- don't care
        "org.aspectj:aspectjrt:1.9.5", -- don't care
        "org.awaitility:awaitility:4.0.3", -- don't care
        "com.tableau.modules:allegro-client:1.190.0" -- need to correlate with component
      ]
    },
*
* */

        fun correlateComponentsWithDependencies(
            components: List<String>,
            dependencies: List<String>
        ): Map<String, String> {


            // Placeholder for logic to correlate components with dependencies
            // This could involve matching component references with dependency references
            return mapOf<String, String>()
        }
    }
}