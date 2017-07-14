package ir.ac.iust.dml.kg.mapper.logic.store.entities

data class TemplateMapping(
    var template: String? = null,
    var properties: MutableMap<String, PropertyMapping>? = mutableMapOf(),
    var rules: MutableSet<MapRule>? = mutableSetOf(),
    var ontologyClass: String = "Thing",
    var tree: List<String> = mutableListOf("Thing"),
    var weight: Double? = null
)