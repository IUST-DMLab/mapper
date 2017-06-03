package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.dbpediahelper.logic.store.data.OntologyClassData
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.data.OntologyPropertyData
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1triplesApi
import org.springframework.stereotype.Service

@Service
class OntologyLogic {

  val tripleApi: V1triplesApi
  val rdfType = PrefixService.prefixToUri(PrefixService.TYPE_URL)!!
  val rdfsLabel = PrefixService.prefixToUri(PrefixService.LABEL_URL)!!
  val fkgVariantLabel = PrefixService.prefixToUri(PrefixService.VARIANT_LABEL_URL)!!
  val owlClass = PrefixService.prefixToUri(PrefixService.TYPE_OF_ALL_CLASSES)!!
  val owlObjectProperty = PrefixService.prefixToUri(PrefixService.TYPE_OF_ALL_PROPERTIES)!!
  val rdfsDomain = PrefixService.prefixToUri(PrefixService.PROPERTY_DOMAIN_URL)!!
  val rdfsRange = PrefixService.prefixToUri(PrefixService.PROPERTY_RANGE_URL)!!
  val owlEqClass = PrefixService.prefixToUri(PrefixService.EQUIVALENT_CLASS_URL)!!
  val owlEqProperty = PrefixService.prefixToUri(PrefixService.EQUIVALENT_PROPERTY_URL)!!
  val rdfsSubClassOf = PrefixService.prefixToUri(PrefixService.SUB_CLASS_OF)!!
  val rdfsComment = PrefixService.prefixToUri(PrefixService.COMMENT_URL)!!
  val owlDisjointWith = PrefixService.prefixToUri(PrefixService.DISJOINT_WITH_URL)!!

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    tripleApi = V1triplesApi(client)
  }

  fun classes(page: Int, pageSize: Int, keyword: String?) = getType(keyword, owlClass, page, pageSize)

  fun properties(page: Int, pageSize: Int, keyword: String?) = getType(keyword, owlObjectProperty, page, pageSize)

  private fun getType(keyword: String?, type: String, page: Int, pageSize: Int): PagedData<String> {
    val result = tripleApi.search1(null, keyword, rdfType, type, page, pageSize)
    val data = result.data.map { it.subject }.toMutableList()
    return PagedData<String>(data, page, pageSize, result.pageCount, result.totalSize)
  }

  private fun predicateValues(subject: String, predicate: String): MutableList<String> {
    val result = mutableListOf<String>()
    val values = tripleApi.search1(null, subject, predicate, null, 0, 1000)
    values.data.forEach { result.add(it.`object`.value) }
    return result
  }

  private fun predicateValue(subject: String, predicate: String): String? {
    val values = tripleApi.search1(null, subject, predicate, null, 0, 1000)
    return values.data.firstOrNull()?.`object`?.value
  }

  fun classData(classUrl: String): OntologyClassData {
    val classData = OntologyClassData()
    val labels = tripleApi.search1(null, classUrl, rdfsLabel, null, 0, 10)
    labels.data.forEach {
      if (it.`object`.lang == "fa") classData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") classData.enLabel = it.`object`.value
    }

    val comments = tripleApi.search1(null, classUrl, rdfsComment, null, 0, 10)
    comments.data.forEach {
      if (it.`object`.lang == "fa") classData.faComment = it.`object`.value
      if (it.`object`.lang == "en") classData.enComment = it.`object`.value
    }

    classData.subClassOf = predicateValue(classUrl, rdfsSubClassOf)
    classData.equivalentClasses = predicateValues(classUrl, owlEqClass)
    classData.disjointWith = predicateValues(classUrl, owlDisjointWith)

    return classData
  }

  fun propertyData(propertyUrl: String): OntologyPropertyData {
    val propertyData = OntologyPropertyData()

    val labels = tripleApi.search1(null, propertyUrl, rdfsLabel, null, 0, 10)
    labels.data.forEach {
      if (it.`object`.lang == "fa") propertyData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") propertyData.enLabel = it.`object`.value
    }

    val variantLabels = tripleApi.search1(null, propertyUrl, fkgVariantLabel, null, 0, 10)
    variantLabels.data.forEach {
      if (it.`object`.lang == "fa") propertyData.faVariantLabels.add(it.`object`.value)
      if (it.`object`.lang == "en") propertyData.enVariantLabels.add(it.`object`.value)
    }

    propertyData.types.addAll(predicateValues(propertyUrl, rdfType))
    propertyData.domains.addAll(predicateValues(propertyUrl, rdfsDomain))
    propertyData.ranges.addAll(predicateValues(propertyUrl, rdfsRange))
    propertyData.equivalentProperties.addAll(predicateValues(propertyUrl, owlEqProperty))

    return propertyData
  }

}