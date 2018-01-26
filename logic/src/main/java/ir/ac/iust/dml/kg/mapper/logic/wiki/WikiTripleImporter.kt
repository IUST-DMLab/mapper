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
import ir.ac.iust.dml.kg.mapper.logic.data.InfoBoxAndCount
import ir.ac.iust.dml.kg.mapper.logic.data.MapRule
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.mapper.logic.data.TemplateMapping
import ir.ac.iust.dml.kg.mapper.logic.mapping.KSMappingHolder
import ir.ac.iust.dml.kg.mapper.logic.mapping.TransformService
import ir.ac.iust.dml.kg.mapper.logic.ontology.EntityClassImporter
import ir.ac.iust.dml.kg.mapper.logic.ontology.NotMappedPropertyHandler
import ir.ac.iust.dml.kg.mapper.logic.ontology.OntologyLogic
import ir.ac.iust.dml.kg.mapper.logic.utils.PathUtils
import ir.ac.iust.dml.kg.mapper.logic.utils.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.utils.TestUtils
import ir.ac.iust.dml.kg.raw.utils.*
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleData
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path

@Service
class WikiTripleImporter {

  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var ontologyLogic: OntologyLogic
  @Autowired private lateinit var storeProvider: StoreProvider
  @Autowired private lateinit var entityClassImporter: EntityClassImporter
  @Autowired private lateinit var notMappedPropertyHandler: NotMappedPropertyHandler
  @Autowired private lateinit var redirectLogic: RedirectLogic
  @Autowired private lateinit var transformers: TransformService


