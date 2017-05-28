package ir.ac.iust.dml.kg.mapper.runner.commander

import ir.ac.iust.dml.kg.dbpediahelper.logic.TripleImporter
import ir.ac.iust.dml.kg.mapper.runner.web.rest.MappingHelperRestServices
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class Commander {
  @Autowired
  lateinit var services: MappingHelperRestServices
  private val logger = Logger.getLogger(this.javaClass)!!

  fun processArgs(command: String, arg: String? = null) {
    logger.info("====================================")
    logger.info("====================================")
    logger.info("running command $command at ${Date()}")
    logger.info("====================================")
    logger.info("====================================")
    try {
      when (command) {
        "load" -> services.load()
        "loadTypes" -> services.loadTypes()
        "createStatsFile" -> services.createStatsFile()
        "writeStats" -> services.writeStats()
        "generateMapping" -> services.generateMapping()
        "triples" -> services.triples(TripleImporter.StoreType.valueOf(arg!!))
        "kgTriples" -> services.kgTriples(TripleImporter.StoreType.valueOf(arg!!))
        "allTriples" -> services.allTriples(TripleImporter.StoreType.valueOf(arg!!))
        "rewriteLabels" -> services.rewriteLabels(TripleImporter.StoreType.valueOf(arg!!))
        "redirects" -> services.redirects()
        "entities" -> services.entities()
        "relations" -> services.relations()
        "migrate" -> services.migrate()
        "ksMapLoad" -> services.ksMapLoad()
      }
    } catch (th: Throwable) {
      logger.error(th)
    }
    logger.info("running command $command ended at ${Date()}. Bye bye!")
    System.exit(0)
  }
}