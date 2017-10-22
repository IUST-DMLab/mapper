/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.viewer

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1triplesApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValue
import org.springframework.stereotype.Service
import java.util.*

@Service
class V1EntityViewer {
  private val tripleApi: V1triplesApi
  private val THING = URIs.getFkgOntologyClassUri("Thing")
  private val propertyLabelCache = mutableMapOf<String, String?>()
  private val filteredPredicates = listOf(
      Regex(URIs.prefixedToUri(URIs.fkgNotMappedPropertyPrefix + ":")!!.replace(".", "\\.") + "[\\d\\w]*"),
      Regex(URIs.fkgOntologyPrefixUrl.replace(".", "\\.") + ".*[yY]ear.*"),
      Regex(URIs.getFkgOntologyPropertyUri("wiki").replace(".", "\\.") + ".*"),
      Regex(URIs.getFkgOntologyPropertyUri("ویکی").replace(".", "\\.") + ".*"),
      Regex(URIs.picture.replace(".", "\\.")),
      Regex(URIs.instanceOf.replace(".", "\\.")),
      Regex(URIs.type.replace(".", "\\.")),
      Regex(URIs.abstract.replace(".", "\\.")),
      Regex(URIs.label.replace(".", "\\.")),
      Regex(URIs.prefixedToUri("foaf:homepage")!!.replace(".", "\\.")),
      Regex(URIs.prefixedToUri("fkgo:predecessor")!!.replace(".", "\\.")),
      Regex(URIs.prefixedToUri("fkgo:successor")!!.replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("source").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("data").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("fontSize").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("imageSize").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("depictionDescription").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("nameData").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("quotation").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("quote").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("quoted").replace(".", "\\.")),
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

  data class EntityPropertyValue(val value: String, var url: String? = null, var image: Boolean = false) : Comparable<EntityPropertyValue> {
    override fun compareTo(other: EntityPropertyValue) = this.value.compareTo(other.value)
    override fun hashCode() = value.hashCode()
    override fun equals(other: Any?) = value == (other as EntityPropertyValue).value
  }

  data class EntityData(var label: String? = null, var type: String? = null, var wikiLink: String? = null,
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

  fun getEntityData(url: String, properties: Boolean = true): EntityData {
    val result = EntityData()
    val entityDefaultName = url.substringAfterLast("/").replace('_', ' ')
    result.wikiLink = "https://fa.wikipedia.org/wiki/" + entityDefaultName.replace(' ', '_')
    result.label = entityDefaultName
    var searched = search(url, URIs.abstract, null, true)
    result.abstract = searched.data.firstOrNull()?.`object`?.value
    if (result.abstract?.length ?: 0 > 250) result.abstract = result.abstract + " ..."
    searched = search(url, URIs.picture, null, true)
    result.image = searched.data.firstOrNull()?.`object`?.value
    if (properties) {
      searched = search(url, URIs.instanceOf, null, true)
      var type = if (searched.data.isEmpty()) THING else searched.data[0].`object`.value
      if (type == THING) type = null
      result.type = if (type == null) null else getLabel(type)
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
            values.add(EntityPropertyValue(l ?: it.`object`.value.substringAfterLast("/").replace('_', ' '),
                it.`object`.value, it.`object`.value.contains("upload.wikimedia.org")))
          } else {
            if (!LanguageChecker.multiLanguages(it.`object`.value))
              values.add(EntityPropertyValue(it.`object`.value))
          }
        }
      }
      result.properties = result.properties.filter { it.value.isNotEmpty() }.toSortedMap()
    }
    return result
  }

  fun getEntities(entities: MutableList<String>) = entities.map { getEntityData(it, false) }
}