package ir.ac.iust.dml.kg.ontologytranslator.logic.export

import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
data class ExportData(var list: MutableList<OntologyClassTranslationData> = mutableListOf())