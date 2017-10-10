/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.mapping

import ir.ac.iust.dml.kg.mapper.logic.data.PropertyMapping
import ir.ac.iust.dml.kg.mapper.logic.data.TemplateMapping
import ir.ac.iust.dml.kg.mapper.logic.ontology.OntologyLogic
import ir.ac.iust.dml.kg.mapper.logic.utils.KSMappingConverter
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PropertyNormaller
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V2mappingsApi
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
      = getTemplateMapping(template).properties!!.getOrPut(property, { PropertyMapping(property = property) })

  fun examinePropertyMapping(template: String, property: String)
      = getTemplateMapping(template).properties!![property]

  override fun toString() = buildString { maps.values.forEach { this.append(it).append('\n') } }

  private val mappingApi: V2mappingsApi

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    mappingApi = V2mappingsApi(client)
  }

  fun all() = maps.values

  fun writeToKS() {
    mappingApi.batchInsert3(maps.values.map { KSMappingConverter.convert(it) })
  }

  fun loadFromKS() {
    maps.clear()
    val start = System.currentTimeMillis()
    val all = mappingApi.readAll2(0, null).data
    all.forEach {
      val tm = getTemplateMapping(it.template)
      tm.weight = it.weight
      tm.template = it.template
      tm.rules = it.rules.map { KSMappingConverter.convert(it) }.toMutableList()
      tm.ontologyClass = (it.rules.filter { it.predicate == URIs.typePrefixed }
          .firstOrNull()?.constant ?: URIs.getFkgOntologyClassPrefixed("Thing")).substringAfterLast(":")
      tm.tree = ontologyLogic.getTree(tm.ontologyClass)?.split("/") ?: listOf(tm.ontologyClass)
      it.properties.forEach { p ->
        val property = PropertyNormaller.removeDigits(p.property)
        val pm = tm.properties!!.getOrPut(property, {
          PropertyMapping(
              property = p.property, weight = p.weight)
        })
        pm.rules.addAll(p.rules
            .filter { !(it.predicate?.startsWith("fkgp") ?: false) } // null predicates means deleting property
            .map { KSMappingConverter.convert(it) })
        pm.recommendations.addAll(p.recommendations
            .filter { !(it.predicate?.startsWith("fkgp") ?: false) }
            .map { KSMappingConverter.convert(it) }.toMutableList())
      }
    }
    logger.info("mapping are loaded in ${System.currentTimeMillis() - start} milliseconds: ${maps.values.size}")
  }
}