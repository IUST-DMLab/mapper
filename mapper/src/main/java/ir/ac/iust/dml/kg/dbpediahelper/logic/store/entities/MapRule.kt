package ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities

data class MapRule(
    var predicate: String? = null,
    var constant: String? = null,
    var type: ValueType? = null,
    var unit: String? = null,
    var transform: String? = null,
    //TODO: we haven't this in knowledge store
    var state: ApproveState? = null
)