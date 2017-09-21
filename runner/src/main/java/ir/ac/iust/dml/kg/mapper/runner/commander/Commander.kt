package ir.ac.iust.dml.kg.mapper.runner.commander

import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
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

  fun processArgs(command: String, arg1: String?, arg2: String?) {
    logger.info("====================================")
    logger.info("====================================")
    logger.info("running command $command at ${Date()}")
    logger.info("====================================")
    logger.info("====================================")
    try {
      when (command) {
        "ksMapLoad" -> services.ksMapLoad() // just for tests. can be removed.
        "triples" -> services.triples(arg1!!.toInt(), StoreType.valueOf(arg2!!)) //writes wikipedia triples
        "abstracts" -> services.abstracts(arg1!!.toInt(), StoreType.valueOf(arg2!!)) // writes wikipedia abstracts
        "withoutInfoBox" -> services.withoutInfoBox(arg1!!.toInt(), StoreType.valueOf(arg2!!)) // write wikipedia entities without info boxes
        "withInfoBox" -> services.withInfoBox(arg1!!.toInt(), StoreType.valueOf(arg2!!)) // write wikipedia entities with info boxes
        "tables" -> services.tables(StoreType.valueOf(arg1!!)) // write custom table as triples
        "redirects" -> services.redirects(arg1!!.toInt()) // writes all wikipedia redirects
        "ambiguities" -> services.ambiguities(arg1!!.toInt()) // writes all wikipedia ambiguities
        "predicates" -> services.predicates(true) // writes all predicates
        "dbpediaPredicates" -> services.dbpediaPredicates() // writes all predicates from dbpedia export
        "predicatesFast" -> services.predicates(false) // writes all predicates without ambiguation resolving
        "properties" -> services.properties(arg1!!.toInt(), StoreType.valueOf(arg2!!), true) // writes all not mapped properties
        "propertiesFast" -> services.properties(arg1!!.toInt(), StoreType.valueOf(arg2!!), false) // writes all not mapped properties without ambiguation resolving
        "raw" -> services.raw(StoreType.valueOf(arg1!!)) // writes all predicates
        "dumpUpdate" -> services.completeDumpUpdate(StoreType.valueOf(arg1!!), false) // all needed tasks in one place
        "completeDumpUpdate" -> services.completeDumpUpdate(StoreType.valueOf(arg1!!), true) // all needed tasks in one place
      }
    } catch (th: Throwable) {
      th.printStackTrace()
      logger.error(th)
    }
    logger.info("running command $command ended at ${Date()}. Bye bye!")
    System.exit(0)
  }
}