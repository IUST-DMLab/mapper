package ir.ac.iust.dml.kg.mapper.runner.commander

import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
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
        "ksMapLoad" -> services.ksMapLoad() // just for tests. can be removed.
        "migrate" -> services.migrate() // migrates data from mysql to knowledge store
        "load" -> services.load() // deprecated
        "loadTypes" -> services.loadTypes() // deprecated
        "createStatsFile" -> services.createStatsFile() // deprecated
        "writeStats" -> services.writeStats() // deprecated
        "generateMapping" -> services.generateMapping() // deprecated
        "tree" -> services.writeTree(StoreType.valueOf(arg!!)) //writes ontology tree form mysql. it will be deprecated
        "triples" -> services.triples(StoreType.valueOf(arg!!)) //writes wikipedia triples
        "abstracts" -> services.abstracts(StoreType.valueOf(arg!!)) // writes wikipedia abstracts
        "withoutInfoBox" -> services.withoutInfoBox(StoreType.valueOf(arg!!)) // write wikipedia entities without info boxes
        "withInfoBox" -> services.withInfoBox(StoreType.valueOf(arg!!)) // write wikipedia entities with info boxes
        "tables" -> services.tables(StoreType.valueOf(arg!!)) // write custom table as triples
        "redirects" -> services.redirects() // writes all wikipedia redirects
        "ambiguities" -> services.ambiguities() // writes all wikipedia ambiguities
        "predicates" -> services.predicates(StoreType.valueOf(arg!!)) // writes all predicates
        "dumpUpdate" -> services.completeDumpUpdate(StoreType.valueOf(arg!!), false) // all needed tasks in one place
        "completeDumpUpdate" -> services.completeDumpUpdate(StoreType.valueOf(arg!!), true) // all needed tasks in one place
      }
    } catch (th: Throwable) {
      logger.error(th)
    }
    logger.info("running command $command ended at ${Date()}. Bye bye!")
    System.exit(0)
  }
}