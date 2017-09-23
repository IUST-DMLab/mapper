/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.wiki

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.FkgTripleProperty
import ir.ac.iust.dml.kg.knowledge.core.ValueType
import ir.ac.iust.dml.kg.mapper.logic.DumpUtils
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
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Path

@Service
class WikiTripleImporter {

  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var ontologyLogic: OntologyLogic
  @Autowired private lateinit var storeProvider: StoreProvider
  @Autowired private lateinit var entityClassImporter: EntityClassImporter
  @Autowired private lateinit var notMappedPropertyHandler: NotMappedPropertyHandler
  private val transformers = TransformService()


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

    val maxNumberOfEntities = TestUtils.getMaxTuples()
    val store = storeProvider.getStore(storeType)

    val startTime = System.currentTimeMillis()
    var entityIndex = 0

    val classInfoBoxes = mutableMapOf<String, MutableList<InfoBoxAndCount>>()

    DumpUtils.read({ infobox, entity, properties ->
      classInfoBoxes.getOrPut(entity, { mutableListOf() })
          .add(InfoBoxAndCount(infobox, properties.size))
    })

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

  data class TripleInfo(var source: String, var subject: String, var `object`: String,
                        var property: String?, var rule: MapRule?, var version: Int)

  fun writeTriples(version: Int, storeType: StoreType = StoreType.none, insert: Boolean = true, path: Path? = null) {
    holder.writeToKS()
    holder.loadFromKS()

    val store = storeProvider.getStore(storeType, path)

    val notSeenProperties = mutableMapOf<String, Int>()
    var numberOfMapped = 0
    var numberOfMappedInTree = 0
    var numberOfNotMapped = 0

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

    DumpUtils.getTriples { triples ->
      DumpUtils.collectTriples(triples).forEach { tripleCollection ->
        // triple collection can be just one triple in most of cases. but when we have numbered keys, they are
        // collected as a collection with size > 1
        val triplesToWrite = mutableListOf<TripleInfo>()
        tripleCollection.forEach { triple ->
          val normalizedTemplate = triple.templateNameFull!!.toLowerCase().replace('_', ' ')
          val property = triple.predicate!!
          val subject = URIs.convertWikiUriToResourceUri(triple.subject!!)
          val objekt = URIs.convertWikiUriToResourceUri(triple.objekt!!)

          // generate template-specific rules in first time of object
          val templateMapping = holder.getTemplateMapping(normalizedTemplate)

          templateMapping.rules!!.forEach {
            numberOfMapped++
            if (insert) store.save(getAsTripe(TripleInfo(triple.source!!, subject, objekt, null, it, version))!!)
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
              if (insert) triplesToWrite.add(TripleInfo(triple.source!!, subject, objekt,
                  null, classMaps[key]!!, version))
            } else {
              numberOfNotMapped++
              notMappedPropertyHandler.addToNotMapped(property)
              if (insert)
                triplesToWrite.add(TripleInfo(triple.source!!, subject, objekt, property, null, version))
            }
          } else {
            numberOfMapped++
            propertyMapping.rules.forEach {
              if (insert) triplesToWrite.add(TripleInfo(triple.source!!, subject, objekt, null, it, version))
            }
          }
        }
        if (triplesToWrite.isEmpty()) return@getTriples
        val first = getAsTripe(triplesToWrite[0])
        if (first != null) {
          for (i in 1 until triplesToWrite.size) {
            val child = getAsTripe(triplesToWrite[i])
            if (child != null)
              first.properties.add(FkgTripleProperty(null, first, child.predicate, child.objekt,
                  child.language, child.valueType))
          }
          store.save(first)
        } else
          for (i in 1 until triplesToWrite.size) {
            val child = getAsTripe(triplesToWrite[i])
            if (child != null) store.save(child)
          }
      }
    }

    if (insert) store.flush()

    logger.info("number of not seen properties ${notSeenProperties.size}")
    logger.info("number of not mapped properties $numberOfMapped")
    logger.info("number of mapped in tree $numberOfMappedInTree")
    logger.info("number of mapped is $numberOfMapped")
  }

  private fun getAsTripe(info: TripleInfo) =
      getAsTripe(info.source, info.subject, info.`object`, info.property, info.rule, info.version)


  private fun getAsTripe(source: String, subject: String, `object`: String, property: String?,
                         rule: MapRule?, version: Int): FkgTriple? {
    if (rule == null) {
      return FkgTriple(source = source, subject = subject,
          predicate = URIs.convertToNotMappedFkgPropertyUri(property!!),
          objekt = URIs.prefixedToUri(`object`),
          module = Module.wiki.name, version = version)
    }
    var type: ValueType? = null
    if (rule.predicate == null) return null
    val value = if (rule.transform != null) {
      val value = transformers.transform(rule.transform!!, `object`, LanguageChecker.detectLanguage(`object`)!!)
      type = value.type
      value.value!!
    } else if (rule.constant != null) rule.constant
    else `object`
    return FkgTriple(source = source, subject = subject,
        predicate = URIs.prefixedToUri(rule.predicate),
        objekt = URIs.prefixedToUri(value.toString()),
        valueType = type, dataType = rule.unit,
        module = Module.wiki.name, version = version)
  }

}