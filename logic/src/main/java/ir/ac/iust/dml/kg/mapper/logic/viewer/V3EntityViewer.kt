/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.viewer

import com.ghasemkiani.util.icu.PersianCalendar
import com.ibm.icu.text.DateFormat
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.virtuoso.connector.VirtuosoConnector
import ir.ac.iust.dml.kg.virtuoso.connector.data.VirtuosoTripleObject
import ir.ac.iust.dml.kg.virtuoso.connector.data.VirtuosoTripleType
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.*

@Service
@CacheConfig(cacheNames = ["viewer3"])
open class V3EntityViewer {
  private var connector: VirtuosoConnector? = null

  //  private val ontologyApi: V1triplesApi
  private val THING = URIs.getFkgOntologyClassUri("Thing")

  private fun getLabel(url: String): String? {
    if (url.contains("/resource/")) return null
    val u = if (url.contains("/property/")) url.replace("/property/", "/ontology") else url
    var propertyLabel = getFilteredValue(u, URIs.label, LanguageChecker::isPersian)
    if (propertyLabel != null) return propertyLabel
    propertyLabel = getFilteredValue(u, URIs.variantLabel, LanguageChecker::isPersian)
    return propertyLabel
  }

  private fun getFilteredValue(subject: String, predicate: String, filter: (String) -> Boolean): String?
      = connector!!.getTriples(subject, predicate)
      .firstOrNull { filter(it.`object`.value.toString()) }?.`object`?.value?.toString()

  private fun getPropertyLabel(url: String) = PropertyFilter.propertyLabelCache
      .getOrPut(url.substringAfterLast("/"), { getLabel(url) })

  @Cacheable
  open fun getEntityData(url: String, properties: Boolean = true): EntityData {
    if (connector == null) {
      connector = VirtuosoConnector(ConfigReader.getString("virtuoso.graph", URIs.defaultContext))
    }
    val result = EntityData()
    val triples = connector!!.getTriplesOfSubject(url)
    val subjectTriples = mutableMapOf<String, MutableList<VirtuosoTripleObject>>()
    triples.forEach { subjectTriples.getOrPut(it.predicate, { mutableListOf() }).add(it.`object`) }
    val entityDefaultName = url.substringAfterLast("/").replace('_', ' ')
    result.wikiLink = "https://fa.wikipedia.org/wiki/" + entityDefaultName.replace(' ', '_')
    var searched = subjectTriples[URIs.label]
    result.label = if (searched?.isEmpty() != false) entityDefaultName else searched[0].value.toString()
    searched = subjectTriples[URIs.abstract]
    result.abstract = searched?.firstOrNull()?.value.toString()

    searched = subjectTriples[URIs.picture]
    result.image = searched?.firstOrNull()?.value.toString()
    if (properties) {
      searched = subjectTriples[URIs.instanceOf]
      var type = searched?.firstOrNull { it.value != THING }?.value
      if (type == THING) type = null
      result.type = if (type == null) null else getLabel(type.toString())
      subjectTriples.filter { triple ->
        PropertyFilter.filteredPredicates.none { it.matches(triple.key) }
            && triple.value[0].value != "no"
      }.forEach { triple ->
        if (triple.key == URIs.relatedPredicates) {
          for (it in triple.value) {
            val related = connector!!.getTriplesOfSubject(it.value.toString())
            val mp = related.firstOrNull { it.predicate == URIs.mainPredicate } ?: continue
            val mpUrl = mp.`object`.value.toString()
            val propertyLabel = getPropertyLabel(mpUrl)
            if (propertyLabel != null) {
              val mainPredicateValue = related.firstOrNull { it.predicate == mpUrl } ?: continue
              val values = result.properties.getOrPut(propertyLabel, { sortedSetOf() })
              val v = convert(mainPredicateValue.`object`) ?: continue
              values.add(v)
              val otherPredicates = related.filter {
                it.predicate != URIs.type &&
                    it.predicate != URIs.mainPredicate && it.predicate != mpUrl
              }
              otherPredicates.forEach {
                val label = getPropertyLabel(it.predicate)
                if (label != null)
                  v.moreInfo[label] = convert(it.`object`)!!
              }
            }
          }
        } else {
          val propertyLabel = getPropertyLabel(triple.key)
          if (propertyLabel != null) {
            val values = result.properties.getOrPut(propertyLabel, { sortedSetOf() })
            triple.value.forEach {
              val entityProperty = convert(it)
              if (entityProperty != null) {
                values.add(entityProperty)
              }
            }
          }
        }
      }
      result.properties = result.properties.filter { it.value.isNotEmpty() }.toSortedMap()
    }
    return result
  }

  private val faLocale = Locale.forLanguageTag("fa")
  private val tehranZone = com.ibm.icu.util.TimeZone.getTimeZone("Asia/Tehran")
  private val pCal = PersianCalendar.getInstance(tehranZone, faLocale)
  private val formatter = pCal.getDateTimeFormat(DateFormat.FULL, DateFormat.NONE, faLocale)

  private fun convert(data: VirtuosoTripleObject): EntityProperty? {
    val strValue =
        if (data.type == VirtuosoTripleType.DateTime) {
          synchronized(pCal) {
            pCal.timeInMillis = (data.value as javax.xml.datatype.XMLGregorianCalendar).toGregorianCalendar().timeInMillis
            formatter.format(pCal)
          }
        } else data.value.toString()
    if (data.type == VirtuosoTripleType.Resource) {
      val l = getLabel(strValue)
      return EntityProperty(EntityPropertyValue(
          l ?: strValue.substringAfterLast("/").replace('_', ' '),
          strValue, strValue.contains("upload.wikimedia.org")))
    } else {
      if (!LanguageChecker.multiLanguages(strValue))
        return EntityProperty(EntityPropertyValue(strValue))
    }
    return null
  }

  @Cacheable
  open fun getEntities(entities: MutableList<String>) = entities.map { getEntityData(it, false) }
}