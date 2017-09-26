/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V2ontologyApi
import ir.ac.iust.dml.kg.services.client.swagger.V2subjectsApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TripleData
import ir.ac.iust.dml.kg.services.client.swagger.model.TripleObject
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValue
import org.springframework.stereotype.Service
import java.util.*

@Service
class V2EntityViewer {
  private val tripleApi: V2subjectsApi
  private val ontologyApi: V2ontologyApi
  //  private val ontologyApi: V1triplesApi
  private val THING = URIs.getFkgOntologyClassUri("Thing")
  private val propertyLabelCache = mutableMapOf<String, String?>(
      URIs.prefixedToUri("name")!! to "نام",
      URIs.prefixedToUri("order")!! to "عنوان",
      URIs.prefixedToUri("spouse")!! to "همسر",
      URIs.prefixedToUri("country")!! to "همسر",
      URIs.prefixedToUri("child")!! to "همسر",
      URIs.prefixedToUri("years")!! to "سال‌ها",
      URIs.prefixedToUri("activeYears")!! to "سال‌های فعالیت",
      URIs.prefixedToUri("activeYearsEndDate")!! to "سال پایان فعالیت",
      URIs.prefixedToUri("activeYearsStartDate")!! to "سال شروع فعالیت",
      URIs.prefixedToUri("vicepresident")!! to "نایب رییس",
      URIs.prefixedToUri("religion")!! to "دین",
      URIs.prefixedToUri("residence")!! to "محل اقامت",
      URIs.prefixedToUri("occupation")!! to "شغل",
      URIs.prefixedToUri("successor")!! to "بعدی",
      URIs.prefixedToUri("predecessor")!! to "قبلی",
      URIs.prefixedToUri("deathDate")!! to "زادمرگ",
      URIs.prefixedToUri("deathDate")!! to "زادمرگ",
      URIs.prefixedToUri("deathPlace")!! to "محل مرگ",
      URIs.prefixedToUri("birthDate")!! to "زادروز",
      URIs.prefixedToUri("birthPlace")!! to "محل تولد",
      URIs.prefixedToUri("almaMater")!! to "محل آموزش",
      URIs.prefixedToUri("award")!! to "جوایز",
      URIs.prefixedToUri("nationality")!! to "ملیت",
      URIs.prefixedToUri("homepage")!! to "صفحه اینترنتی"
  )
  private val filteredPredicates = listOf(
      Regex(URIs.prefixedToUri(URIs.fkgNotMappedPropertyPrefix + ":")!!.replace(".", "\\.") + "[\\d\\w]*"),
      //      Regex(URIs.fkgOntologyPrefixUrl.replace(".", "\\.") + ".*[yY]ear.*"),
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
      Regex(URIs.getFkgOntologyPropertyUri("categoryMember").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("activeYears").replace(".", "\\.")),
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
    tripleApi = V2subjectsApi(client)
    ontologyApi = V2ontologyApi(client)
//    ontologyApi = V1triplesApi(client)
  }

  data class EntityPropertyValue(val value: String, var url: String? = null, var image: Boolean = false) : Comparable<EntityPropertyValue> {
    override fun compareTo(other: EntityPropertyValue) = this.value.compareTo(other.value)
    override fun hashCode() = value.hashCode()
    override fun equals(other: Any?) = value == (other as EntityPropertyValue).value
  }

  data class EntityProperty(var value: EntityPropertyValue,
                            var moreInfo: MutableMap<String, EntityProperty> = mutableMapOf()) : Comparable<EntityProperty> {
    override fun compareTo(other: EntityProperty) = this.value.compareTo(other.value)
    override fun hashCode() = value.hashCode()
    override fun equals(other: Any?) = value == (other as EntityProperty).value
  }

  data class EntityData(var label: String? = null, var type: String? = null, var wikiLink: String? = null,
                        var abstract: String? = null, var image: String? = null,
                        var properties: SortedMap<String, SortedSet<EntityProperty>> = sortedMapOf())

