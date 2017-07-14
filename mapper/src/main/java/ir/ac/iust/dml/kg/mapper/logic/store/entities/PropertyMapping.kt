package ir.ac.iust.dml.kg.mapper.logic.store.entities

data class PropertyMapping(
    var property: String? = null,
    var weight: Double? = null,
    var rules: MutableSet<MapRule> = mutableSetOf(),
    var recommendations: MutableSet<MapRule> = mutableSetOf()
)