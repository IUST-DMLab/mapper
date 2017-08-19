package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.mapper.logic.EntityInfoLogic
import ir.ac.iust.dml.kg.mapper.logic.PathUtils
import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.store.entities.MapRule
import ir.ac.iust.dml.kg.mapper.logic.test.TestUtils
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.Module
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1triplesApi
import ir.ac.iust.nlp.jhazm.Stemmer
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RawTripleImporter {
  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var storeProvider: StoreProvider
  @Autowired private lateinit var ontologyLogic: OntologyLogic
  @Autowired private lateinit var entityInfoLogic: EntityInfoLogic
  @Autowired private lateinit var notMappedPropertyHandler: NotMappedPropertyHandler
  @Autowired private lateinit var entityClassImporter: EntityClassImporter
  private var propertyToPredicates = mutableMapOf<String, MutableSet<String>>()
  private var predicatesOfClass = mutableMapOf<String, MutableSet<String>>()
  val tripleApi: V1triplesApi

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    tripleApi = V1triplesApi(client)
  }

  data class SubjectData(var subject: String, var ontologyClass: String? = null, var classDepth: Int = 0)

  fun writeTriples(storeType: StoreType = StoreType.none) {
    val path = PathUtils.getPath("raw.folder.input", "~/raw/triples")
    val maxNumberOfTriples = TestUtils.getMaxTuples()
    val store = storeProvider.getStore(storeType, path)

    val result = PathWalker.getPath(path, Regex(".*\\.json"))
    val startTime = System.currentTimeMillis()
    var tripleNumber = 0

    entityInfoLogic.reload()
    ontologyLogic.reloadTreeCache()
    holder.writeToKS()
    holder.loadFromKS()

    if (propertyToPredicates.isEmpty()) {
      propertyToPredicates = mutableMapOf()
      holder.all().forEach { map ->
        val ontologyClass: String? = map.rules?.filter { it.predicate == URIs.typePrefixed }?.firstOrNull()?.constant?.substringAfterLast(":")
        map.properties?.forEach { property, propertyMapping ->
          propertyMapping.rules.forEach { rule ->
            if (rule.predicate != null) {
              propertyToPredicates.getOrPut(property, { mutableSetOf() }).add(URIs.prefixedToUri(rule.predicate!!)!!)
            }
          }
          if (ontologyClass != null) {
            predicatesOfClass.getOrPut(ontologyClass, { mutableSetOf() }).add(property)
//                ontologyLogic.getTree(ontologyClass)?.split("/")?.forEach {
//                  predicatesOfClass.getOrPut(it, { mutableSetOf() }).add(rule.predicate!!)
//                }
          }
        }
      }
    }

    val newSubjects = mutableSetOf<String>()

    result.forEachIndexed { index, p ->
      ir.ac.iust.dml.kg.raw.triple.RawTripleImporter(p).use { reader ->
        while (reader.hasNext()) {
          val triple = reader.next()
          tripleNumber++
          if (tripleNumber > maxNumberOfTriples) break
          try {
            if (tripleNumber % 1000 == 0)
              logger.warn("triple number is $tripleNumber. $index file is $p. " +
                  "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            val subjectLabel = if (triple.subject.contains("/")) triple.subject.substringAfterLast("/") else triple.subject
            val subjects = tripleApi.search1(null, null, null, null, URIs.variantLabel,
                null, subjectLabel, null, 0, 0).data.map { it.subject }
            val subjectsData = subjects.map { subject ->
              val subjectData = SubjectData(subject)
              val subjectInfoBoxes = entityInfoLogic.resources[subject.substringAfterLast("/").replace('_', ' ')]
              subjectInfoBoxes?.forEach { infobox ->
                val map = holder.getTemplateMapping(infobox).rules?.filter { it.predicate == URIs.typePrefixed }?.firstOrNull()
                if (map != null && map.constant != null) {
                  val ontologyClass = map.constant!!.substringAfterLast(":")
                  val classParents = ontologyLogic.getClassParents(ontologyClass)!!
                  if (classParents.size > subjectData.classDepth) {
                    subjectData.classDepth = classParents.size
                    subjectData.ontologyClass = ontologyClass.substringAfterLast("/")
                  }
                }
              }
              subjectData
            }.sortedByDescending { it.classDepth }

            var subject = subjectsData.filter {
              (it.ontologyClass != null) && (predicatesOfClass[it.ontologyClass!!] ?: mutableSetOf())
                  .contains(triple.predicate)
            }.firstOrNull()?.subject
            if (subject == null) subject = subjectsData.firstOrNull()?.subject
            if (subject == null) {
              subject = URIs.getFkgResourceUri(subjectLabel)
              newSubjects.add(subject)
            }

            val objekt = if (entityInfoLogic.resources.containsKey(triple.`object`))
              URIs.getFkgResourceUri(triple.`object`) else triple.`object`
            val predicate: String
            val stemmedPredicate = Stemmer.i().stem(triple.predicate)
            if (triple.isNeedsMapping) {
              val subjectInfoBoxes = entityInfoLogic.resources[subject.substringAfterLast("/").replace('_', ' ')]
              val defaultProperty = URIs.convertToNotMappedFkgPropertyUri(triple.predicate)!!
              predicate =
                  if (subjectInfoBoxes == null) defaultProperty
                  else {
                    var m = mutableSetOf<MapRule>()
                    subjectInfoBoxes.forEach {
                      val pm = holder.examinePropertyMapping(it, triple.predicate)
                      if (pm != null && pm.rules.isNotEmpty()) {
                        m = pm.rules
                        return@forEach
                      }
                    }
                    if (m.isNotEmpty()) URIs.prefixedToUri(m.iterator().next().predicate!!)!!
                    else {
                      val ped = mutableSetOf<String>()
                      ped.addAll(propertyToPredicates[stemmedPredicate] ?: mutableSetOf())
                      ped.addAll(propertyToPredicates[triple.predicate] ?: mutableSetOf())
                      if (ped.isNotEmpty()) ped.first()
                      else defaultProperty
                    }
                  }
              if (predicate == defaultProperty) notMappedPropertyHandler.addToNotMapped(triple.predicate)
            } else predicate = triple.predicate
            store.save(source = triple.sourceUrl, subject = subject,
                objeck = objekt, property = predicate, version = triple.version,
                extractionTime = triple.extractionTime, module = triple.module, rawText = triple.rawText,
                accuracy = triple.accuracy)
          } catch (th: Throwable) {
            th.printStackTrace()
          }
        }
      }
    }

    newSubjects.forEach { subject ->
      logger.info("new subject detected: $subject")
      entityClassImporter.addResourceAsThing(subject, store, Module.raw_mapper_entity_adder.name)
    }

    store.flush()
    notMappedPropertyHandler.writeNotMappedProperties(storeType, true)
    logger.info("new subjects has been added: ${newSubjects.joinToString("\n")}")
  }
}