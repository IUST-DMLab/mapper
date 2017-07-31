package ir.ac.iust.dml.kg.mapper.logic.data

import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus

data class FkgPropertyMappingData(
    var id: Long? = null,
    var templateName: String? = null,
    var ontologyClass: String? = null,
    var templateProperty: String? = null,
    var ontologyProperty: String? = null,
    var language: String? = null,
    var approved: Boolean? = null,
    var status: MappingStatus? = null)