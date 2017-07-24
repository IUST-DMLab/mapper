package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
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

  private fun rebuildIndexes() {
    allMapping.forEachIndexed { index, mapping ->
      indexTemplateNames[mapping.template] = index
    }
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