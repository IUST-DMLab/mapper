/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.ontology

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.data.*
import ir.ac.iust.dml.kg.mapper.logic.utils.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.utils.TestUtils
import ir.ac.iust.dml.kg.raw.utils.*
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V2ontologyApi
import ir.ac.iust.dml.kg.services.client.swagger.model.Ontology
import ir.ac.iust.dml.kg.services.client.swagger.model.OntologyData
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Files
import kotlin.concurrent.thread

@Service
class OntologyLogic {

  private val logger = Logger.getLogger(this.javaClass)!!
  private val tripleApi: V2ontologyApi
  private val treeCache = mutableMapOf<String, String>()
  // TODO remove tree cache and use tree paretns
  private val treeParents = mutableMapOf<String, List<String>>()
  private val childrenCache = mutableMapOf<String, List<String>>()
  private val traversedTree = mutableListOf<String>()
  @Autowired private lateinit var storeProvider: StoreProvider
  private val VERSION = 1

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    tripleApi = V2ontologyApi(client)
  }

  fun save(store: FkgTripleDao, source: String, subject: String, predicate: String, `object`: String) {
    store.save(source, subject, predicate, `object`, Module.expert.name, VERSION)
  }

  fun importFromDBpedia() {
    val exportedJson = ConfigReader.getPath("dbpedia.properties.export", "~/.pkg/data/ontology_property.json")
    if (!Files.exists(exportedJson.parent)) Files.createDirectories(exportedJson.parent)
    if (!Files.exists(exportedJson)) {
      throw Exception("There is no file ${exportedJson.toAbsolutePath()} existed.")
    }
    val store = storeProvider.getStore(StoreType.ontologyStore)
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
            store.convertAndSave(subject, subject, URIs.name, name, Module.expert.name, VERSION)
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
      val all = tripleApi.search2(null, null, null, null, URIs.subClassOf,
          null, null, null, null, 0, 0)
      val classParents = mutableMapOf<String, MutableSet<String>>()
      val classChildren = mutableMapOf<String, MutableSet<String>>()
      all.data.forEach {
        val p = classParents.getOrPut(it.subject, { mutableSetOf() })
        p.add(it.`object`.value)
        val c = classChildren.getOrPut(it.`object`.value, { mutableSetOf() })
        c.add(it.subject)
      }
      val classes = getType(null, URIs.typeOfAllClasses, 0, 0)
      classes.data.forEach { classUrl ->
        val name = classUrl.substringAfterLast("/")
        val parents = mutableListOf(classUrl)
        fillParents(classUrl, parents, classParents)
        treeCache[name] = parents.map { it.substringAfterLast("/") }.joinToString("/")
        treeParents[name] = parents
        val children = classChildren[classUrl] ?: mutableSetOf()
        childrenCache[name] = children.map { it.substringAfterLast("/") }
      }
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

  private fun fillParents(classUrl: String, parents: MutableList<String>,
                          classParents: MutableMap<String, MutableSet<String>>) {
    val s = classParents[classUrl]
    if (s != null && s.isNotEmpty()) {
      val parentClassUrl = s.first()
      parents.add(parentClassUrl)
      fillParents(parentClassUrl, parents, classParents)
    }
  }

  fun getTree(ontologyClass: String) = treeCache[ontologyClass]

  fun getClassParents(ontologyClass: String) = treeParents[ontologyClass]

  fun getChildren(ontologyClass: String) = childrenCache[ontologyClass]

  private fun search(subject: String?, subjectLike: Boolean,
                     predicate: String?, predicateLike: Boolean,
                     `object`: String?, objectLike: Boolean,
                     page: Int, pageSize: Int?) =
      tripleApi.search2(null, null, subject, subjectLike,
          predicate, predicateLike, `object`, objectLike, null, page, pageSize)

  private fun search(subject: String?, predicate: String?, `object`: String?, page: Int, pageSize: Int?) =
      tripleApi.search2(null, null, subject, false,
          predicate, false, `object`, false, null, page, pageSize)

  private fun getType(keyword: String?, type: String, page: Int, pageSize: Int): PagedData<String> {
    val result =
        if (keyword != null) search(keyword, true, URIs.type, false, type, false, page, pageSize)
        else search(keyword, URIs.type, type, page, pageSize)
    val data = result.data.map { it.subject }.toMutableList()
    return PagedData(data, page, pageSize, result.pageCount, result.totalSize)
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
                            objectType: TypedValueData.TypeEnum): Boolean {
    if (objectValue.isBlank()) return false
    val tripleData = OntologyData()
    tripleData.context = URIs.defaultContext
    tripleData.subject = subject
    tripleData.predicate = predicate
    tripleData.`object` = TypedValueData()
    tripleData.`object`.lang = LanguageChecker.detectLanguage(objectValue)
    tripleData.`object`.type = objectType
    tripleData.`object`.value = objectValue
    tripleData.approved = true
    return insertAndVote(tripleData)
  }

  private fun insertAndVote(data: OntologyData): Boolean {
    tripleApi.insert6(data) ?: return false
    //    val triple = tripleApi.triple1(data.subject, data.predicate, data.`object`.value, data.context)
//    expertApi.vote1(triple.identifier, data.module, "expert", "accept")
    return true
  }

  fun classes(page: Int, pageSize: Int, query: String?)
      = getType(query, URIs.typeOfAllClasses, page, pageSize)

  data class OntologyNode(var url: String, var label: String? = null, var name: String? = null,
                          var children: MutableList<OntologyNode> = mutableListOf<OntologyNode>())

  fun classTree(rootUrl: String?, maxDepth: Int? = null, labelLanguage: String? = null): OntologyNode {
    val root = OntologyNode(rootUrl ?: URIs.getFkgOntologyClassUri("Thing"))
    fillNode(root, labelLanguage, 0, maxDepth ?: 100)
    return root
  }

  private fun fillNode(node: OntologyNode, labelLanguage: String?, depth: Int, maxDepth: Int?) {
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
    try {
      node.children.sortBy { it.url }
    } catch (th: Throwable) {
      th.printStackTrace()
    }
  }

  private fun getLabel(url: String, language: String? = null): String? {
    return try {
      search(url, URIs.label, null, 0, 0).data.filter {
        language == null || it.`object`?.lang == language
      }.firstOrNull()?.`object`?.value
    } catch (th: Throwable) {
      null
    }
  }

  fun properties(page: Int, pageSize: Int, query: String?, type: String?): PagedData<String> {
    var t = URIs.typeOfAnyProperties
    if (type != null) t = if (!type.contains("://"))
      (if (type.contains(":")) URIs.prefixedToUri(type)!! else URIs.prefixedToUri("owl:" + type)!!)
    else type
    return getType(query, t, page, pageSize)
  }

  fun ontologyPredicates(query: String): List<String> {
    val result = getType(query, URIs.typeOfAnyProperties, 0, 200)
    return result.data.filter { !it.contains("/property/") }
  }

  @Deprecated("Old service for backward compatibility")
  fun getNode(name: String, old: FkgClassData? = null): FkgClassData {
    val classData = classData(URIs.getFkgOntologyClassUri(name), false)
    val result = old ?: FkgClassData(ontologyClass = classData.url)
    result.parentOntologyClass = classData.subClassOf
    result.approved = true
    result.comment = classData.faComment
    result.note = classData.enComment
    result.enLabel = classData.enLabel
    result.faLabel = classData.faLabel
    result.faOtherLabels = classData.faVariantLabels.joinToString(", ")
    return result
  }

  @Deprecated("Old service for backward compatibility")
  fun search(page: Int, pageSize: Int, like: Boolean, name: String?, parent: String?): PagedData<FkgClassData> {
    if (treeParents.isEmpty()) reloadTreeCache()
    val nameLower = name?.toLowerCase()
    val parentAddress = if (parent != null) URIs.getFkgOntologyClassUri(parent) else null
    val searched = treeParents.filter {
      var matched = true
      if (nameLower != null)
        matched = if (like) it.key.toLowerCase().contains(nameLower)
        else it.key.toLowerCase() == nameLower
      if (matched && parentAddress != null)
        matched = it.value.contains(parentAddress)
      matched
    }.map { FkgClassData(ontologyClass = it.key) }
    val result = PageUtils.asPages(page, pageSize, searched)
    result.data.forEach {
      getNode(it.ontologyClass!!, it)
    }
    return result
  }

  fun classData(classUrl: String, property: Boolean = true): OntologyClassData {
    val classData = OntologyClassData(url = classUrl)

    val triples = search(classUrl, null, null, 0, 0).data!!

    val labels = filterPredicates(triples, URIs.label)
    labels.forEach {
      if (it.`object`.lang == "fa") classData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") classData.enLabel = it.`object`.value
    }

    val variantLabels = filterPredicates(triples, URIs.variantLabel)
    variantLabels.forEach {
      if (it.`object`.lang == "fa") classData.faVariantLabels.add(it.`object`.value)
      if (it.`object`.lang == "en") classData.enVariantLabels.add(it.`object`.value)
    }

    val comments = filterPredicates(triples, URIs.comment)
    comments.forEach {
      if (it.`object`.lang == "fa") classData.faComment = it.`object`.value
      if (it.`object`.lang == "en") classData.enComment = it.`object`.value
    }

    classData.name = filterPredicate(triples, URIs.name)
    classData.subClassOf = filterPredicate(triples, URIs.subClassOf)
    classData.wasDerivedFrom = filterPredicate(triples, URIs.wasDerivedFrom)
    classData.equivalentClasses = filterPredicateValues(triples, URIs.equivalentClass)
    classData.disjointWith = filterPredicateValues(triples, URIs.disjointWith)

    if (property) {
      val properties = subjectsOfPredicate(URIs.propertyDomain, classUrl)
      properties.forEach {
        classData.properties.add(propertyData(it))
      }
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
    tripleApi.remove2(subject, predicate, `object`, subject)
    tripleApi.remove2(subject, predicate, `object`, URIs.defaultContext)
  }

  fun removeClass(classUrl: String): Boolean {
    val subClasses = search(null, URIs.subClassOf, classUrl, 0, 0)
    if (subClasses.data.isNotEmpty()) return false
    val triples = search(classUrl, null, null, 0, 0)
    triples.data.forEach {
      remove(it.subject!!, it.predicate!!, it.`object`.value)
    }
    thread(true) { reloadTreeCache() }
    return true
  }

  fun removePropertyFromClass(propertyUrl: String, classUrl: String): Boolean {
    return try {
      remove(propertyUrl, URIs.propertyDomain, classUrl)
      true
    } catch (th: Throwable) {
      false
    }
  }

  fun removePropertyCompletely(propertyUrl: String): Boolean {
    return try {
      var triples = search(propertyUrl, null, null, 0, 0)
      triples.data.forEach { remove(it.subject!!, it.predicate!!, it.`object`.value) }
      triples = search(null, propertyUrl, null, 0, 0)
      triples.data.forEach { remove(it.subject!!, it.predicate!!, it.`object`.value) }
      triples = search(null, null, propertyUrl, 0, 0)
      triples.data.forEach { remove(it.subject!!, it.predicate!!, it.`object`.value) }
      true
    } catch (th: Throwable) {
      false
    }
  }

  fun saveClass(data: OntologyClassData): OntologyClassData? {
    if (data.url == null) return null

    val oldData = classData(data.url!!)
    val labels = tripleApi.search2(null, null, data.url, null,
        URIs.label, null, null, null, null, 0, 0)
    labels.data.forEach { remove(data.url, URIs.label, it.`object`.value) }
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

    insertAndVote(data.url, URIs.type, URIs.typeOfAllClasses, TypedValueData.TypeEnum.RESOURCE)
    if (data.faLabel != null) insertAndVote(data.url, URIs.label, data.faLabel!!, TypedValueData.TypeEnum.STRING)
    if (data.enLabel != null) insertAndVote(data.url, URIs.label, data.enLabel!!, TypedValueData.TypeEnum.STRING)
    if (data.faComment != null) insertAndVote(data.url, URIs.comment, data.faComment!!, TypedValueData.TypeEnum.STRING)
    if (data.enComment != null) insertAndVote(data.url, URIs.comment, data.enComment!!, TypedValueData.TypeEnum.STRING)
    if (data.subClassOf != null) insertAndVote(data.url, URIs.subClassOf, data.subClassOf!!, TypedValueData.TypeEnum.RESOURCE)
    if (data.wasDerivedFrom != null) insertAndVote(data.url, URIs.wasDerivedFrom, data.wasDerivedFrom!!, TypedValueData.TypeEnum.RESOURCE)
    if (data.name != null) insertAndVote(data.url, URIs.name, data.name!!, TypedValueData.TypeEnum.STRING)
    data.faVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it, TypedValueData.TypeEnum.STRING) }
    data.enVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it, TypedValueData.TypeEnum.STRING) }
    data.equivalentClasses.forEach { insertAndVote(data.url, URIs.equivalentClass, it, TypedValueData.TypeEnum.RESOURCE) }
    data.disjointWith.forEach { insertAndVote(data.url, URIs.disjointWith, it, TypedValueData.TypeEnum.RESOURCE) }
    data.properties.forEach { insertAndVote(it.url, URIs.propertyDomain, data.url!!, TypedValueData.TypeEnum.RESOURCE) }

    if (data.subClassOf != oldData.subClassOf) {
      // can i load tree?
      if (!reloadTreeCache()) {
        saveClass(oldData)
        return classData(data.url!!)
      }
    }
    thread(true) { reloadTreeCache() }
    return classData(data.url!!)
  }

  fun filterPredicates(triples: List<Ontology>, predicate: String) = triples.filter { it.predicate == predicate }.toMutableList()
  fun filterPredicateValues(triples: List<Ontology>, predicate: String) = triples.filter { it.predicate == predicate }.map { it.`object`.value }.toMutableList()
  fun filterPredicate(triples: List<Ontology>, predicate: String) = triples.filter { it.predicate == predicate }.firstOrNull()?.`object`?.value

  fun propertyData(propertyUrl: String): OntologyPropertyData {
    val propertyData = OntologyPropertyData(url = propertyUrl)

    val triples = search(propertyUrl, null, null, 0, 0).data!!

    val labels = filterPredicates(triples, URIs.label)
    labels.forEach {
      if (it.`object`.lang == "fa") propertyData.faLabel = it.`object`.value
      if (it.`object`.lang == "en") propertyData.enLabel = it.`object`.value
    }

    val variantLabels = filterPredicates(triples, URIs.variantLabel)
    variantLabels.forEach {
      if (it.`object`.lang == "fa") propertyData.faVariantLabels.add(it.`object`.value)
      if (it.`object`.lang == "en") propertyData.enVariantLabels.add(it.`object`.value)
    }

    propertyData.name = filterPredicate(triples, URIs.name)
    propertyData.wasDerivedFrom = filterPredicate(triples, URIs.wasDerivedFrom)
    propertyData.types.addAll(filterPredicateValues(triples, URIs.type))
    propertyData.domains.addAll(filterPredicateValues(triples, URIs.propertyDomain))
    propertyData.autoDomains.addAll(filterPredicateValues(triples, URIs.propertyAutoDomain))
    propertyData.ranges.addAll(filterPredicateValues(triples, URIs.propertyRange))
    propertyData.autoRanges.addAll(filterPredicateValues(triples, URIs.propertyAutoRange))
    propertyData.equivalentProperties.addAll(filterPredicateValues(triples, URIs.equivalentProperty))

    return propertyData
  }

  fun saveProperty(data: OntologyPropertyData): OntologyPropertyData? {
    if (data.url == null) return null
    val oldData = propertyData(data.url!!)
    val labels = tripleApi.search2(null, null, data.url, null,
        URIs.label, null, null, null, null, 0, 0)
    labels.data.forEach { remove(data.url, URIs.label, it.`object`.value) }
    if ((oldData.name != null) && (oldData.name != data.name))
      remove(data.url, URIs.name, oldData.name)
    if ((oldData.wasDerivedFrom != null) && (oldData.wasDerivedFrom != data.wasDerivedFrom))
      remove(data.url, URIs.wasDerivedFrom, oldData.wasDerivedFrom)
    oldData.faVariantLabels.subtract(data.faVariantLabels).forEach { remove(data.url, URIs.variantLabel, it) }
    oldData.enVariantLabels.subtract(data.enVariantLabels).forEach { remove(data.url, URIs.variantLabel, it) }
    oldData.domains.subtract(data.domains).forEach { remove(data.url, URIs.propertyDomain, it) }
    oldData.ranges.subtract(data.ranges).forEach { remove(data.url, URIs.propertyRange, it) }
    oldData.equivalentProperties.subtract(data.equivalentProperties).forEach { remove(data.url, URIs.equivalentProperty, it) }

    insertAndVote(data.url, URIs.type, URIs.typeOfAnyProperties, TypedValueData.TypeEnum.RESOURCE)
    if (data.name != null) insertAndVote(data.url, URIs.name, data.name!!, TypedValueData.TypeEnum.STRING)
    if (data.wasDerivedFrom != null) insertAndVote(data.url, URIs.wasDerivedFrom, data.wasDerivedFrom!!, TypedValueData.TypeEnum.RESOURCE)
    if (data.faLabel != null) insertAndVote(data.url, URIs.label, data.faLabel!!, TypedValueData.TypeEnum.STRING)
    if (data.enLabel != null) insertAndVote(data.url, URIs.label, data.enLabel!!, TypedValueData.TypeEnum.STRING)
    data.faVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it, TypedValueData.TypeEnum.STRING) }
    data.enVariantLabels.forEach { insertAndVote(data.url, URIs.variantLabel, it, TypedValueData.TypeEnum.STRING) }
    data.types.forEach { insertAndVote(data.url, URIs.type, it, TypedValueData.TypeEnum.RESOURCE) }
    data.domains.forEach { insertAndVote(data.url, URIs.propertyDomain, it, TypedValueData.TypeEnum.RESOURCE) }
    data.ranges.forEach { insertAndVote(data.url, URIs.propertyRange, it, TypedValueData.TypeEnum.RESOURCE) }
    data.equivalentProperties.forEach { insertAndVote(it, URIs.equivalentProperty, data.url!!, TypedValueData.TypeEnum.RESOURCE) }
    return propertyData(data.url!!)
  }

}