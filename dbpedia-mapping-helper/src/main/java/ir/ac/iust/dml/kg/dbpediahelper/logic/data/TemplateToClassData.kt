package ir.ac.iust.dml.kg.dbpediahelper.logic.data

data class TemplateToClassData(
      var id: Long? = null,
      var templateName: String? = null,
      var className: String? = null,
      var language: String? = null,
      var approved: Boolean? = null)