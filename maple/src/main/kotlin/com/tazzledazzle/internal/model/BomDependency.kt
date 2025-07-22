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
    var type: String = "",
    @SerializedName("bom-ref")
    var bomRef : String = "",
    var group: String = "",
    var name: String = "",
    var version: String = "",
    var purl: String = ""
) {
    fun convertToComponentDependency(): ComponentDependency {
        return ComponentDependency(
            component = this,
            dependencies = listOf()
        )
    }
}

data class BomDependency(
   var ref: String,
    var dependsOn: List<String>
)

data class ComponentDependency(
    var component: BomComponent = BomComponent(),
    var dependencies: List<ComponentDependency> = listOf()
) {
    override fun toString(): String {
        return "ComponentDependency(component=$component, dependencies=$dependencies)"
    }
}
