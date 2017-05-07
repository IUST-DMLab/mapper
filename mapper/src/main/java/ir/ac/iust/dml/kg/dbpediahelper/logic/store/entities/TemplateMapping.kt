package ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities

data class TemplateMapping(
    var template: String? = null,
    var properties: MutableMap<String, PropertyMapping>? = mutableMapOf(),
    var rules: MutableSet<MapRule>? = mutableSetOf(),
    //TODO we haven't this in knowledge store
    var weight: Double? = null
)