  private fun ontologySearch(subject: String? = null, predicate: String? = null, `object`: String? = null, one: Boolean)
      = ontologyApi.search2(null, subject, predicate, `object`, null, 0, if (one) 1 else 0)
//      = ontologyApi.search1(null, false, subject, false, predicate, false,
//      `object`, false, 0, if (one) 1 else 0)

  private fun getLabel(url: String): String? {
    if (url.contains("/resource/")) return null
    val u = if (url.contains("/property/")) url.replace("/property/", "/ontology") else url
    var propertyLabel = getFilteredValue(u, URIs.label, LanguageChecker::isPersian)
    if (propertyLabel != null) return propertyLabel
    propertyLabel = getFilteredValue(u, URIs.variantLabel, LanguageChecker::isPersian)
    return propertyLabel
  }

  private fun getFilteredValue(subject: String, predicate: String, filter: (String) -> Boolean): String?
      = ontologySearch(subject, predicate, null, false).data.filter { filter(it.`object`.value) }
      .firstOrNull()?.`object`?.value

  fun search(triples: List<TripleData>, predicate: String): List<TripleData> {
    return triples.filter { it.predicate == predicate }
  }

  fun getEntityData(url: String, properties: Boolean = true): EntityData {
    val result = EntityData()
    val subjectTriples = tripleApi.get1(null, url).triples!!
    val entityDefaultName = url.substringAfterLast("/").replace('_', ' ')
    result.wikiLink = "https://fa.wikipedia.org/wiki/" + entityDefaultName.replace(' ', '_')
    var searched = subjectTriples[URIs.label]
    result.label = if (searched?.isEmpty() != false) entityDefaultName else searched[0].value
    searched = subjectTriples[URIs.abstract]
    result.abstract = searched?.firstOrNull()?.value

    searched = subjectTriples[URIs.picture]
    result.image = searched?.firstOrNull()?.value
    if (properties) {
      searched = subjectTriples[URIs.instanceOf]
      var type = searched?.firstOrNull { it.value != THING }?.value
      if (type == THING) type = null
      result.type = if (type == null) null else getLabel(type)
      subjectTriples.filter { triple ->
        filteredPredicates.none { it.matches(triple.key) }
            && triple.value[0].value != "no"
      }.forEach { triple ->
        var propertyLabel = propertyLabelCache.getOrPut(triple.key.substringAfterLast("/"), { getLabel(triple.key) })
        if (propertyLabel != null) {
          val values = result.properties.getOrPut(propertyLabel, { sortedSetOf() })
          triple.value.forEach {
            val entityProperty = convert(it)
            if (entityProperty != null) {
              values.add(entityProperty)
              it.properties.forEach { url, value ->
                propertyLabel = propertyLabelCache.getOrPut(url.substringAfterLast("/"), { getLabel(url) })
                if (propertyLabel != null) {
                  val v = convert(value)
                  if (v != null) entityProperty.moreInfo[propertyLabel!!] = v
                }
              }
            }
          }
        }
      }
      result.properties = result.properties.filter { it.value.isNotEmpty() }.toSortedMap()
    }
    return result
  }

  fun convert(data: TripleObject): EntityProperty? {
    if (data.type == TripleObject.TypeEnum.RESOURCE) {
      val l = getLabel(data.value)
      return EntityProperty(EntityPropertyValue(
          l ?: data.value.substringAfterLast("/").replace('_', ' '),
          data.value, data.value.contains("upload.wikimedia.org")))
    } else {
      if (!LanguageChecker.multiLanguages(data.value))
        return EntityProperty(EntityPropertyValue(data.value))
    }
    return null
  }

  fun convert(data: TypedValue): EntityProperty? {
    if (data.type == TypedValue.TypeEnum.RESOURCE) {
      val l = getLabel(data.value)
      return EntityProperty(EntityPropertyValue(
          l ?: data.value.substringAfterLast("/").replace('_', ' '),
          data.value, data.value.contains("upload.wikimedia.org")))
    } else {
      if (!LanguageChecker.multiLanguages(data.value))
        return EntityProperty(EntityPropertyValue(data.value))
    }
    return null
  }

  fun getEntities(entities: MutableList<String>) = entities.map { getEntityData(it, false) }
}