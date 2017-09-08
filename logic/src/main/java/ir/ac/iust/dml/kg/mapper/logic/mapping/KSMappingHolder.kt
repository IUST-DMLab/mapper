package ir.ac.iust.dml.kg.mapper.logic.mapping

import ir.ac.iust.dml.kg.mapper.logic.data.PropertyMapping
import ir.ac.iust.dml.kg.mapper.logic.data.TemplateMapping
import ir.ac.iust.dml.kg.mapper.logic.ontology.OntologyLogic
import ir.ac.iust.dml.kg.mapper.logic.utils.KSMappingConverter
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PropertyNormaller
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1mappingsApi
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class KSMappingHolder {

  private val logger = Logger.getLogger(this.javaClass)!!
  private val maps = mutableMapOf<String, TemplateMapping>()
  @Autowired lateinit var ontologyLogic: OntologyLogic

  fun isValidTemplate(template: String) = maps.containsKey(template)

  fun getTemplateMapping(template: String)
      = maps.getOrPut(template, { TemplateMapping(template) })

  fun getPropertyMapping(template: String, property: String)
      = getTemplateMapping(template).properties!!.getOrPut(property, { PropertyMapping() })

  fun examinePropertyMapping(template: String, property: String)
      = getTemplateMapping(template).properties!![property]

  override fun toString() = buildString { maps.values.forEach { this.append(it).append('\n') } }

  private val mappingApi: V1mappingsApi

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    mappingApi = V1mappingsApi(client)
  }

  fun all() = maps.values

  fun writeToKS() {
    mappingApi.batchInsert1(maps.values.map { KSMappingConverter.convert(it) })
  }

  fun loadFromKS() {
    maps.clear()
    val start = System.currentTimeMillis()
    val all = mappingApi.readAll1(0, null).data
    all.forEach {
      val tm = getTemplateMapping(it.template)
      tm.weight = it.weight
      tm.template = it.template
      tm.rules = it.rules.map { KSMappingConverter.convert(it) }.toMutableSet()
      tm.ontologyClass = (it.rules.filter { it.predicate == URIs.typePrefixed }
          .firstOrNull()?.constant ?: URIs.getFkgOntologyClassPrefixed("Thing")).substringAfterLast(":")
      tm.tree = ontologyLogic.getTree(tm.ontologyClass)?.split("/") ?: listOf(tm.ontologyClass)
      it.properties.forEach { pm ->
        val property = PropertyNormaller.removeDigits(pm.property)
        tm.properties!![property] = PropertyMapping(
            property = pm.property, weight = pm.weight,
            rules = pm.rules.map { KSMappingConverter.convert(it) }.toMutableSet(),
            recommendations = pm.recommendations.map { KSMappingConverter.convert(it) }.toMutableSet())
      }
    }
    logger.info("mapping are loaded in ${System.currentTimeMillis() - start} milliseconds: ${maps.values.size}")
  }
}