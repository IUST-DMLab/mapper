package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.dbpediahelper.logic.EntityToClassLogic
import ir.ac.iust.dml.kg.dbpediahelper.logic.StoreProvider
import ir.ac.iust.dml.kg.dbpediahelper.logic.TripleImporter
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.MapRule
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import ir.ac.iust.dml.kg.raw.utils.Transformers
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class KGTripleImporter {

  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var entityToClassLogic: EntityToClassLogic
  @Autowired private lateinit var storeProvider: StoreProvider
  @Autowired private lateinit var predicateImporter: PredicateImporter
  @Autowired private lateinit var entityClassImporter: EntityClassImporter
  private val transformers = Transformers()

  private val invalidPropertyRegex = Regex("\\d+")

  fun writeTriples(storeType: TripleImporter.StoreType = TripleImporter.StoreType.none) {
    holder.writeToKS()
    holder.loadFromKS()

    val path = getTriplesPath()

    val store = storeProvider.getStore(storeType, path)
    val maxNumberOfTriples = ConfigReader.getInt("test.mode.max.triples", "10000000")

    store.deleteAll()

    val entityTree = mutableMapOf<String, MutableSet<String>>()
    val notSeenTemplates = mutableMapOf<String, Int>()
    val notSeenProperties = mutableMapOf<String, Int>()
    var numberOfMapped = 0
    var numberOfMappedInTree = 0
    var numberOfNotMapped = 0

    val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
    val startTime = System.currentTimeMillis()
    entityToClassLogic.reloadTreeCache()

    val classMaps = mutableMapOf<String, MapRule>()
    holder.all().forEach { templateMapping ->
      templateMapping.properties!!.forEach { property, mapping ->
        if (mapping.rules.size == 1)
          entityToClassLogic.getChildren(templateMapping.ontologyClass)?.forEach {
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
            if (property.matches(invalidPropertyRegex)) continue
            val subject = PrefixService.convertFkgResourceUrl(triple.subject!!)
            val objekt = PrefixService.convertFkgResourceUrl(triple.objekt!!)

            //generate template-specific rules in first time of object
            val templateMapping = holder.getTemplateMapping(normalizedTemplate)

            val newClassTree = entityToClassLogic.getTree(templateMapping.ontologyClass)!!
            entityTree.getOrPut(subject, { mutableSetOf() }).add(newClassTree)

            if (!entityTree.contains(subject)) {
              if (templateMapping.rules!!.isEmpty()) {
                val old = notSeenTemplates.getOrDefault(normalizedTemplate, 0)
                notSeenTemplates[normalizedTemplate] = old + 1
              }
              templateMapping.rules!!.forEach {
                numberOfMapped++
                store.saveTriple(source = triple.source!!, subject = subject,
                    objeck = objekt, rule = it)
              }
            }

            val propertyMapping = templateMapping.properties!![property]
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
                store.saveTriple(source = triple.source!!, subject = subject,
                    objeck = objekt, rule = classMaps[key]!!)
              } else {
                numberOfNotMapped++
                store.convertAndSave(source = triple.source!!, subject = subject,
                    objeck = objekt, property = property)
              }
            } else {
              numberOfMapped++
              propertyMapping.rules.forEach {
                store.saveTriple(source = triple.source!!, subject = subject,
                    objeck = objekt, rule = it)
              }
            }
          } catch (th: Throwable) {
            logger.info("triple: $triple")
            logger.error(th)
          }
        }
      }
    }

    entityToClassLogic.writeTree(store)
    entityClassImporter.writeEntityTrees(entityTree, store)
    predicateImporter.writePredicates(store)

    if (store is KnowledgeStoreFkgTripleDaoImpl) store.flush()
    if (store is VirtuosoFkgTripleDaoImpl) store.close()

    logger.info("number of not seen templates ${notSeenTemplates.size}")
    logger.info("number of not seen properties ${notSeenProperties.size}")
    logger.info("number of not mapped properties $numberOfMapped")
    logger.info("number of mapped in tree $numberOfMappedInTree")
    logger.info("number of mapped is $numberOfMapped")
  }

  fun rewriteLabels(storeType: TripleImporter.StoreType = TripleImporter.StoreType.none) {

    val path = getTriplesPath()
    val store = storeProvider.getStore(storeType, path)
    val maxNumberOfTriples = ConfigReader.getInt("test.mode.max.triples", "10000000")

    val visitedSources = mutableSetOf<String>()

    val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))

    var tripleNumber = 0
    result.forEachIndexed { index, p ->
      TripleJsonFileReader(p).use { reader ->
        while (reader.hasNext()) {
          val triple = reader.next()
          tripleNumber++
          if (tripleNumber > maxNumberOfTriples) break
          try {
            if (triple.subject == null || !triple.subject!!.contains('/')) continue
            if (tripleNumber % 100000 == 0) logger.warn("triple number is $tripleNumber. $index file is $p. ")
            visitedSources.add(triple.subject!!)
          } catch (th: Throwable) {
            logger.info("triple: $triple")
            logger.error(th)
          }
        }
      }
    }

    val VARIANT_LABEL_URL = PrefixService.prefixToUri(PrefixService.VARIANT_LABEL_URL)!!

    visitedSources.forEach { source ->
      val subject = PrefixService.convertFkgResourceUrl(source)
      var label = source.substringAfterLast("/").replace('_', ' ')
//      store.convertAndSave(source = subject, subject = subject, property = LABEL, objeck = label)
      if (label.contains('(')) {
        label = label.substringBeforeLast('(').trim()
        store.convertAndSave(source = source, subject = subject, property = VARIANT_LABEL_URL, objeck = label)
      }
    }

    if (store is KnowledgeStoreFkgTripleDaoImpl) store.flush()
    if (store is VirtuosoFkgTripleDaoImpl) store.close()
  }

  private fun getTriplesPath(): Path {
    val path = ConfigReader.getPath("wiki.triple.input.folder", "~/.pkg/data/triples")
    if (!Files.exists(path.parent)) Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
      throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }
    return path
  }

  private fun FkgTripleDao.saveTriple(source: String, subject: String, objeck: String, rule: MapRule) {
    val value = if (rule.transform != null) {
      Transformers::class.java.getMethod(rule.transform, String::class.java).invoke(transformers, objeck)
    } else if (rule.constant != null) rule.constant
    else objeck
    this.save(FkgTriple(source = source, subject = subject,
        predicate = PrefixService.prefixToUri(rule.predicate),
        objekt = PrefixService.prefixToUri(value.toString())), null)
  }
}