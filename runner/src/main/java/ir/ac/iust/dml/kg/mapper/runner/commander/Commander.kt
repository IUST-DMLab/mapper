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
        "load" -> services.load() // deprecated
        "loadTypes" -> services.loadTypes() // deprecated
        "createStatsFile" -> services.createStatsFile() // deprecated
        "writeStats" -> services.writeStats() // deprecated
        "generateMapping" -> services.generateMapping() // deprecated
        "triples" -> services.triples(TripleImporter.StoreType.valueOf(arg!!)) // deprecated
        "kgTriples" -> services.kgTriples(TripleImporter.StoreType.valueOf(arg!!))
        "kgTables" -> services.kgTables(TripleImporter.StoreType.valueOf(arg!!))
        "allTriples" -> services.allTriples(TripleImporter.StoreType.valueOf(arg!!))
        "rewriteLabels" -> services.rewriteLabels(TripleImporter.StoreType.valueOf(arg!!)) // fixes labels
        "redirects" -> services.redirects()
        "ambiguities" -> services.ambiguities()
        "entities" -> services.entities()
        "predicates" -> services.predicates()
        "relations" -> services.relations() // deprecated
        "migrate" -> services.migrate()
        "ksMapLoad" -> services.ksMapLoad() // just for tests. can be removed.
        "completeDumpUpdate" -> services.completeDumpUpdate(TripleImporter.StoreType.valueOf(arg!!)) // all needed tasks in one place
      }
    } catch (th: Throwable) {
      logger.error(th)
    }
    logger.info("running command $command ended at ${Date()}. Bye bye!")
    System.exit(0)
  }
}