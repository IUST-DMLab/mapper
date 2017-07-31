package ir.ac.iust.dml.kg.mapper.logic.export

import ir.ac.iust.dml.kg.access.dao.FkgPropertyMappingDao
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class TemplateToOntologyExporter {

  @Autowired
  lateinit var dao: FkgPropertyMappingDao
  val logger = Logger.getLogger(this.javaClass)!!

  @Throws(Exception::class)
  fun export(filterLanguage: String?): ExportData {
    return data(filterLanguage)
  }

  @Throws(Exception::class)
  fun exportJson(filterLanguage: String?): ExportData {
    val toWrite = data(filterLanguage)
    DataExporter.export(DataExporter.ExportTypes.json,
        "dbpedia.mapping.helper.export.json",
        "~/.pkg/data/dbpedia_mappings.json",
        toWrite, ExportData::class.java)
    return toWrite
  }

  @Throws(Exception::class)
  fun exportXml(filterLanguage: String?) {
    val toWrite = data(filterLanguage)
    DataExporter.export(DataExporter.ExportTypes.xml,
        "dbpedia.mapping.helper.export.xml",
        "~/.pkg/data/dbpedia_mappings.xml",
        toWrite, ExportData::class.java)
  }

  fun data(filterLanguage: String?): ExportData {
    val toWrite = ExportData()
    var page = 0
    do {
      val list = dao.list(page = page++)
      logger.trace("I have read page " + page)
      for ((id, language, type, clazz, templateProperty, ontologyProperty, status) in list.data) {
        if (filterLanguage != null && language != filterLanguage) continue
        toWrite.list.add(TemplateToOntologyMap(language = language,
            infoboxType = type,
            ontologyClass = clazz,
            infoboxTemplateProperty = templateProperty,
            ontologyProperty = ontologyProperty,
            status = status))
      }
    } while (!list.data.isEmpty())
    return toWrite
  }
}