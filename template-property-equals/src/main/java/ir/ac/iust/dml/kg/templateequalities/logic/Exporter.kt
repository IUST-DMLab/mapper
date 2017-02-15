package ir.ac.iust.dml.kg.templateequalities.logic

import ir.ac.iust.dml.kg.templateequalities.access.dao.TemplatePropertyMappingDao
import ir.ac.iust.dml.kg.templateequalities.access.entities.TemplatePropertyMapping
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
    fun exportJson(response: HttpServletResponse?) {
        val toWrite = data()
        if (response == null)
            DataExporter.export(DataExporter.ExportTypes.json,
                    "template.property.mapping.export.json",
                    "~/pkg/data/template_equalities.json",
                    toWrite, TemplatePropertyMapping::class.java)
        else
            DataExporter.export(DataExporter.ExportTypes.json, toWrite, response.outputStream,
                    TemplatePropertyMapping::class.java)
    }

    @Throws(Exception::class)
    fun exportXml(response: HttpServletResponse?) {
        val toWrite = data()
        if (response == null)
            DataExporter.export(DataExporter.ExportTypes.xml,
                    "template.property.mapping.export.xml",
                    "~/pkg/data/template_equalities.xml",
                    toWrite, TemplatePropertyMapping::class.java)
        else
            DataExporter.export(DataExporter.ExportTypes.xml, toWrite, response.outputStream,
                    TemplatePropertyMapping::class.java)
    }

    fun data(): MutableList<TemplatePropertyMapping> {
        val toWrite = mutableListOf<TemplatePropertyMapping>()
        var page = 0
        do {
            val list = dao.list(page = page++)
            logger.trace("I have read page " + page)
            toWrite.addAll(list.data)
        } while (!list.data.isEmpty())
        return toWrite
    }
}