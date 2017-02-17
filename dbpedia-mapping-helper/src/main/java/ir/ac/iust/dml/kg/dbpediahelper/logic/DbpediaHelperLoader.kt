package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.dbpediahelper.logic.dump.OwlDumpReader
import ir.ac.iust.dml.kg.utils.ConfigReader
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class DbpediaHelperLoader {

    //    @Autowired
//    lateinit var dao: TemplatePropertyMappingDao
    @Autowired
    lateinit var prefixService: PrefixService
    val logger = Logger.getLogger(this.javaClass)!!

    @Throws(Exception::class)
    fun load() {
        val ONTOLOGY_DUMP = "ontology.dump.en"
        val config = ConfigReader.getConfig(mapOf(ONTOLOGY_DUMP to "~/pkg/data/dbpedia_mapping.owl"))
        val path = ConfigReader.getPath(config[ONTOLOGY_DUMP]!! as String)
        Files.createDirectories(path.parent)
        if (!Files.exists(path)) {
            throw Exception("There is no file ${path.toAbsolutePath()} existed.")
        }

        prefixService.reload()
        OwlDumpReader(path).use {
            owlDumpReader ->
            while (owlDumpReader.hasNext()) {
                val triples = owlDumpReader.next()
                for (triple in triples) {
                    triple.subject = prefixService.replacePrefixes(triple.subject)
                    triple.predicate = prefixService.replacePrefixes(triple.predicate)
                    triple.objekt = prefixService.replacePrefixes(triple.objekt)
                    logger.info(triple)
                }
            }
        }
    }

}