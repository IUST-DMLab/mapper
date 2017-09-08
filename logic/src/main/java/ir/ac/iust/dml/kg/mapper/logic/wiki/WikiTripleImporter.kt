package ir.ac.iust.dml.kg.mapper.logic.wiki

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.knowledge.core.ValueType
import ir.ac.iust.dml.kg.mapper.logic.data.InfoBoxAndCount
import ir.ac.iust.dml.kg.mapper.logic.data.MapRule
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.mapper.logic.mapping.KSMappingHolder
import ir.ac.iust.dml.kg.mapper.logic.mapping.TransformService
import ir.ac.iust.dml.kg.mapper.logic.ontology.EntityClassImporter
import ir.ac.iust.dml.kg.mapper.logic.ontology.NotMappedPropertyHandler
import ir.ac.iust.dml.kg.mapper.logic.ontology.OntologyLogic
import ir.ac.iust.dml.kg.mapper.logic.utils.PathUtils
import ir.ac.iust.dml.kg.mapper.logic.utils.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.utils.TestUtils
import ir.ac.iust.dml.kg.raw.utils.*
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

@Service
class WikiTripleImporter {

  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var ontologyLogic: OntologyLogic
  @Autowired private lateinit var storeProvider: StoreProvider
  @Autowired private lateinit var entityClassImporter: EntityClassImporter
  @Autowired private lateinit var notMappedPropertyHandler: NotMappedPropertyHandler
  private val transformers = TransformService()

  private val invalidPropertyRegex = Regex("\\d+")

  fun writeAbstracts(version: Int, storeType: StoreType = StoreType.none) {
    val path = PathUtils.getAbstractPath()
    val maxNumberOfEntities = TestUtils.getMaxTuples()
    val store = storeProvider.getStore(storeType, path)

    val type = object : TypeToken<Map<String, String>>() {}.type
    val gson = Gson()

    val result = PathWalker.getPath(path, Regex("\\d+\\.json"))
    val startTime = System.currentTimeMillis()
    val ABSTRACT_PREDICATE = URIs.getFkgOntologyPropertyUri("abstract")

    var entityIndex = 0

    result.forEachIndexed { index, p ->
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val revisionIdMap: Map<String, String> = gson.fromJson(it, type)
          revisionIdMap.forEach { entity, abstract ->
            entityIndex++
            if (entityIndex > maxNumberOfEntities) return@forEach
            val subject = URIs.getFkgResourceUri(entity)
            store.save(
                "http://fa.wikipedia.org/wiki/" + entity.replace(' ', '_'),
                subject, ABSTRACT_PREDICATE, abstract, Module.wiki.name, version)
          }
          logger.warn("$index file is $p. time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
        }
      }
    }

