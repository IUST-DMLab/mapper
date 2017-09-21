package ir.ac.iust.dml.kg.mapper.logic

import ir.ac.iust.dml.kg.mapper.logic.data.MapRule
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.mapper.logic.mapping.KSMappingHolder
import ir.ac.iust.dml.kg.mapper.logic.ontology.EntityClassImporter
import ir.ac.iust.dml.kg.mapper.logic.ontology.NotMappedPropertyHandler
import ir.ac.iust.dml.kg.mapper.logic.ontology.OntologyLogic
import ir.ac.iust.dml.kg.mapper.logic.utils.PathUtils
import ir.ac.iust.dml.kg.mapper.logic.utils.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.utils.TestUtils
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
  private val tripleApi: V1triplesApi

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
          }
        }
      }
    }

    val newSubjects = mutableSetOf<String>()
    val VERSION = 1
    val informer = ProgressInformer(result.size + 2)

    result.forEachIndexed { index, p ->
      try {
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
                  if (map?.constant != null) {
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
              store.save(triple.sourceUrl, subject, predicate, objekt, triple.module!!, VERSION,
                  triple.rawText, triple.accuracy, triple.extractionTime)
            } catch (th: Throwable) {
              th.printStackTrace()
            }
          }
        }
      } catch (th: Throwable) {
        logger.error(th)
      }
      informer.stepDone(index + 1)
    }

    newSubjects.forEach { subject ->
      logger.info("new subject detected: $subject")
      entityClassImporter.addResourceAsThing(subject, store, Module.raw_mapper_entity_adder.name, VERSION)
    }

    store.flush()
    informer.stepDone(result.size + 1)
    notMappedPropertyHandler.writeNotMappedProperties("raw", VERSION, true)
    logger.info("new subjects has been added: ${newSubjects.joinToString("\n")}")
    informer.done()
  }
}