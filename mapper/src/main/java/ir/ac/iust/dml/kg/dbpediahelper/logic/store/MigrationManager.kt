package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgPropertyMappingDao
import ir.ac.iust.dml.kg.access.dao.FkgTemplateMappingDao
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.MapRule
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.ValueType
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MigrationManager {

  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var templateDao: FkgTemplateMappingDao
  @Autowired private lateinit var propertyDao: FkgPropertyMappingDao
  @Autowired private lateinit var holder: KSMappingHolder

  fun migrate() {
    migrateTemplateMapping()
    migratePropertyMapping()
  }

  fun save() = holder.writeToKS()

  val validTemplatePrefixes = mapOf(
      "en" to listOf("infobox ", "taxobox ", "chembox ", "reactionbox ", "ionbox ", "drugbox ", "geobox ",
          "planetbox ", "starbox ", "drugclassbox ", "speciesbox ", "comiccharacterbox "),
      "fa" to listOf("جعبه اطلاعات ", "جعبه ")
  )

  private fun getValidTemplateName(templateName: String): String {
    var valid = false
    val prefixes = validTemplatePrefixes[LanguageChecker.detectLanguage(templateName)]!!
    prefixes.forEach {
      if (templateName.startsWith(it)) {
        valid = true; return@forEach
      }
    }
    if (!valid) {
      prefixes.forEach { prefix ->
        if (holder.isValidTemplate(prefix + templateName)) {
          val result = prefix + templateName
          logger.trace("$templateName converted to $result")
          return result
        }
      }
      val result = prefixes[0] + templateName
      logger.trace("$templateName converted to new $result")
      return prefixes[0] + templateName
    }
    return templateName
  }

  private fun migratePropertyMapping() {
    var start = System.currentTimeMillis()
    val all = propertyDao.search(page = 0, pageSize = 0, language = null).data
    logger.info("all property data loaded from database " + (System.currentTimeMillis() - start))

    start = System.currentTimeMillis()
    all.forEach {
      val templateName = getValidTemplateName(it.templateName!!.toLowerCase().replace('_', ' ').replace('-', ' '))
      val tm = holder.getTemplateMapping(templateName)
      if (tm.rules!!.isEmpty() && it.ontologyClass == null) return@forEach
      if (tm.rules!!.isEmpty()) {
        logger.info("no explicit template mapping existed for ${it.templateName} to ${it.ontologyClass}")
        tm.rules!!.add(MapRule(
            predicate = "rdf:type",
            constant = "fkgo:" + it.ontologyClass,
            type = ValueType.Resource
        ))
      }
      val templateProperty = it.templateProperty!!
      if (templateProperty.isBlank()) return@forEach
      val pm = holder.getPropertyMapping(templateName, templateProperty)
      pm.property = templateProperty
      pm.weight = it.tupleCount?.toDouble()
      val rule = MapRule(
          predicate = it.ontologyProperty!!.replace("dbo:", "fkgo:").replace("dbp:", "fkgp:"),
          type = ValueType.String,
          unit = null,
          constant = null,
          transform = null)
      if (it.approved ?: false) pm.rules.add(rule)
      else pm.recommendations.add(rule)
    }
    logger.info("all property data written in memory with new structure: " + (System.currentTimeMillis() - start))
  }

  fun migrateTemplateMapping() {
    var start = System.currentTimeMillis()
    val all = templateDao.search(page = 0, pageSize = 0).data
    logger.info("all template data loaded from database " + (System.currentTimeMillis() - start))

    start = System.currentTimeMillis()
    all.forEach {
      val templateName = it.templateName!!.toLowerCase().replace('_', ' ').replace('-', ' ')
      val tm = holder.getTemplateMapping(templateName)
      tm.rules!!.add(MapRule(
          predicate = "rdf:type",
          constant = "fkgo:" + it.ontologyClass,
          type = ValueType.Resource
      ))
      tm.weight = it.tupleCount!!.toDouble()
    }

    logger.info("all template data written in memory with new structure: " + (System.currentTimeMillis() - start))
  }
}