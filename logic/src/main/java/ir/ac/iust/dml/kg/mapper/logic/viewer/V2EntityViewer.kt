/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.viewer

import com.ghasemkiani.util.icu.PersianCalendar
import com.ibm.icu.text.DateFormat
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V2ontologyApi
import ir.ac.iust.dml.kg.services.client.swagger.V2subjectsApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TripleObject
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValue
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.*

@Service
@CacheConfig(cacheNames = ["viewer2"])
open class V2EntityViewer {
  private val tripleApi: V2subjectsApi
  private val ontologyApi: V2ontologyApi
  //  private val ontologyApi: V1triplesApi
  private val THING = URIs.getFkgOntologyClassUri("Thing")

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    tripleApi = V2subjectsApi(client)
    ontologyApi = V2ontologyApi(client)
//    ontologyApi = V1triplesApi(client)
  }

  private fun ontologySearch(subject: String? = null, predicate: String? = null, `object`: String? = null, one: Boolean)
      = ontologyApi.search2(null, null, subject, false, predicate, false,
      `object`, false, null, 0, if (one) 1 else 0)
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

  @Cacheable
  open fun getEntityData(url: String, properties: Boolean = true): EntityData {
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
        PropertyFilter.filteredPredicates.none { it.matches(triple.key) }
            && triple.value[0].value != "no"
      }.forEach { triple ->
        var propertyLabel = PropertyFilter.propertyLabelCache.getOrPut(triple.key.substringAfterLast("/"),
            { getLabel(triple.key) })
        if (propertyLabel != null) {
          val values = result.properties.getOrPut(propertyLabel, { sortedSetOf() })
          triple.value.forEach {
            val entityProperty = convert(it)
            if (entityProperty != null) {
              values.add(entityProperty)
              it.properties.forEach { url, value ->
                propertyLabel = PropertyFilter.propertyLabelCache.
                    getOrPut(url.substringAfterLast("/"), { getLabel(url) })
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

  private fun convert(data: TripleObject): EntityProperty? {
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

  private val faLocale = Locale.forLanguageTag("fa")
  private val tehranZone = com.ibm.icu.util.TimeZone.getTimeZone("Asia/Tehran")
  private val pCal = PersianCalendar.getInstance(tehranZone, faLocale)
  private val formatter = pCal.getDateTimeFormat(DateFormat.FULL, DateFormat.NONE, faLocale)

  private fun convert(data: TypedValue): EntityProperty? {
    if (data.type == TypedValue.TypeEnum.RESOURCE) {
      val l = getLabel(data.value)
      return EntityProperty(EntityPropertyValue(
          l ?: data.value.substringAfterLast("/").replace('_', ' '),
          data.value, data.value.contains("upload.wikimedia.org")))
    } else if (data.type == TypedValue.TypeEnum.DATE) {
      synchronized(pCal) {
        println("date here: " + data.value)
        pCal.timeInMillis = data.value.toLong()
        return EntityProperty(EntityPropertyValue(formatter.format(pCal)))
      }
    } else {
      if (!LanguageChecker.multiLanguages(data.value))
        return EntityProperty(EntityPropertyValue(data.value))
    }
    return null
  }

  @Cacheable
  open fun getEntities(entities: MutableList<String>) = entities.map { getEntityData(it, false) }
}