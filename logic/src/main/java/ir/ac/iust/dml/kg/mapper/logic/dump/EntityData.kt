package ir.ac.iust.dml.kg.mapper.logic.dump

data class EntityData(
        var entityName: String? = null,
        var infoboxes: List<String> = mutableListOf()
)