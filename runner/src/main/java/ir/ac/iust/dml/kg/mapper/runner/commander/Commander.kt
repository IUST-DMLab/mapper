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

  fun processArgs(command: String, vararg args: String?) {
    logger.info("====================================")
    logger.info("====================================")
    logger.info("running command $command at ${Date()}")
    logger.info("====================================")
    logger.info("====================================")
    try {
      when (command) {
        "createTestSet" -> services.createTestSet(if (args.isNotEmpty()) args[0] else "subjects.txt")
        "fix" -> services.fix(args)
        "ksMapLoad" -> services.ksMapLoad() // just for tests. can be removed.
        "sameAs" -> services.sameAs(args[0]!!.toInt(), StoreType.valueOf(args[1]!!)) //write same as
        "triples" -> services.triples(args[0]!!.toInt(), StoreType.valueOf(args[1]!!)) //writes wikipedia triples
        "categoryTriples" -> services.categoryTriples(args[0]!!.toInt(), StoreType.valueOf(args[1]!!)) //writes wikipedia triples
        "abstracts" -> services.abstracts(args[0]!!.toInt(), StoreType.valueOf(args[1]!!)) // writes wikipedia abstracts
        "withoutInfoBox" -> services.withoutInfoBox(args[0]!!.toInt(), StoreType.valueOf(args[1]!!)) // write wikipedia entities without info boxes
        "withInfoBox" -> services.withInfoBox(args[0]!!.toInt(), StoreType.valueOf(args[1]!!)) // write wikipedia entities with info boxes
        "tables" -> services.tables(StoreType.valueOf(args[0]!!)) // write custom table as triples
        "redirects" -> services.redirects(args[0]!!.toInt()) // writes all wikipedia redirects
        "ambiguities" -> services.ambiguities(args[0]!!.toInt()) // writes all wikipedia ambiguities
        "predicates" -> services.predicates(true) // writes all predicates
        "dbpediaPredicates" -> services.dbpediaPredicates() // writes all predicates from dbpedia export
        "predicatesFast" -> services.predicates(false) // writes all predicates without ambiguation resolving
        "properties" -> services.properties(args[0]!!.toInt(), StoreType.valueOf(args[1]!!), true) // writes all not mapped properties
        "propertiesFast" -> services.properties(args[0]!!.toInt(), StoreType.valueOf(args[1]!!), false) // writes all not mapped properties without ambiguation resolving
        "raw" -> services.raw(StoreType.valueOf(args[0]!!), if (args.size > 1) args[1]!!.toBoolean() else null) // writes all predicates
        "dumpUpdate" -> services.completeDumpUpdate(StoreType.valueOf(args[0]!!), false) // all needed tasks in one place
        "completeDumpUpdate" -> services.completeDumpUpdate(StoreType.valueOf(args[0]!!), true) // all needed tasks in one place
        "fileToStore" -> services.fileToStore(
            if (args.isNotEmpty()) args[0]!!.toInt() else null,
            if (args.size > 1) args[1]!! else null)
        "fastWikiUpdate" -> services.fastWikiUpdate()
      }
    } catch (th: Throwable) {
      th.printStackTrace()
      logger.error(th)
    }
    logger.info("running command $command ended at ${Date()}. Bye bye!")
    System.exit(0)
  }
}