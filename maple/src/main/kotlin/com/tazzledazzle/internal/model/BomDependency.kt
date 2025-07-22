package com.tazzledazzle.internal.model

import com.google.gson.annotations.SerializedName


data class Bom(
    var bomFormat: String = "Empty Bom",
    var specFormat: String = "Empty Spec",
    var version: Int = -1,
    var metadata: BomMetadata = BomMetadata(
        timestamp = "",
        tools = listOf()
    ),
    var components: List<BomComponent> = listOf(),
    var dependencies: List<BomDependency> = listOf()
)

data class BomMetadata (
    var timestamp: String,
    var tools: List<MetadataTool>,
)

data class MetadataTool (
    var vendor: String,
    var name: String,
    var version: String
)

data class BomComponent (
    var type: String,
    @SerializedName("bom-ref")
    var bomRef : String,
    var group: String,
    var name: String,
    var version: String,
    var purl: String
)

data class BomDependency(
   var ref: String,
    var dependsOn: List<String>
)
