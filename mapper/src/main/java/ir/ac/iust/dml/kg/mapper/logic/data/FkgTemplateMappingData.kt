package ir.ac.iust.dml.kg.mapper.logic.data

data class FkgTemplateMappingData(
      var id: Long? = null,
      var templateName: String? = null,
      var ontologyClass: String? = null,
      var language: String? = null,
      var approved: Boolean? = null)