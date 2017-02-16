package ir.ac.iust.dml.kg.templateequalities.logic.export

import javax.xml.bind.annotation.XmlRootElement

data class PropertyMap(var fa: String? = null, var en: String? = null)

data class InfoboxMaps(var maps: MutableList<PropertyMap> = mutableListOf())

@XmlRootElement
data class ExportData(var infoxboxes: MutableMap<String, InfoboxMaps> = mutableMapOf())