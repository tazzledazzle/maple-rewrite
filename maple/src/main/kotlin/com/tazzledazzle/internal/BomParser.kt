package com.tazzledazzle.internal

import com.google.gson.Gson
import com.tazzledazzle.internal.model.Bom
import java.io.File

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

    fun dependencyRefsFromBom(bom: Bom) : List<String> = bom.dependencies.map { it.ref }.toList()

    fun componentRefsFromBom(bom: Bom): List<String> = bom.components.map { it.bomRef }.toList()


}