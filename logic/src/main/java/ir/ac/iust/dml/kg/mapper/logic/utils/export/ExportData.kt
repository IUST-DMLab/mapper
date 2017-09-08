package ir.ac.iust.dml.kg.mapper.logic.utils.export

import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import javax.xml.bind.annotation.XmlRootElement

data class TemplateToOntologyMap(var language: String? = null,
                                 var infoboxType: String? = null,
                                 var ontologyClass: String? = null,
                                 var infoboxTemplateProperty: String? = null,
                                 var ontologyProperty: String? = null,
                                 var status: MappingStatus? = null)

@XmlRootElement
data class ExportData(var list: MutableList<TemplateToOntologyMap> = mutableListOf())