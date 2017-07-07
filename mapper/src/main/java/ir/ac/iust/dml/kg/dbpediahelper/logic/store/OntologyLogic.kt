package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.dbpediahelper.logic.store.data.OntologyClassData
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.data.OntologyPropertyData
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1expertsApi
import ir.ac.iust.dml.kg.services.client.swagger.V1triplesApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TripleData
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import org.springframework.stereotype.Service

@Service
class OntologyLogic {

  val tripleApi: V1triplesApi
  val expertApi: V1expertsApi
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
    expertApi = V1expertsApi(client)
  }

  private fun search(like: Boolean, subject: String?, predicate: String?, `object`: String?, page: Int, pageSize: Int?) =
      tripleApi.search1(null, false, subject, false, predicate,
          false, `object`, false, page, pageSize)

  private fun getType(keyword: String?, type: String, page: Int, pageSize: Int): PagedData<String> {
    val result = search(true, keyword, rdfType, type, page, pageSize)
    val data = result.data.map { it.subject }.toMutableList()
    return PagedData<String>(data, page, pageSize, result.pageCount, result.totalSize)
  }

  private fun subjectsOfPredicate(predicate: String, `object`: String): MutableList<String> {
    val result = mutableListOf<String>()
    val values = search(false, null, predicate, `object`, 0, 1000)
    values.data.forEach { result.add(it.subject) }
    return result
  }

  private fun objectsOfPredicate(subject: String, predicate: String): MutableList<String> {
    val result = mutableListOf<String>()
    val values = search(false, subject, predicate, null, 0, 1000)
    values.data.forEach { result.add(it.`object`.value) }
    return result
  }

  private fun objectOfPredicate(subject: String, predicate: String): String? {
    val values = search(false, subject, predicate, null, 0, 1000)
    return values.data.firstOrNull()?.`object`?.value
  }

  private fun insertAndVote(subject: String?, predicate: String?,
                            objectValue: String,
                            objectType: TypedValueData.TypeEnum = TypedValueData.TypeEnum.RESOURCE): Boolean {
    val tripleData = TripleData()
    tripleData.context = subject
    tripleData.subject = subject
    tripleData.predicate = predicate
    tripleData.`object` = TypedValueData()
    tripleData.`object`.lang = LanguageChecker.detectLanguage(objectValue)
    tripleData.`object`.type = objectType
    tripleData.`object`.value = objectValue
    tripleData.module = "ontology"
    tripleData.precession = 1.0
    tripleData.urls = mutableListOf(subject)
    return insertAndVote(tripleData)
  }

  private fun insertAndVote(data: TripleData): Boolean {
    val success = tripleApi.insert3(data)
    if (!success) return false
//    val triple = tripleApi.triple1(data.subject, data.predicate, data.`object`.value, data.context)
//    expertApi.vote1(triple.identifier, data.module, "expert", "accept")
    return true
  }

  fun classes(page: Int, pageSize: Int, keyword: String?) = getType(keyword, owlClass, page, pageSize)

  data class OntologyNode(var url: String, var label: String? = null,
                          var children: MutableList<OntologyNode> = mutableListOf<OntologyNode>())

  fun classTree(rootUrl: String?, maxDepth: Int? = null, label: Boolean = false): OntologyNode {
    val root = OntologyNode(rootUrl ?: PrefixService.getFkgOntologyClassUrl("Thing"))
    fillNode(root, label, 0, maxDepth ?: 100)
    return root
  }

  fun fillNode(node: OntologyNode, label: Boolean, depth: Int, maxDepth: Int?) {
    if (label) node.label = getLabel(node.url)
    if (maxDepth != null && depth == maxDepth) return
    val children = search(false, null, rdfsSubClassOf, node.url, 0, null).data
    children.forEach {
      val child = OntologyNode(it.subject)
      fillNode(child, label, depth + 1, maxDepth)
      node.children.add(child)
    }
  }

  fun getLabel(url: String): String? {
    try {
      return search(false, url, rdfsLabel, null, 0, 1).data.firstOrNull()?.`object`?.value
    } catch (th: Throwable) {
      return null
    }
  }

  fun properties(page: Int, pageSize: Int, keyword: String?) = getType(keyword, owlObjectProperty, page, pageSize)

  fun classData(classUrl: String): OntologyClassData {
    val classData = OntologyClassData(url = classUrl)
    val labels = search(false, classUrl, rdfsLabel, null, 0, 10)
    labels.data.forEach {
      if (it.`object`.lang == "fa") classData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") classData.enLabel = it.`object`.value
    }

    val comments = search(false, classUrl, rdfsComment, null, 0, 10)
    comments.data.forEach {
      if (it.`object`.lang == "fa") classData.faComment = it.`object`.value
      if (it.`object`.lang == "en") classData.enComment = it.`object`.value
    }

    classData.subClassOf = objectOfPredicate(classUrl, rdfsSubClassOf)
    classData.equivalentClasses = objectsOfPredicate(classUrl, owlEqClass)
    classData.disjointWith = objectsOfPredicate(classUrl, owlDisjointWith)
    classData.properties = subjectsOfPredicate(rdfsDomain, classUrl)

    return classData
  }

  fun saveClass(data: OntologyClassData): Boolean {
    if (data.url == null) return false
    insertAndVote(data.url, rdfType, owlClass)
    if (data.faLabel != null) insertAndVote(data.url, rdfsLabel, data.faLabel!!)
    if (data.enLabel != null) insertAndVote(data.url, rdfsLabel, data.enLabel!!)
    if (data.faComment != null) insertAndVote(data.url, rdfsComment, data.faComment!!)
    if (data.enComment != null) insertAndVote(data.url, rdfsComment, data.enComment!!)
    if (data.subClassOf != null) insertAndVote(data.url, rdfsSubClassOf, data.subClassOf!!)
    data.equivalentClasses.forEach { insertAndVote(data.url, owlEqClass, it) }
    data.disjointWith.forEach { insertAndVote(data.url, owlDisjointWith, it) }
    data.properties.forEach { insertAndVote(it, rdfsDomain, data.url!!) }
    return true
  }

  fun saveProperty(data: OntologyPropertyData): Boolean {
    if (data.url == null) return false
    insertAndVote(data.url, rdfType, owlObjectProperty)
    if (data.faLabel != null) insertAndVote(data.url, rdfsLabel, data.faLabel!!)
    if (data.enLabel != null) insertAndVote(data.url, rdfsLabel, data.enLabel!!)
    data.faVariantLabels.forEach { insertAndVote(data.url, fkgVariantLabel, it) }
    data.enVariantLabels.forEach { insertAndVote(data.url, fkgVariantLabel, it) }
    data.types.forEach { insertAndVote(data.url, rdfType, it) }
    data.domains.forEach { insertAndVote(data.url, rdfsDomain, it) }
    data.ranges.forEach { insertAndVote(data.url, rdfsRange, it) }
    data.equivalentProperties.forEach { insertAndVote(it, owlEqProperty, data.url!!) }
    return true
  }

  fun propertyData(propertyUrl: String): OntologyPropertyData {
    val propertyData = OntologyPropertyData(url = propertyUrl)

    val labels = search(false, propertyUrl, rdfsLabel, null, 0, 10)
    labels.data.forEach {
      if (it.`object`.lang == "fa") propertyData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") propertyData.enLabel = it.`object`.value
    }

    val variantLabels = search(false, propertyUrl, fkgVariantLabel, null, 0, 10)
    variantLabels.data.forEach {
      if (it.`object`.lang == "fa") propertyData.faVariantLabels.add(it.`object`.value)
      if (it.`object`.lang == "en") propertyData.enVariantLabels.add(it.`object`.value)
    }

    propertyData.types.addAll(objectsOfPredicate(propertyUrl, rdfType))
    propertyData.domains.addAll(objectsOfPredicate(propertyUrl, rdfsDomain))
    propertyData.ranges.addAll(objectsOfPredicate(propertyUrl, rdfsRange))
    propertyData.equivalentProperties.addAll(objectsOfPredicate(propertyUrl, owlEqProperty))

    return propertyData
  }

}