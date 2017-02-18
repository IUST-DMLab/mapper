package ir.ac.iust.dml.kg.dbpediahelper.logic.export

import javax.xml.bind.annotation.XmlRootElement

data class TemplateToOntologyMap(var language: String? = null,
                                 var infoboxType: String? = null,
                                 var ontologyClass: String? = null,
                                 var infoboxTemplateProperty: String? = null,
                                 var ontologyProperty: String? = null)

@XmlRootElement
data class ExportData(var list: MutableList<TemplateToOntologyMap> = mutableListOf())