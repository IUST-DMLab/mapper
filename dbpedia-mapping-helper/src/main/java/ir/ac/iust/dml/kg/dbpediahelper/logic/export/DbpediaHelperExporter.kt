package ir.ac.iust.dml.kg.dbpediahelper.logic.export

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.DBpediaPropertyMappingDao
import ir.ac.iust.dml.kg.utils.DataExporter
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletResponse


@Service
class DbpediaHelperExporter {

    @Autowired
    lateinit var dao: DBpediaPropertyMappingDao
    val logger = Logger.getLogger(this.javaClass)!!

    @Throws(Exception::class)
    fun exportJson(filterLanguage: String?, response: HttpServletResponse?): ExportData {
        val toWrite = data(filterLanguage)
        if (response == null)
            DataExporter.export(DataExporter.ExportTypes.json,
                    "dbpedia.mapping.helper.export.json",
                    "~/pkg/data/dbpedia_mappings.json",
                    toWrite, ExportData::class.java)
        return toWrite
    }

    @Throws(Exception::class)
    fun exportXml(filterLanguage: String?, response: HttpServletResponse?) {
        val toWrite = data(filterLanguage)
        if (response == null)
            DataExporter.export(DataExporter.ExportTypes.xml,
                    "dbpedia.mapping.helper.export.xml",
                    "~/pkg/data/dbpedia_mappings.xml",
                    toWrite, ExportData::class.java)
        else
            DataExporter.export(DataExporter.ExportTypes.xml, toWrite, response.outputStream,
                    ExportData::class.java)
    }

    fun data(filterLanguage: String?): ExportData {
        val toWrite = ExportData()
        var page = 0
        do {
            val list = dao.list(page = page++)
            logger.trace("I have read page " + page)
            for ((id, language, type, clazz, templateProperty, ontologyProperty) in list.data) {
                if (filterLanguage != null && language != filterLanguage) continue
                toWrite.list.add(TemplateToOntologyMap(language = language,
                        infoboxType = type,
                        ontologyClass = clazz,
                        infoboxTemplateProperty = templateProperty,
                        ontologyProperty = ontologyProperty))
            }
        } while (!list.data.isEmpty())
        return toWrite
    }
}