  fun writeAbstracts(version: Int, storeType: StoreType = StoreType.none) {
    val path = PathUtils.getAbstractPath()
    val maxNumberOfEntities = TestUtils.getMaxTuples()
    val store = storeProvider.getStore(storeType)

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
          for (entity in revisionIdMap.keys) {
            val abstract = revisionIdMap[entity] ?: continue
            entityIndex++
            if (entityIndex > maxNumberOfEntities) continue
            val subject = URIs.getFkgResourceUri(entity)
            store.save(
                "http://fa.wikipedia.org/wiki/" + entity.replace(' ', '_'),
                subject, ABSTRACT_PREDICATE, abstract, Module.wiki.name, version, "fa")
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
    val store = storeProvider.getStore(storeType)

    val result = PathWalker.getPath(path, Regex("\\d+-revision_ids\\.json"))
    val startTime = System.currentTimeMillis()

    val type = object : TypeToken<Map<String, String>>() {}.type
    val gson = Gson()

    var entityIndex = 0

    val redirects = redirectLogic.getRedirects()

    result.forEachIndexed { index, p ->
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val revisionIdMap: Map<String, String> = gson.fromJson(it, type)
          revisionIdMap.keys.forEach { entity ->
            entityIndex++
            if (entityIndex > maxNumberOfEntities) return@forEachIndexed
            if (!redirects.contains(entity))
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
    ontologyLogic.reloadTreeCache()

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
        for (infobox in infoboxes) {
          val normalizedTemplate = infobox.infoBox.toLowerCase().replace('_', ' ')
          val templateMapping = holder.getTemplateMapping(normalizedTemplate)
          var tree = ontologyLogic.getTree(templateMapping.ontologyClass)
          if (tree == null) tree = "Thing"
          infobox.tree = tree.split("/")
          tress.add(infobox)
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

  fun writeCategoryTriples(version: Int, storeType: StoreType = StoreType.none, insert: Boolean = true, path: Path? = null) {
    val store = storeProvider.getStore(storeType)
    val categories = mutableSetOf<String>()
    DumpUtils.getTriples(PathUtils.getCategoryTriplesPath(), "\\d+\\.json", { triples ->
      var subjectUrl: String? = null
      triples.forEach { triple ->
        if (triple.source == null || triple.subject == null || triple.objekt == null ||
            triple.objekt!!.isBlank() || triple.predicate != "wikiCategory")
          return@getTriples
        if (subjectUrl == null) subjectUrl = URIs.convertWikiUriToResourceUri(triple.subject!!)
        val categoryUrl = URIs.getFkgCategoryUri(triple.objekt!!)
        if (!categories.contains(triple.objekt!!)) {
          categories.add(triple.objekt!!)
          // it's new category
          val wikiAddress = "https://fa.wikipedia.org/wiki/Category:${triple.objekt}"
          store.save(wikiAddress, categoryUrl, URIs.type, URIs.typeOfAllCategories, Module.wiki.name, version)
          store.save(wikiAddress, categoryUrl, URIs.label, triple.objekt!!, Module.wiki.name, version)
          store.save(wikiAddress, categoryUrl, URIs.variantLabel, triple.objekt!!, Module.wiki.name, version)
          store.save(wikiAddress, categoryUrl, URIs.preferedLabel, triple.objekt!!, Module.wiki.name, version)
          store.save(wikiAddress, categoryUrl, URIs.wasDerivedFrom, wikiAddress, Module.wiki.name, version)
        }
        // subject and objects are written reversely in store object <--> subject
        if (insert)
          store.save(triple.source!!, subjectUrl!!, URIs.categoryMember, categoryUrl, Module.wiki.name, version)
      }
    }, false)
    store.flush()
  }

  fun createTestTriples(vararg subjectToFilter: String?) {
    logger.info("going to filter ${subjectToFilter.joinToString(", ")} from triples")
    val outPath = PathUtils.getTriplesTestPath().resolve("0-infoboxes.json")
    if (!Files.exists(outPath.parent)) Files.createDirectories(outPath.parent)
    DumpUtils.tripleFilter(PathUtils.getTriplesPath(), "\\d+-infoboxes\\.json",
        subjectToFilter
            .mapNotNull { it }
            .map { "http://fa.wikipedia.org/wiki/${it.replace(' ', '_')}" }
            .toSet(),
        outPath)
  }

  fun writeTriples(version: Int, storeType: StoreType = StoreType.none, insert: Boolean = true) {
    holder.writeToKS()
    holder.loadFromKS()

    val store = storeProvider.getStore(storeType)

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

    val notMappedPrefix = URIs.fkgNotMappedPropertyPrefix + ":"

    DumpUtils.getTriples(
        if (TestUtils.isDebugMode()) PathUtils.getTriplesTestPath() else PathUtils.getTriplesPath(),
        "\\d+-infoboxes\\.json", { triples ->
      if (triples.isEmpty()) return@getTriples
      val firstTriple = triples.first()
      val subject = URIs.convertWikiUriToResourceUri(firstTriple.subject!!)

      val templateMappings = mutableMapOf<String, TemplateMapping>()
      for (triple in triples) {
        if (!templateMappings.containsKey(triple.templateNameFull)) {
          val normalizedTemplate = triple.templateNameFull!!.toLowerCase().replace('_', ' ')
          // generate template-specific rules in first time of object
          val templateMapping = holder.getTemplateMapping(normalizedTemplate)
          for (rule in templateMapping.rules!!) {
            numberOfMapped++
            if (rule.constant == null || triple.predicate == null || triple.predicate!!.isBlank()) continue
            if (insert) store.save(getAsTripe(TripleInfo(firstTriple.source!!, subject, rule.constant!!,
                rule.predicate, rule, version))!!)
          }
          templateMappings[triple.templateNameFull!!] = templateMapping
        }
      }

      DumpUtils.collectTriples(triples).forEach { tripleCollection ->
        if (tripleCollection.size == 2 &&
            tripleCollection[0].objekt != null &&
            tripleCollection[1].objekt != null &&
            tripleCollection[0].predicate?.contains("name") == true &&
            tripleCollection[1].predicate?.contains("type") == true) {
          val key =
              if (tripleCollection[1].objekt!!.contains("/"))
                tripleCollection[1].objekt!!.substringAfterLast('/').replace('_', ' ')
              else tripleCollection[1].objekt!!
          tripleCollection.add(0, TripleData(
              tripleCollection[0].source, tripleCollection[0].subject, key, tripleCollection[0].templateNameFull,
              tripleCollection[0].templateName, tripleCollection[0].templateType, tripleCollection[0].objekt
          ))
        }
        // triple collection can be just one triple in most of cases. but when we have numbered keys, they are
        // collected as a collection with size > 1
        val triplesToWrite = mutableListOf<TripleInfo>()
        for (triple in tripleCollection) {
          val property = triple.predicate!!
          val objekt = URIs.convertWikiUriToResourceUri(triple.objekt!!)

          val templateMapping = templateMappings[triple.templateNameFull]!!
          val normalizedProperty = PropertyNormaller.removeDigits(property)
          val propertyMapping = holder.getPropertyMapping(templateMapping.template!!, normalizedProperty)
//          val propertyMapping = templateMapping.properties!![PropertyNormaller.removeDigits(property)]
          val propertyRules = propertyMapping.rules.filter {
            // This filter just fixes wrong mappings which are mapped to not ontology maps.
            it.predicate != null && !(it.predicate?.startsWith(notMappedPrefix) ?: false)
          }
          if (propertyRules.isEmpty()) {
            if (!propertyMapping.recommendations.isEmpty()) {
              // not too bad, we have at least some recommendations. this block is only for better clearance of code
            } else {
              val key = templateMapping.template + "/" + property
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
            propertyRules.forEach {
              if (insert) triplesToWrite.add(TripleInfo(triple.source!!, subject, objekt, null, it, version))
            }
          }
        }
        if (triplesToWrite.isEmpty()) return@getTriples
        if (tripleCollection.size > 1) {
          val first = getAsTripe(triplesToWrite[0])
          if (first != null) {
            (1 until triplesToWrite.size)
                .mapNotNull { getAsTripe(triplesToWrite[it]) }
                .forEach {
                  first.properties.add(FkgTripleProperty(null, first, it.predicate, it.objekt,
                      it.language, it.valueType))
                }
            store.save(first)
          } else
            (1 until triplesToWrite.size)
                .mapNotNull { getAsTripe(triplesToWrite[it]) }
                .forEach { store.save(it) }
        } else // collection may have just one triple but maps to more than one triple.
          triplesToWrite.forEach {
            val triple = getAsTripe(it)
            if (triple != null) store.save(triple)
          }
      }
    })

    if (insert) store.flush()

    logger.info("number of not seen properties ${notSeenProperties.size}")
    logger.info("number of not mapped properties $numberOfMapped")
    logger.info("number of mapped in tree $numberOfMappedInTree")
    logger.info("number of mapped is $numberOfMapped")
    holder.writeToKS()
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
    if (rule.predicate == null || rule.predicate == "NULL")
      return null
    val value = when {
      rule.transform != null -> {
        try {
          val value = transformers.transform(rule.transform!!, `object`, LanguageChecker.detectLanguage(`object`)!!)
          type = value.type
          value.value!!
        } catch (th: Throwable) {
          type = ValueType.String
          `object`
        }
      }
      rule.constant != null -> rule.constant
      else -> `object`
    }
    return FkgTriple(source = source, subject = subject,
        predicate = URIs.prefixedToUri(rule.predicate),
        objekt = URIs.prefixedToUri(value.toString()) ?: value.toString(),
        valueType = type, dataType = rule.unit,
        module = Module.wiki.name, version = version)
  }

}