    store.flush()
  }

  fun writeEntitiesWithoutInfoBox(version: Int, storeType: StoreType = StoreType.none) {
    val path = PathUtils.getWithoutInfoboxPath()
    val maxNumberOfEntities = TestUtils.getMaxTuples()
    val store = storeProvider.getStore(storeType, path)

    val result = PathWalker.getPath(path, Regex("\\d+-revision_ids\\.json"))
    val startTime = System.currentTimeMillis()

    val type = object : TypeToken<Map<String, String>>() {}.type
    val gson = Gson()

    var entityIndex = 0

    result.forEachIndexed { index, p ->
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val revisionIdMap: Map<String, String> = gson.fromJson(it, type)
          revisionIdMap.keys.forEach { entity ->
            entityIndex++
            if (entityIndex > maxNumberOfEntities) return@forEachIndexed
            entityClassImporter.addResourceAsThing(entity, store, Module.wiki.name, version)
          }
          logger.warn("$index file is $p. time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
        }
      }
    }

    store.flush()
  }

  fun writeEntitiesWithInfoBox(version: Int, storeType: StoreType = StoreType.none) {
    holder.writeToKS()
    holder.loadFromKS()

    val path = PathUtils.getWithInfoboxPath()
    val maxNumberOfEntities = TestUtils.getMaxTuples()
    val store = storeProvider.getStore(storeType, path)

    val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
    val startTime = System.currentTimeMillis()

    val type = object : TypeToken<Map<String, Map<String, List<Map<String, String>>>>>() {}.type
    val gson = Gson()
    var entityIndex = 0

    val classInfoBoxes = mutableMapOf<String, MutableList<InfoBoxAndCount>>()

    result.forEachIndexed { index, p ->
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val infoBoxes: Map<String, Map<String, List<Map<String, String>>>> = gson.fromJson(it, type)
          infoBoxes.forEach { infoBox, entityInfo ->
            entityInfo.forEach { entity, properties ->
              classInfoBoxes.getOrPut(entity, { mutableListOf() })
                  .add(InfoBoxAndCount(infoBox,
                      if (properties.isNotEmpty()) properties[0].size else 0))
            }
          }
        }
      }
      logger.warn("$index file is $p. time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
    }

    classInfoBoxes.forEach { entity, infoboxes ->
      entityIndex++
      if (entityIndex > maxNumberOfEntities) return@forEach
      if (entityIndex % 1000 == 0)
        logger.warn("$$entityIndex entities has been done." +
            " time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
      try {
        val tress = mutableSetOf<InfoBoxAndCount>()
        infoboxes.forEach {
          val normalizedTemplate = it.infoBox.toLowerCase().replace('_', ' ')
          val templateMapping = holder.getTemplateMapping(normalizedTemplate)
          var tree = ontologyLogic.getTree(templateMapping.ontologyClass)
          if (tree == null) tree = "Thing"
          it.tree = tree.split("/")
          tress.add(it)
        }
        entityClassImporter.writeEntityTrees(entity, tress, store, Module.wiki.name, version)
      } catch (th: Throwable) {
        println("entity: >>>> $entity")
        logger.error(th)
        th.printStackTrace()
      }
    }

    store.flush()
  }

  fun writeTriples(version: Int, storeType: StoreType = StoreType.none, insert: Boolean = true) {
    holder.writeToKS()
    holder.loadFromKS()

    val path = PathUtils.getTriplesPath()

    val store = storeProvider.getStore(storeType, path)
    val maxNumberOfTriples = TestUtils.getMaxTuples()

    val notSeenProperties = mutableMapOf<String, Int>()
    var numberOfMapped = 0
    var numberOfMappedInTree = 0
    var numberOfNotMapped = 0

    val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
    val startTime = System.currentTimeMillis()
    ontologyLogic.reloadTreeCache()

    val classMaps = mutableMapOf<String, MapRule>()
    holder.all().forEach { templateMapping ->
      templateMapping.properties!!.forEach { property, mapping ->
        if (mapping.rules.size == 1)
          ontologyLogic.getChildren(templateMapping.ontologyClass)?.forEach {
            classMaps[it + "~" + property] = mapping.rules.first()
          }
      }
    }

    var tripleNumber = 0
    result.forEachIndexed { index, p ->
      TripleJsonFileReader(p).use { reader ->
        while (reader.hasNext()) {
          val triple = reader.next()
          tripleNumber++
          if (tripleNumber > maxNumberOfTriples) break
          try {
            if (triple.templateType == null || triple.templateNameFull == null) continue

            if (triple.objekt!!.startsWith("fa.wikipedia.org/wiki"))
              triple.objekt = "http://" + triple.objekt

            if (tripleNumber % 1000 == 0)
              logger.warn("triple number is $tripleNumber. $index file is $p. " +
                  "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")

            val normalizedTemplate = triple.templateNameFull!!.toLowerCase().replace('_', ' ')
            val property = triple.predicate!!
            // some properties are invalid based on rdf standards
            if (property.trim().isBlank() || property.matches(invalidPropertyRegex)) continue
            val subject = URIs.convertWikiUriToResourceUri(triple.subject!!)
            val objekt = URIs.convertWikiUriToResourceUri(triple.objekt!!)

            // generate template-specific rules in first time of object
            val templateMapping = holder.getTemplateMapping(normalizedTemplate)

            templateMapping.rules!!.forEach {
              numberOfMapped++
              if (insert) store.saveTriple(triple.source!!, subject, objekt, it, version)
            }

            val propertyMapping = templateMapping.properties!![PropertyNormaller.removeDigits(property)]
            if (propertyMapping == null || propertyMapping.rules.isEmpty()) {
              if (propertyMapping != null && !propertyMapping.recommendations.isEmpty()) {
                // not too bad, we have at least some recommendations. this block is only for better clearance of code
              } else {
                val key = normalizedTemplate + "/" + property
                val old = notSeenProperties.getOrDefault(key, 0)
                notSeenProperties[key] = old + 1
              }
              val key = templateMapping.ontologyClass + "~" + property
              if (classMaps.containsKey(key)) {
                numberOfMappedInTree++
                if (insert) store.saveTriple(triple.source!!, subject, objekt, classMaps[key]!!, version)
              } else {
                numberOfNotMapped++
                notMappedPropertyHandler.addToNotMapped(property)
                if (insert) store.convertAndSave(triple.source!!, subject,
                    property, objekt, Module.wiki.name, version)
              }
            } else {
              numberOfMapped++
              propertyMapping.rules.forEach {
                if (insert) store.saveTriple(triple.source!!, subject, objekt, it, version)
              }
            }
          } catch (th: Throwable) {
            logger.info("triple: $triple")
            logger.error(th)
          }
        }
      }
    }

    if (insert) store.flush()

    logger.info("number of not seen properties ${notSeenProperties.size}")
    logger.info("number of not mapped properties $numberOfMapped")
    logger.info("number of mapped in tree $numberOfMappedInTree")
    logger.info("number of mapped is $numberOfMapped")
  }

  private fun FkgTripleDao.saveTriple(source: String, subject: String, `object`: String, rule: MapRule, version: Int) {
    var type: ValueType? = null
    if (rule.predicate == null) return
    val value = if (rule.transform != null) {
      val value = transformers.transform(rule.transform!!, `object`, LanguageChecker.detectLanguage(`object`)!!)
      type = value.type
      value.value!!
    } else if (rule.constant != null) rule.constant
    else `object`
    this.save(FkgTriple(source = source, subject = subject,
        predicate = URIs.prefixedToUri(rule.predicate),
        objekt = URIs.prefixedToUri(value.toString()),
        valueType = type, dataType = rule.unit,
        module = Module.wiki.name, version = version))
  }

}