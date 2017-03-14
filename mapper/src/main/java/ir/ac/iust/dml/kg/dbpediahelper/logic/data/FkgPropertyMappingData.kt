package ir.ac.iust.dml.kg.dbpediahelper.logic.data

/**
 * Created by majid on 3/15/17.
 */
data class FkgPropertyMappingData(
      var id: Long? = null,
      var templateName: String? = null,
      var ontologyClass: String? = null,
      var templateProperty: String? = null,
      var ontologyProperty: String? = null,
      var language: String? = null,
      var approved: Boolean? = null)