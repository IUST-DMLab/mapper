package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1triplesApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValue
import org.springframework.stereotype.Service
import java.util.*

@Service
class EntityViewer {
  val tripleApi: V1triplesApi
  val THING = URIs.getFkgOntologyClassUri("Thing")
  val propertyLabelCache = mutableMapOf<String, String?>()
  val filteredPredicates = listOf(
      Regex(URIs.prefixedToUri(URIs.fkgNotMappedPropertyPrefix + ":")!!.replace(".", "\\.") + "[\\d\\w]*"),
      Regex(URIs.prefixedToUri(URIs.fkgOntologyPrefix + ":")!!.replace(".", "\\.") + ".*[yY]ear.*"),
      Regex(URIs.getFkgOntologyPropertyUri("wiki").replace(".", "\\.") + ".*"),
      Regex(URIs.picture.replace(".", "\\.")),
      Regex(URIs.instanceOf.replace(".", "\\.")),
      Regex(URIs.type.replace(".", "\\.")),
      Regex(URIs.abstract.replace(".", "\\.")),
      Regex(URIs.label.replace(".", "\\.")),
      Regex(URIs.prefixedToUri("foaf:homepage")!!.replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("source").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("data").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("fontSize").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("nameData").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("quotation").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("quote").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("signature").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("width").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("sourceAlign").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("sourceAlignment").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("align").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("alignment").replace(".", "\\."))
  )

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    tripleApi = V1triplesApi(client)
  }

  data class EntityPropertyValue(val value: String, var url: String? = null) : Comparator<Any> {
    override fun compare(o1: Any?, o2: Any?): Int {
      if (o1 is EntityPropertyValue && o2 is EntityPropertyValue) o1.value.compareTo(o2.value)
      return 0
    }

    override fun hashCode() = value.hashCode()
    override fun equals(other: Any?) = value == (other as EntityPropertyValue).value
  }

  data class EntityData(var label: String? = null, var type: String? = null,
                        var abstract: String? = null, var image: String? = null,
                        var properties: SortedMap<String, SortedSet<EntityPropertyValue>> = sortedMapOf())

  private fun search(subject: String? = null, predicate: String? = null, `object`: String? = null, one: Boolean)
      = tripleApi.search1(null, null, subject, false, predicate,
      false, `object`, null, 0, if (one) 1 else 0)

  private fun getLabel(url: String): String? {
    var propertyLabel = getFirstPersianValue(url, URIs.label, LanguageChecker::isPersian)
    if (propertyLabel != null) return propertyLabel
    propertyLabel = getFirstPersianValue(url, URIs.variantLabel, LanguageChecker::isPersian)
    return propertyLabel
  }

  private fun getFirstPersianValue(subject: String, predicate: String, filter: (String) -> Boolean): String?
      = search(subject, predicate, null, false).data.filter { filter(it.`object`.value) }
      .firstOrNull()?.`object`?.value

  fun getEntityData(url: String): EntityData {
    val result = EntityData()
    val entityDefaultName = url.substringAfterLast("/")
    var searched = search(url, URIs.label, null, true)
    result.label = if (searched.data.isEmpty()) entityDefaultName else searched.data[0].`object`.value
    searched = search(url, URIs.instanceOf, null, true)
    result.type = getLabel(if (searched.data.isEmpty()) THING else searched.data[0].`object`.value)
    searched = search(url, URIs.abstract, null, true)
    result.abstract = searched.data.firstOrNull()?.`object`?.value
    searched = search(url, URIs.picture, null, true)
    result.image = searched.data.firstOrNull()?.`object`?.value
    searched = search(url, null, null, false)
    searched.data.filter { triple ->
      filteredPredicates.none { it.matches(triple.predicate) }
          && triple.`object`.value != "no"
    }.forEach {
      val propertyLabel = propertyLabelCache.getOrPut(it.predicate, { getLabel(it.predicate) })
      if (propertyLabel != null) {
        val values = result.properties.getOrPut(propertyLabel, { sortedSetOf() })
        if (it.`object`.type == TypedValue.TypeEnum.RESOURCE) {
          val l = getLabel(it.`object`.value)
          values.add(EntityPropertyValue(l ?: it.`object`.value.substringAfterLast("/"), it.`object`.value))
        } else values.add(EntityPropertyValue(it.`object`.value))
      }
    }
    return result
  }
}