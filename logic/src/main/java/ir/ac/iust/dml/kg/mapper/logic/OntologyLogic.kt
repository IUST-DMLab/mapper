package ir.ac.iust.dml.kg.mapper.logic

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.data.ExportedPropertyData
import ir.ac.iust.dml.kg.mapper.logic.data.OntologyClassData
import ir.ac.iust.dml.kg.mapper.logic.data.OntologyPropertyData
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.mapper.logic.utils.TestUtils
import ir.ac.iust.dml.kg.raw.utils.*
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1expertsApi
import ir.ac.iust.dml.kg.services.client.swagger.V1triplesApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TripleData
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Files

@Service
class OntologyLogic {

  private val logger = Logger.getLogger(this.javaClass)!!
  val tripleApi: V1triplesApi
  val expertApi: V1expertsApi
  private val treeCache = mutableMapOf<String, String>()
  // TODO remove tree cache and use tree paretns
  private val treeParents = mutableMapOf<String, List<String>>()
  private val childrenCache = mutableMapOf<String, List<String>>()
  private val traversedTree = mutableListOf<String>()
  @Autowired lateinit var storeProvider: StoreProvider
  private val VERSION = 1

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    tripleApi = V1triplesApi(client)
    expertApi = V1expertsApi(client)
  }

  fun save(store: FkgTripleDao, source: String, subject: String, predicate: String, `object`: String) {
    store.save(source, subject, predicate, `object`, Module.expert.name, VERSION)
  }

  fun importFromDBpedia(storeType: StoreType = StoreType.knowledgeStore) {

    val exportedJson = ConfigReader.getPath("dbpedia.properties.export", "~/.pkg/data/ontology_property.json")
    if (!Files.exists(exportedJson.parent)) Files.createDirectories(exportedJson.parent)
    if (!Files.exists(exportedJson)) {
      throw Exception("There is no file ${exportedJson.toAbsolutePath()} existed.")
    }
    val store = storeProvider.getStore(storeType)
    val gson = Gson()
    val maxWrites = TestUtils.getMaxTuples()
    val type = object : TypeToken<Map<String, ExportedPropertyData>>() {}.type
    var index = 0
    val dbpediaMainPrefix = "http://dbpedia.org/"
    val fkgMainPrefix = URIs.prefixedToUri(URIs.fkgMainPrefix + ":")!!
    val thing = URIs.getFkgOntologyClassUri("Thing")
    try {
      BufferedReader(InputStreamReader(FileInputStream(exportedJson.toFile()), "UTF8")).use { reader ->
        val map: Map<String, ExportedPropertyData> = gson.fromJson(reader, type)
        map.forEach { property, data ->
          if (index >= maxWrites) return@forEach
          index++
          val subject = property.replace(dbpediaMainPrefix, fkgMainPrefix)
          if (index % 1000 == 0) logger.info("writing property $property: $data")
          val source = if (data.wasDerivedFrom == null) property else data.wasDerivedFrom!!
          if (data.label != null) save(store, source, subject, data.label!!, URIs.label)
          if (data.comment != null) save(store, source, subject, data.comment!!, URIs.comment)
          if (data.domain != null) {
            val oldDomains = store.read(subject = subject, predicate = URIs.propertyDomain)
            oldDomains.forEach { store.delete(it.subject!!, it.predicate!!, it.objekt!!) }
            save(store, source, subject, data.domain!!.replace(dbpediaMainPrefix, fkgMainPrefix), URIs.propertyDomain)
          } else save(store, source, subject, thing, URIs.propertyDomain)
          val result = store.read(subject = subject, predicate = URIs.name)
          if (result.isEmpty()) {
            val name = subject.substring(subject.indexOf("/ontology/") + 10)
            store.convertAndSave(source = subject, subject = subject, property = URIs.name, objeck = name,
                module = Module.expert.name, version = VERSION)
          }
          if (data.range != null) save(store, source, subject,
              data.range!!.replace(dbpediaMainPrefix, fkgMainPrefix), URIs.propertyRange)
          if (data.wasDerivedFrom != null) save(store, source, subject, data.wasDerivedFrom!!, URIs.wasDerivedFrom)
          if (data.equivalentProperty != null) save(store, source, subject,
              data.equivalentProperty!!.replace(dbpediaMainPrefix, fkgMainPrefix), URIs.equivalentProperty)

          if (data.type != null) {
            val typeUrl = URIs.prefixedToUri(data.type)!!
            val oldTypes = store.read(subject = subject, predicate = URIs.type)
            oldTypes.forEach { store.delete(it.subject!!, it.predicate!!, it.objekt!!) }
            save(store, source, subject, typeUrl, URIs.type)
          }
          save(store, source, subject, URIs.typeOfAnyProperties, URIs.type)
        }
      }
    } catch (th: Throwable) {
      logger.error(th)
    }

    store.flush()
  }

  fun findCommonRoot(classes: Collection<String>): String? {
    if (treeCache.isEmpty()) reloadTreeCache()
    val tress = mutableListOf<List<String>>()
    classes.forEach {
      val name = if (it.contains("/")) it.substringAfterLast("/") else it
      val parents = treeCache[name]?.split("/")
      if (parents != null && parents.isNotEmpty()) tress.add(parents.asReversed())
    }
    if (tress.isEmpty()) return null
    var index = 0
    val minSize = tress.map { it.size }.min()!!
    val result: String
    while (true) {
      val one = tress[0][index]
      if ((tress.filter { it[index] == one }.size < tress.size)) {
        result = tress[0][index - 1]
        break
      }
      if (index == minSize - 1) {
        result = tress[0][index]
        break
      }
      index++
    }
    return URIs.getFkgOntologyClassUri(result)
  }

  fun reloadTreeCache(): Boolean {
    try {
      var page = 0
      do {
        val classes = getType(null, URIs.typeOfAllClasses, page++, 100)
        classes.data.forEach { classUrl ->
          val name = classUrl.substringAfterLast("/")
          val parents = mutableListOf(classUrl)
          fillParents(classUrl, parents)
          treeCache[name] = parents.map { it.substringAfterLast("/") }.joinToString("/")
          treeParents[name] = parents
          val children = mutableListOf<String>()
          fillChildren(classUrl, children)
          childrenCache[name] = children.map { it.substringAfterLast("/") }
        }
      } while (classes.data.isNotEmpty())
      traversedTree.clear()
      traverseCache("Thing", traversedTree)
      return true
    } catch (th: Throwable) {
      return false
    }
  }

  private fun traverseCache(current: String, list: MutableList<String>) {
    list.add(URIs.getFkgOntologyClassUri(current))
    childrenCache[current]?.forEach { traverseCache(it, list) }
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
    children.addAll(pages.data.map { it.subject }.sortedBy { it })
  }

  fun getTree(ontologyClass: String) = treeCache[ontologyClass]

  fun getClassParents(ontologyClass: String) = treeParents[ontologyClass]

  fun getChildren(ontologyClass: String) = childrenCache[ontologyClass]

  private fun search(subject: String?, predicate: String?, `object`: String?, page: Int, pageSize: Int?,
                     likeSubject: Boolean = false, likePredicate: Boolean = false, likeObject: Boolean = false) =
      tripleApi.search1(null, false, subject, likeSubject, predicate,
          likePredicate, `object`, likeObject, page, pageSize)

  private fun getType(keyword: String?, type: String, page: Int, pageSize: Int, like: Boolean = false): PagedData<String> {
    val result = search(keyword, URIs.type, type, page, pageSize, likeSubject = like)
    val data = result.data.map { it.subject }.toMutableList()
    return PagedData<String>(data, page, pageSize, result.pageCount, result.totalSize)
  }

  private fun subjectsOfPredicate(predicate: String, `object`: String): MutableList<String> {
    val result = mutableListOf<String>()
    val values = search(null, predicate, `object`, 0, 1000)
    values.data.forEach { result.add(it.subject) }
    return result
  }

  private fun objectsOfPredicate(subject: String, predicate: String): MutableList<String> {
    val result = mutableListOf<String>()
    val values = search(subject, predicate, null, 0, 1000)
    values.data.forEach { result.add(it.`object`.value) }
    return result
  }

  private fun objectOfPredicate(subject: String, predicate: String): String? {
    val values = search(subject, predicate, null, 0, 1000)
    return values.data.firstOrNull()?.`object`?.value
  }

  private fun insertAndVote(subject: String?, predicate: String?,
                            objectValue: String,
                            objectType: TypedValueData.TypeEnum = TypedValueData.TypeEnum.RESOURCE): Boolean {
    val tripleData = TripleData()
    tripleData.context = URIs.defaultContext
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

  fun classes(page: Int, pageSize: Int, query: String?, like: Boolean)
      = getType(query, URIs.typeOfAllClasses, page, pageSize, like)

  data class OntologyNode(var url: String, var label: String? = null, var name: String? = null,
                          var children: MutableList<OntologyNode> = mutableListOf<OntologyNode>())

  fun classTree(rootUrl: String?, maxDepth: Int? = null, labelLanguage: String? = null): OntologyNode {
    val root = OntologyNode(rootUrl ?: URIs.getFkgOntologyClassUri("Thing"))
    fillNode(root, labelLanguage, 0, maxDepth ?: 100)
    return root
  }

  fun fillNode(node: OntologyNode, labelLanguage: String?, depth: Int, maxDepth: Int?) {
    if (treeCache.isEmpty()) reloadTreeCache()
    if (labelLanguage != null) node.label = getLabel(node.url, labelLanguage)
    node.name = node.url.substring(node.url.indexOf("/ontology/") + 10)
    if (maxDepth != null && depth == maxDepth) return
    val children = childrenCache[node.name!!] ?: listOf()
    children.forEach {
      val child = OntologyNode(URIs.getFkgOntologyClassUri(it))
      fillNode(child, labelLanguage, depth + 1, maxDepth)
      node.children.add(child)
    }
    node.children.sortBy { it.url }
  }

  fun getLabel(url: String, language: String? = null): String? {
    try {
      return search(url, URIs.label, null, 0, 0).data.filter {
        language == null || it.`object`?.lang == language
      }.firstOrNull()?.`object`?.value
    } catch (th: Throwable) {
      return null
    }
  }

  fun getName(url: String): String? {
    try {
      return search(url, URIs.name, null, 0, 0).data.firstOrNull()?.`object`?.value
    } catch (th: Throwable) {
      return null
    }
  }

  fun properties(page: Int, pageSize: Int, query: String?, type: String?, like: Boolean): PagedData<String> {
    var t = URIs.typeOfAnyProperties
    if (type != null) t = if (!type.contains("://"))
      (if (type.contains(":")) URIs.prefixedToUri(type)!! else URIs.prefixedToUri("owl:" + type)!!)
    else type
    return getType(query, t, page, pageSize, like)
  }

  fun classData(classUrl: String): OntologyClassData {
    val classData = OntologyClassData(url = classUrl)
    val labels = search(classUrl, URIs.label, null, 0, 10)
    labels.data.forEach {
      if (it.`object`.lang == "fa") classData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") classData.enLabel = it.`object`.value
    }

    val variantLabels = search(classUrl, URIs.variantLabel, null, 0, 10)
    variantLabels.data.forEach {
      if (it.`object`.lang == "fa") classData.faVariantLabels.add(it.`object`.value)
      if (it.`object`.lang == "en") classData.enVariantLabels.add(it.`object`.value)
    }

    val comments = search(classUrl, URIs.comment, null, 0, 10)
    comments.data.forEach {
      if (it.`object`.lang == "fa") classData.faComment = it.`object`.value
      if (it.`object`.lang == "en") classData.enComment = it.`object`.value
    }

    classData.name = objectOfPredicate(classUrl, URIs.name)
    classData.subClassOf = objectOfPredicate(classUrl, URIs.subClassOf)
    classData.wasDerivedFrom = objectOfPredicate(classUrl, URIs.wasDerivedFrom)
    classData.equivalentClasses = objectsOfPredicate(classUrl, URIs.equivalentClass)
    classData.disjointWith = objectsOfPredicate(classUrl, URIs.disjointWith)
    val properties = subjectsOfPredicate(URIs.propertyDomain, classUrl)
    properties.forEach {
      classData.properties.add(propertyData(it))
    }

    if (traversedTree.isEmpty()) reloadTreeCache()
    val index = traversedTree.indexOf(classUrl)
    classData.next = if (index > -1 && index < traversedTree.size - 2) traversedTree[index + 1] else null
    classData.previous = if (index > 0) traversedTree[index - 1] else null
    return classData
  }

  private fun remove(subject: String?, predicate: String?, `object`: String?) {
    if (subject == null || predicate == null || `object` == null) return
    logger.info("removing $subject, $predicate, $`object`")
    //TODO we must remove next line after first run of mapping on data
    tripleApi.remove1(subject, predicate, `object`, subject)
    tripleApi.remove1(subject, predicate, `object`, URIs.defaultContext)
  }

  fun saveClass(data: OntologyClassData): OntologyClassData? {
    if (data.url == null) return null

    val oldData = classData(data.url!!)
    if ((oldData.faLabel != null) && (oldData.faLabel != data.faLabel))
      remove(data.url, URIs.label, oldData.faLabel)
    if ((oldData.enLabel != null) && (oldData.enLabel != data.enLabel))
      remove(data.url, URIs.label, oldData.enLabel)
    if ((oldData.faComment != null) && (oldData.faComment != data.faComment))
      remove(data.url, URIs.comment, oldData.faComment)
    if ((oldData.enComment != null) && (oldData.enComment != data.enComment))
      remove(data.url, URIs.comment, oldData.enComment)
    if ((oldData.subClassOf != null) && (oldData.subClassOf != data.subClassOf))
      remove(data.url, URIs.subClassOf, oldData.subClassOf)
    if ((oldData.wasDerivedFrom != null) && (oldData.wasDerivedFrom != data.wasDerivedFrom))
      remove(data.url, URIs.wasDerivedFrom, oldData.wasDerivedFrom)
    if ((oldData.name != null) && (oldData.name != data.name))
      remove(data.url, URIs.name, oldData.name)
    oldData.faVariantLabels.subtract(data.faVariantLabels).forEach { remove(data.url, URIs.variantLabel, it) }
    oldData.enVariantLabels.subtract(data.enVariantLabels).forEach { remove(data.url, URIs.variantLabel, it) }
    oldData.equivalentClasses.subtract(data.equivalentClasses).forEach { remove(data.url, URIs.equivalentClass, it) }
    oldData.disjointWith.subtract(data.disjointWith).forEach { remove(data.url, URIs.disjointWith, it) }
    oldData.properties.subtract(data.properties).forEach { remove(data.url, URIs.propertyDomain, it.url) }

    insertAndVote(data.url, URIs.type, URIs.typeOfAllClasses)
    if (data.faLabel != null) insertAndVote(data.url, URIs.label, data.faLabel!!)
    if (data.enLabel != null) insertAndVote(data.url, URIs.label, data.enLabel!!)
    if (data.faComment != null) insertAndVote(data.url, URIs.comment, data.faComment!!)
    if (data.enComment != null) insertAndVote(data.url, URIs.comment, data.enComment!!)
    if (data.subClassOf != null) insertAndVote(data.url, URIs.subClassOf, data.subClassOf!!)
    if (data.wasDerivedFrom != null) insertAndVote(data.url, URIs.wasDerivedFrom, data.wasDerivedFrom!!)
    if (data.name != null) insertAndVote(data.url, URIs.name, data.name!!)
    data.faVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it) }
    data.enVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it) }
    data.equivalentClasses.forEach { insertAndVote(data.url, URIs.equivalentClass, it) }
    data.disjointWith.forEach { insertAndVote(data.url, URIs.disjointWith, it) }
    data.properties.forEach { insertAndVote(it.url, URIs.propertyDomain, data.url!!) }

    if (data.subClassOf != oldData.subClassOf) {
      // can i load tree?
      if (!reloadTreeCache()) {
        saveClass(oldData)
        return classData(data.url!!)
      }
    }
    return classData(data.url!!)
  }

  fun propertyData(propertyUrl: String): OntologyPropertyData {
    val propertyData = OntologyPropertyData(url = propertyUrl)

    val labels = search(propertyUrl, URIs.label, null, 0, 10)
    labels.data.forEach {
      if (it.`object`.lang == "fa") propertyData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") propertyData.enLabel = it.`object`.value
    }

    val variantLabels = search(propertyUrl, URIs.variantLabel, null, 0, 10)
    variantLabels.data.forEach {
      if (it.`object`.lang == "fa") propertyData.faVariantLabels.add(it.`object`.value)
      if (it.`object`.lang == "en") propertyData.enVariantLabels.add(it.`object`.value)
    }

    propertyData.name = objectOfPredicate(propertyUrl, URIs.name)
    propertyData.wasDerivedFrom = objectOfPredicate(propertyUrl, URIs.wasDerivedFrom)
    propertyData.types.addAll(objectsOfPredicate(propertyUrl, URIs.type))
    propertyData.domains.addAll(objectsOfPredicate(propertyUrl, URIs.propertyDomain))
    propertyData.autoDomains.addAll(objectsOfPredicate(propertyUrl, URIs.propertyAutoDomain))
    propertyData.ranges.addAll(objectsOfPredicate(propertyUrl, URIs.propertyRange))
    propertyData.autoRanges.addAll(objectsOfPredicate(propertyUrl, URIs.propertyAutoRange))
    propertyData.equivalentProperties.addAll(objectsOfPredicate(propertyUrl, URIs.equivalentProperty))

    return propertyData
  }

  fun saveProperty(data: OntologyPropertyData): OntologyPropertyData? {
    if (data.url == null) return null

    val oldData = propertyData(data.url!!)
    if ((oldData.faLabel != null) && (oldData.faLabel != data.faLabel))
      remove(data.url, URIs.label, oldData.faLabel)
    if ((oldData.enLabel != null) && (oldData.enLabel != data.enLabel))
      remove(data.url, URIs.label, oldData.enLabel)
    if ((oldData.name != null) && (oldData.name != data.name))
      remove(data.url, URIs.name, oldData.name)
    if ((oldData.wasDerivedFrom != null) && (oldData.wasDerivedFrom != data.wasDerivedFrom))
      remove(data.url, URIs.wasDerivedFrom, oldData.wasDerivedFrom)
    oldData.faVariantLabels.subtract(data.faVariantLabels).forEach { remove(data.url, URIs.variantLabel, it) }
    oldData.enVariantLabels.subtract(data.enVariantLabels).forEach { remove(data.url, URIs.variantLabel, it) }
    oldData.domains.subtract(data.domains).forEach { remove(data.url, URIs.propertyDomain, it) }
    oldData.ranges.subtract(data.ranges).forEach { remove(data.url, URIs.propertyRange, it) }
    oldData.equivalentProperties.subtract(data.equivalentProperties).forEach { remove(data.url, URIs.equivalentProperty, it) }

    insertAndVote(data.url, URIs.type, URIs.typeOfAnyProperties)
    if (data.name != null) insertAndVote(data.url, URIs.name, data.name!!)
    if (data.wasDerivedFrom != null) insertAndVote(data.url, URIs.wasDerivedFrom, data.wasDerivedFrom!!)
    if (data.faLabel != null) insertAndVote(data.url, URIs.label, data.faLabel!!)
    if (data.enLabel != null) insertAndVote(data.url, URIs.label, data.enLabel!!)
    data.faVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it) }
    data.enVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it) }
    data.types.forEach { insertAndVote(data.url, URIs.type, it) }
    data.domains.forEach { insertAndVote(data.url, URIs.propertyDomain, it) }
    data.ranges.forEach { insertAndVote(data.url, URIs.propertyRange, it) }
    data.equivalentProperties.forEach { insertAndVote(it, URIs.equivalentProperty, data.url!!) }
    return propertyData(data.url!!)
  }

}