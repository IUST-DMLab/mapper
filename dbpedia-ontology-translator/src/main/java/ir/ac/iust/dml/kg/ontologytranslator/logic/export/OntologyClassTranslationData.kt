package ir.ac.iust.dml.kg.ontologytranslator.logic.export

data class OntologyClassTranslationData(
      var ontologyClass: String? = null,
      var parentOntologyClass: String? = null,
      var enLabel: String? = null,
      var faLabel: String? = null,
      var faOtherLabels: String? = null,
      var note: String? = null)
