package ir.ac.iust.dml.kg.mapper.deprecated.ontologytranslator.logic.export

import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.mapper.deprecated.ontologytranslator.logic.Translator
import ir.ac.iust.dml.kg.mapper.logic.export.DataExporter
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class FkgClassExporter {

  @Autowired
  lateinit var dao: FkgClassDao
  @Autowired
  lateinit var translator: Translator
  val logger = Logger.getLogger(this.javaClass)!!

  @Throws(Exception::class)
  fun export(): ExportData {
    return data()
  }

  @Throws(Exception::class)
  fun exportJson(): ExportData {
    val toWrite = data()
    DataExporter.export(DataExporter.ExportTypes.json,
        "dbpedia.ontology.translator.export.json",
        "~/.pkg/data/dbpedia_ontology_translator.json",
        toWrite, ExportData::class.java)
    return toWrite
  }

  @Throws(Exception::class)
  fun exportXml() {
    val toWrite = data()
    DataExporter.export(DataExporter.ExportTypes.xml,
        "dbpedia.ontology.translator.export.xml",
        "~/.pkg/data/dbpedia_ontology_translator.xml",
        toWrite, ExportData::class.java)
  }

  fun data(): ExportData {
    val toWrite = ExportData()
    var page = 0
    do {
      val list = dao.search(page = page++, pageSize = 20)
      logger.trace("I have read page " + page)
      for (translation in list.data)
        toWrite.list.add(translator.sync(translation)!!)
    } while (!list.data.isEmpty())
    return toWrite
  }
}