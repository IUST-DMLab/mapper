package ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities

data class PropertyMapping(
    var property: String? = null,
    var weight: Double? = null,
    var rules: MutableSet<MapRule> = mutableSetOf()
)