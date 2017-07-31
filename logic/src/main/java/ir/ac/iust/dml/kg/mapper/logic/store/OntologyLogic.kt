package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.mapper.logic.store.data.OntologyClassData
import ir.ac.iust.dml.kg.mapper.logic.store.data.OntologyPropertyData
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.URIs
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
  private val treeCache = mutableMapOf<String, String>()
  private val childrenCache = mutableMapOf<String, List<String>>()

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    tripleApi = V1triplesApi(client)
    expertApi = V1expertsApi(client)
  }

  fun reloadTreeCache() {
    var page = 0
    do {
      val classes = getType(null, URIs.typeOfAllClasses, page++, 100)
      classes.data.forEach { classUrl ->
        val name = classUrl.substringAfterLast("/")
        val parents = mutableListOf<String>()
        fillParents(classUrl, parents)
        treeCache[name] = parents.map { it.substringAfterLast("/") }.joinToString("/")
        val children = mutableListOf<String>()
        fillChildren(classUrl, children)
        childrenCache[name] = children.map { it.substringAfterLast("/") }
      }
    } while (classes.data.isNotEmpty())
  }

  private fun fillParents(classUrl: String, parents: MutableList<String>) {
    val pages = tripleApi.search1(null, false, classUrl, false,
        URIs.subClassOf, false, null, false, 0, 1)
    if (pages.data.isNotEmpty()) {
      val parentClassUrl = pages.data.first().`object`.value
      parents.add(parentClassUrl)
      fillParents(parentClassUrl, parents)
    }
  }

  private fun fillChildren(classUrl: String, children: MutableList<String>) {
    val pages = tripleApi.search1(null, false, null, false,
        URIs.subClassOf, false, classUrl, false, 0, 10000)
    children.addAll(pages.data.map { it.subject })
  }

  fun getTree(ontologyClass: String) = treeCache[ontologyClass]

  fun getChildren(ontologyClass: String) = childrenCache[ontologyClass]

  private fun search(like: Boolean, subject: String?, predicate: String?, `object`: String?, page: Int, pageSize: Int?) =
      tripleApi.search1(null, like, subject, like, predicate,
          like, `object`, like, page, pageSize)

  private fun getType(keyword: String?, type: String, page: Int, pageSize: Int): PagedData<String> {
    val result = search(true, keyword, URIs.type, type, page, pageSize)
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

  fun classes(page: Int, pageSize: Int, keyword: String?) = getType(keyword, URIs.typeOfAllClasses, page, pageSize)

  data class OntologyNode(var url: String, var label: String? = null,
                          var children: MutableList<OntologyNode> = mutableListOf<OntologyNode>())

  fun classTree(rootUrl: String?, maxDepth: Int? = null, label: Boolean = false): OntologyNode {
    val root = OntologyNode(rootUrl ?: URIs.getFkgOntologyClassUri("Thing"))
    fillNode(root, label, 0, maxDepth ?: 100)
    return root
  }

  fun fillNode(node: OntologyNode, label: Boolean, depth: Int, maxDepth: Int?) {
    if (label) node.label = getLabel(node.url)
    if (maxDepth != null && depth == maxDepth) return
    val children = search(false, null, URIs.subClassOf, node.url, 0, null).data
    children.forEach {
      val child = OntologyNode(it.subject)
      fillNode(child, label, depth + 1, maxDepth)
      node.children.add(child)
    }
  }

  fun getLabel(url: String): String? {
    try {
      return search(false, url, URIs.label, null, 0, 1).data.firstOrNull()?.`object`?.value
    } catch (th: Throwable) {
      return null
    }
  }

  fun properties(page: Int, pageSize: Int, keyword: String?)
      = getType(keyword, URIs.typeOfAllProperties, page, pageSize)

  fun classData(classUrl: String): OntologyClassData {
    val classData = OntologyClassData(url = classUrl)
    val labels = search(false, classUrl, URIs.label, null, 0, 10)
    labels.data.forEach {
      if (it.`object`.lang == "fa") classData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") classData.enLabel = it.`object`.value
    }

    val comments = search(false, classUrl, URIs.comment, null, 0, 10)
    comments.data.forEach {
      if (it.`object`.lang == "fa") classData.faComment = it.`object`.value
      if (it.`object`.lang == "en") classData.enComment = it.`object`.value
    }

    classData.subClassOf = objectOfPredicate(classUrl, URIs.subClassOf)
    classData.equivalentClasses = objectsOfPredicate(classUrl, URIs.equivalentClass)
    classData.disjointWith = objectsOfPredicate(classUrl, URIs.disjointWith)
    val properties = subjectsOfPredicate(URIs.propertyDomain, classUrl)
    properties.forEach {
      classData.properties.add(propertyData(it))
    }

    return classData
  }

  fun saveClass(data: OntologyClassData): Boolean {
    if (data.url == null) return false
    insertAndVote(data.url, URIs.type, URIs.typeOfAllClasses)
    if (data.faLabel != null) insertAndVote(data.url, URIs.label, data.faLabel!!)
    if (data.enLabel != null) insertAndVote(data.url, URIs.label, data.enLabel!!)
    if (data.faComment != null) insertAndVote(data.url, URIs.comment, data.faComment!!)
    if (data.enComment != null) insertAndVote(data.url, URIs.comment, data.enComment!!)
    if (data.subClassOf != null) insertAndVote(data.url, URIs.subClassOf, data.subClassOf!!)
    data.equivalentClasses.forEach { insertAndVote(data.url, URIs.equivalentClass, it) }
    data.disjointWith.forEach { insertAndVote(data.url, URIs.disjointWith, it) }
    data.properties.forEach { insertAndVote(it.url, URIs.propertyDomain, data.url!!) }
    return true
  }

  fun saveProperty(data: OntologyPropertyData): Boolean {
    if (data.url == null) return false
    insertAndVote(data.url, URIs.type, URIs.typeOfAllProperties)
    if (data.faLabel != null) insertAndVote(data.url, URIs.label, data.faLabel!!)
    if (data.enLabel != null) insertAndVote(data.url, URIs.label, data.enLabel!!)
    data.faVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it) }
    data.enVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it) }
    data.types.forEach { insertAndVote(data.url, URIs.type, it) }
    data.domains.forEach { insertAndVote(data.url, URIs.propertyDomain, it) }
    data.ranges.forEach { insertAndVote(data.url, URIs.propertyRange, it) }
    data.equivalentProperties.forEach { insertAndVote(it, URIs.equivalentProperty, data.url!!) }
    return true
  }

  fun propertyData(propertyUrl: String): OntologyPropertyData {
    val propertyData = OntologyPropertyData(url = propertyUrl)

    val labels = search(false, propertyUrl, URIs.label, null, 0, 10)
    labels.data.forEach {
      if (it.`object`.lang == "fa") propertyData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") propertyData.enLabel = it.`object`.value
    }

    val variantLabels = search(false, propertyUrl, URIs.variantLabel, null, 0, 10)
    variantLabels.data.forEach {
      if (it.`object`.lang == "fa") propertyData.faVariantLabels.add(it.`object`.value)
      if (it.`object`.lang == "en") propertyData.enVariantLabels.add(it.`object`.value)
    }

    propertyData.types.addAll(objectsOfPredicate(propertyUrl, URIs.type))
    propertyData.domains.addAll(objectsOfPredicate(propertyUrl, URIs.propertyDomain))
    propertyData.ranges.addAll(objectsOfPredicate(propertyUrl, URIs.propertyRange))
    propertyData.equivalentProperties.addAll(objectsOfPredicate(propertyUrl, URIs.equivalentProperty))

    return propertyData
  }

}