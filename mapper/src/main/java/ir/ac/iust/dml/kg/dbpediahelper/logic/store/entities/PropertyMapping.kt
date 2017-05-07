package ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities

data class PropertyMapping(
    //TODO why we have template in knowledge store?
    // val template: String? = null,
    var property: String? = null,
    var weight: Double? = null,
    var rules: MutableSet<MapRule> = mutableSetOf()
)