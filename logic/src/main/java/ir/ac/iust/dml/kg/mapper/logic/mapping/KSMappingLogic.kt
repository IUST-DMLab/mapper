/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.mapping

import ir.ac.iust.dml.kg.mapper.logic.data.PropertyStats
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PageUtils
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V2mappingsApi
import ir.ac.iust.dml.kg.services.client.swagger.V2ontologyApi
import ir.ac.iust.dml.kg.services.client.swagger.model.PagingListTemplateMapping
import ir.ac.iust.dml.kg.services.client.swagger.model.TemplateData
import ir.ac.iust.dml.kg.services.client.swagger.model.TemplateMapping
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class KSMappingLogic {

  private val mappingApi: V2mappingsApi
  private val ontologyApi: V2ontologyApi
  private val allMapping = mutableListOf<TemplateMapping>()
  private val indexTemplateNames = mutableMapOf<String, Int>()
  private var propertyToTemplate = mutableListOf<PropertyStats>()
  private var uniquePredicates = mutableSetOf<String>()
  private var indexPropertyToTemplate = mutableMapOf<String, Int>()

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    mappingApi = V2mappingsApi(client)
    ontologyApi = V2ontologyApi(client)
  }

  @PostConstruct
  fun load() {
    var page = 0
    do {
      val pages = mappingApi.readAll2(page++, 100)
      pages.data.forEach { fixData(it) }
      allMapping.addAll(pages.data.sortedByDescending { it.weight })
    } while (pages.page < pages.pageCount)
    rebuildIndexes()
  }

  private fun fixData(data: TemplateMapping) {
    data.rules.forEach {
      if (it.predicate?.startsWith("http://") == true) it.predicate = URIs.replaceAllPrefixesInString(it.predicate)
    }
    data.properties.forEach {
      if (it.recommendations.size == 1 && it.rules.size > 0) it.recommendations.clear()
      it.rules?.forEach {
        if (it.predicate?.startsWith("http://") == true) it.predicate = URIs.replaceAllPrefixesInString(it.predicate)
      }
      it.recommendations.forEach {
        if (it.predicate?.startsWith("http://") == true) it.predicate = URIs.replaceAllPrefixesInString(it.predicate)
      }
    }
  }

  private fun fixData(data: TemplateData) {
    data.rules.forEach {
      if (it.predicate?.startsWith("http://") == true) it.predicate = URIs.replaceAllPrefixesInString(it.predicate)
    }
    data.properties.forEach {
      it.rules?.forEach {
        if (it.predicate?.startsWith("http://") == true) it.predicate = URIs.replaceAllPrefixesInString(it.predicate)
      }
      it.recommendations.forEach {
        if (it.predicate?.startsWith("http://") == true) it.predicate = URIs.replaceAllPrefixesInString(it.predicate)
      }
    }
  }

  fun insert(data: TemplateData): TemplateMapping? {
    fixData(data)
    if (indexTemplateNames.isEmpty()) load()
    data.incremental = false
    val success = mappingApi.insert5(data)
    if (success != null) {
      val mappingIndex = indexTemplateNames[data.template]!!
      val updated = mappingApi.readAll2(0, 0).data.first { it.template == data.template }
      if (updated != null) allMapping[mappingIndex] = updated
      // TODO make it faster. we don't need to calculate stats for all properties
      rebuildPropertyStats()
      return updated
    }
    return null
  }

  fun search(page: Int, pageSize: Int,
             templateName: String?, templateNameLike: Boolean,
             className: String?, classNameLike: Boolean,
             propertyName: String?, propertyNameLike: Boolean,
             predicateName: String?, predicateNameLike: Boolean,
             approved: Boolean?): PagingListTemplateMapping {
    var filtered: List<TemplateMapping> = allMapping
    if (templateName != null) {
      if (templateNameLike) filtered = filtered.filter { it.template?.contains(templateName) ?: false }
      else filtered = filtered.filter { it.template == templateName }
    }
    if (className != null) {
      val classNameAndPrefix =
          if (className.contains(":")) className
          else URIs.getFkgOntologyClassPrefixed(className)
      filtered = filtered.filter {
        it.rules.any {
          it.predicate == URIs.type && compare(classNameLike, it.constant, classNameAndPrefix)
        }
      }
    }
    if (propertyName != null) {
      filtered = filtered.filter {
        it.properties.any { compare(propertyNameLike, it.property, propertyName) }
      }
    }
    if (predicateName != null) {
      filtered = filtered.filter {
        it.properties.any {
          it.rules.any { compare(predicateNameLike, it.predicate, predicateName) }
              || it.recommendations.any { compare(predicateNameLike, it.predicate, predicateName) }
        }
      }
    }
    if (approved != null) {
      filtered = filtered.filter {
        it.properties.any { it.recommendations.isEmpty() == approved }
      }
    }
    return asPages(page, pageSize, filtered)
  }

  fun searchProperty(page: Int, pageSize: Int,
                     propertyName: String?, propertyNameLike: Boolean,
                     templateName: String?, templateNameLike: Boolean,
                     className: String?, classNameLike: Boolean,
                     predicateName: String?, predicateNameLike: Boolean,
                     allNull: Boolean? = null, oneNull: Boolean? = null,
                     approved: Boolean?): PagedData<PropertyStats> {
    var filtered: List<PropertyStats> = propertyToTemplate
    if (propertyName != null)
      filtered = filtered.filter { compare(propertyNameLike, it.property, propertyName) }
    if (templateName != null)
      filtered = filtered.filter { it.templates.filter { compare(templateNameLike, it, templateName) }.isNotEmpty() }
    if (className != null) {
      val fixedClassName =
          if (className.contains(":")) className
          else URIs.getFkgOntologyClassPrefixed(className)
      filtered = filtered.filter { it.classes.filter { compare(classNameLike, it, fixedClassName) }.isNotEmpty() }
    }
    if (predicateName != null)
      filtered = filtered.filter { it.predicates.filter { compare(predicateNameLike, it, predicateName) }.isNotEmpty() }
    if (allNull != null)
      filtered = filtered.filter { (it.nullInTemplates.size == it.templates.size) == allNull }
    if (oneNull != null)
      filtered = filtered.filter { it.nullInTemplates.isNotEmpty() == oneNull }
    if (approved != null)
      filtered = filtered.filter { (it.approvedInTemplates.size == it.templates.size) == approved }
    return PageUtils.asPages(page, pageSize, filtered)
  }

  fun predicateProposal(keyword: String): List<String> {
    val proposals = mutableListOf<String>()
    // search in ontology and other mapped predicates
    val lc = keyword.toLowerCase()
    uniquePredicates.forEach { if (it.toLowerCase().contains(lc)) proposals.add(it) }
    if (keyword.length < 2) return mutableListOf()
    val result = ontologyApi.search2(null, null, keyword, true, URIs.type, null,
        URIs.typeOfAnyProperties, null, null, 0, 0)
    val propertyPrefix = URIs.prefixedToUri(URIs.fkgNotMappedPropertyPrefix + ":")!!
    result.data.forEach {
      if (!it.subject.startsWith(propertyPrefix)) proposals.add(URIs.replaceAllPrefixesInString(it.subject)!!)
    }
    return proposals
  }

  private fun rebuildIndexes() {
    allMapping.forEachIndexed { index, mapping ->
      indexTemplateNames[mapping.template] = index
    }
    rebuildPropertyStats()
  }

  private fun rebuildPropertyStats() {
    val properties = mutableMapOf<String, PropertyStats>()
    uniquePredicates.clear()
    allMapping.forEach { template ->
      val classes = mutableSetOf<String>()
      template.rules.forEach {
        if (it.predicate == URIs.type && it.constant != null) classes.add(it.constant)
        if (it.predicate != null && !it.predicate.startsWith(URIs.fkgMainPrefix)) uniquePredicates.add(it.predicate)
      }

      template.properties.forEach { pm ->
        val stats = properties.getOrPut(pm.property, { PropertyStats(property = pm.property) })
        stats.templates.add(template.template)
        stats.classes.addAll(classes)
        pm.recommendations.forEach {
          if (it.predicate != null) {
            stats.predicates.add(it.predicate)
            if (!it.predicate.startsWith(URIs.fkgMainPrefix)) uniquePredicates.add(it.predicate)
          }
          else stats.nullInTemplates.add(template.template)
        }
        pm.rules.forEach {
          if (it.predicate != null) {
            stats.predicates.add(it.predicate)
            if (!it.predicate.startsWith(URIs.fkgMainPrefix)) uniquePredicates.add(it.predicate)
          }
          else stats.nullInTemplates.add(template.template)
          stats.approvedInTemplates.add(template.template)
        }
      }
    }
    val list = properties.values.sortedBy { it.property }.toMutableList()
    val indexes = mutableMapOf<String, Int>()
    list.forEachIndexed { index, (property) -> indexes[property] = index }
    this.propertyToTemplate = list
    this.indexPropertyToTemplate = indexes
  }

  private fun asPages(page: Int, pageSize: Int, list: List<TemplateMapping>): PagingListTemplateMapping {
    val pages = PagingListTemplateMapping()
    val startIndex = page * pageSize
    pages.data =
        if (list.size < startIndex) listOf()
        else {
          val endIndex = startIndex + pageSize
          list.subList(startIndex, if (list.size < endIndex) list.size else endIndex)
        }
    pages.page = page
    pages.pageSize = pageSize
    pages.totalSize = list.size.toLong()
    pages.pageCount = (pages.totalSize / pages.pageSize) + (if (pages.totalSize % pages.pageSize == 0L) 0 else 1)
    return pages
  }

  private fun compare(like: Boolean, first: String?, second: String)
      = if (like) first?.contains(second, true) ?: false else first == second
}