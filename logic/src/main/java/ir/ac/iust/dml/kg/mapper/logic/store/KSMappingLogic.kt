package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.mapper.logic.data.PropertyStats
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1mappingsApi
import ir.ac.iust.dml.kg.services.client.swagger.model.PagingListTemplateMapping
import ir.ac.iust.dml.kg.services.client.swagger.model.TemplateData
import ir.ac.iust.dml.kg.services.client.swagger.model.TemplateMapping
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class KSMappingLogic {

  val mappingApi: V1mappingsApi
  val allMapping = mutableListOf<TemplateMapping>()
  val indexTemplateNames = mutableMapOf<String, Int>()
  var propertyToTemplate = mutableListOf<PropertyStats>()
  var indexPropertyToTemplate = mutableMapOf<String, Int>()

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    mappingApi = V1mappingsApi(client)
  }

  @PostConstruct
  fun load() {
    var page = 0
    do {
      val pages = mappingApi.readAll1(page++, 100)
      allMapping.addAll(pages.data)
    } while (pages.page < pages.pageCount)
    rebuildIndexes()
  }

  fun insert(data: TemplateData): Boolean? {
    val success = mappingApi.insert2(data)
    if (success) {
      val mappingIndex = indexTemplateNames[data.template]!!
      val updated = mappingApi.readAll1(mappingIndex, 1).data[0]
      allMapping[mappingIndex] = updated
      // TODO make it faster. we don't need to calculate stats for all properties
      rebuildPropertyStats()
    }
    return success
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
      val classNameAndPrefix = if (className.startsWith("fkgo:")) className else "fkgo:" + className
      filtered = filtered.filter {
        it.rules.filter {
          it.predicate == "rdf:type" && compare(classNameLike, it.constant, classNameAndPrefix)
        }.isNotEmpty()
      }
    }
    if (propertyName != null) {
      filtered = filtered.filter {
        it.properties.filter { compare(propertyNameLike, it.property, propertyName) }.isNotEmpty()
      }
    }
    if (predicateName != null) {
      filtered = filtered.filter {
        it.properties.filter {
          it.rules.filter { compare(predicateNameLike, it.predicate, predicateName) }.isNotEmpty()
              || it.recommendations.filter { compare(predicateNameLike, it.predicate, predicateName) }.isNotEmpty()
        }.isNotEmpty()
      }
    }
    if (approved != null) {
      filtered = filtered.filter {
        it.properties.filter { it.recommendations.isEmpty() == approved }.isNotEmpty()
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
    if (className != null)
      filtered = filtered.filter { it.classes.filter { compare(classNameLike, it, className) }.isNotEmpty() }
    if (predicateName != null)
      filtered = filtered.filter { it.predicates.filter { compare(predicateNameLike, it, predicateName) }.isNotEmpty() }
    if (allNull != null)
      filtered = filtered.filter { (it.nullInTemplates.size == it.templates.size) == allNull }
    if (oneNull != null)
      filtered = filtered.filter { it.nullInTemplates.isNotEmpty() == oneNull }
    if (approved != null)
      filtered = filtered.filter { (it.approvedInTemplates.size == it.templates.size) == approved }
    return asPages(page, pageSize, filtered)
  }

  private fun rebuildIndexes() {
    allMapping.forEachIndexed { index, mapping ->
      indexTemplateNames[mapping.template] = index
    }
    rebuildPropertyStats()
  }

  private fun rebuildPropertyStats() {
    val properties = mutableMapOf<String, PropertyStats>()
    allMapping.forEach { template ->
      val classes = mutableSetOf<String>()
      template.rules.forEach { if (it.predicate == "rdf:type" && it.constant != null) classes.add(it.constant) }

      template.properties.forEach { pm ->
        val stats = properties.getOrPut(pm.property, { PropertyStats(property = pm.property) })
        stats.templates.add(template.template)
        stats.classes.addAll(classes)
        pm.recommendations.forEach {
          if (it.predicate != null) stats.predicates.add(it.predicate)
          else stats.nullInTemplates.add(template.template)
        }
        pm.rules.forEach {
          if (it.predicate != null) stats.predicates.add(it.predicate)
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

  private fun <T> asPages(page: Int, pageSize: Int, list: List<T>): PagedData<T> {
    val startIndex = page * pageSize
    val data =
        if (list.size < startIndex) mutableListOf<T>()
        else {
          val endIndex = startIndex + pageSize
          list.subList(startIndex, if (list.size < endIndex) list.size else endIndex).toMutableList()
        }
    val totalSize = list.size.toLong()
    val pageCount = (totalSize / pageSize) + (if (totalSize % pageSize == 0L) 0 else 1)
    return PagedData(data, page, pageSize, pageCount, totalSize)
  }

  private fun compare(like: Boolean, first: String?, second: String)
      = if (like) first?.contains(second, true) ?: false else first == second
}