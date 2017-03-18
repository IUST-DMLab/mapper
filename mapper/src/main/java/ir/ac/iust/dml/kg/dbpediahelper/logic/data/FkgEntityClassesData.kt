package ir.ac.iust.dml.kg.dbpediahelper.logic.data

import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus

data class FkgEntityClassesData(
        var id: Long? = null,
        var entity: String? = null,
        var className: String? = null,
        var approved: Boolean? = null,
        var status: MappingStatus? = null)