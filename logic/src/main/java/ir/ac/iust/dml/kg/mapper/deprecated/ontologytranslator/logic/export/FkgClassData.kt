package ir.ac.iust.dml.kg.mapper.deprecated.ontologytranslator.logic.export

data class FkgClassData(
      var ontologyClass: String? = null,
      var parentOntologyClass: String? = null,
      var enLabel: String? = null,
      var comment: String? = null,
      var faLabel: String? = null,
      var faOtherLabels: String? = null,
      var note: String? = null,
      var approved: Boolean? = null)
