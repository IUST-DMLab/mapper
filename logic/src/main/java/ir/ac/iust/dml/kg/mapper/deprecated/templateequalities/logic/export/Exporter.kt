package ir.ac.iust.dml.kg.mapper.deprecated.templateequalities.logic.export

import ir.ac.iust.dml.kg.access.dao.WikipediaPropertyTranslationDao
import ir.ac.iust.dml.kg.mapper.logic.export.DataExporter
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class Exporter {

   @Autowired
   lateinit var dao: WikipediaPropertyTranslationDao
   val logger = Logger.getLogger(this.javaClass)!!

   @Throws(Exception::class)
   fun export(): ExportData {
      val toWrite = data()
      return toWrite
   }

   @Throws(Exception::class)
   fun exportJson(): ExportData {
      val toWrite = data()
      DataExporter.export(DataExporter.ExportTypes.json,
            "template.property.mapping.export.json",
            "~/.pkg/data/template_equalities.json",
            toWrite, ExportData::class.java)
      return toWrite
   }

   @Throws(Exception::class)
   fun exportXml() {
      val toWrite = data()
      DataExporter.export(DataExporter.ExportTypes.xml,
            "template.property.mapping.export.xml",
            "~/.pkg/data/template_equalities.xml",
            toWrite, ExportData::class.java)
   }

   fun data(): ExportData {
      val toWrite = ExportData()
      var page = 0
      do {
         val list = dao.list(page = page++)
         logger.trace("I have read page " + page)
         for ((id, type, faProperty, enProperty, notTranslated) in list.data) {
            val infoboxMap = toWrite.infoxboxes.getOrPut(type!!, { InfoboxMaps() })
            infoboxMap.maps.add(PropertyMap(fa = faProperty, en = enProperty, translated = !notTranslated!!))
         }
      } while (!list.data.isEmpty())
      return toWrite
   }
}