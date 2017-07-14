package ir.ac.iust.dml.kg.mapper.deprecated.ontologytranslator.logic.export

import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
data class ExportData(var list: MutableList<FkgClassData> = mutableListOf())