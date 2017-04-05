package ir.ac.iust.dml.kg.dbpediahelper.logic.dump

data class EntityData(
        var entityName: String? = null,
        var infoboxes: List<String> = mutableListOf()
)