package ir.ac.iust.dml.kg.templateequalities.logic.export

import ir.ac.iust.dml.kg.templateequalities.access.dao.TemplatePropertyMappingDao
import ir.ac.iust.dml.kg.utils.DataExporter
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletResponse


@Service
class Exporter {

    @Autowired
    lateinit var dao: TemplatePropertyMappingDao
    val logger = Logger.getLogger(this.javaClass)!!

    @Throws(Exception::class)
    fun exportJson(response: HttpServletResponse?): ExportData {
        val toWrite = data()
        if (response == null)
            DataExporter.export(DataExporter.ExportTypes.json,
                    "template.property.mapping.export.json",
                    "~/pkg/data/template_equalities.json",
                    toWrite, ExportData::class.java)
        return toWrite
    }

    @Throws(Exception::class)
    fun exportXml(response: HttpServletResponse?) {
        val toWrite = data()
        if (response == null)
            DataExporter.export(DataExporter.ExportTypes.xml,
                    "template.property.mapping.export.xml",
                    "~/pkg/data/template_equalities.xml",
                    toWrite, ExportData::class.java)
        else
            DataExporter.export(DataExporter.ExportTypes.xml, toWrite, response.outputStream,
                    ExportData::class.java)
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