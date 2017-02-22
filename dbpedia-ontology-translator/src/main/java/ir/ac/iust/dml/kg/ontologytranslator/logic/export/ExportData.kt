package ir.ac.iust.dml.kg.ontologytranslator.logic.export

import javax.xml.bind.annotation.XmlRootElement

data class OntologyClassTranslation(
      var ontologyClass: String? = null,
      var parentOntologyClass: String? = null,
      var enLabel: String? = null,
      var faLabel: String? = null,
      var faOtherLabels: String? = null,
      var note: String? = null)

@XmlRootElement
data class ExportData(var list: MutableList<OntologyClassTranslation> = mutableListOf())