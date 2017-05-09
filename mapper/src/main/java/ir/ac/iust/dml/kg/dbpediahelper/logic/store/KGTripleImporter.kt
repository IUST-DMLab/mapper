package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.file.FileFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.dbpediahelper.logic.EntityToClassLogic
import ir.ac.iust.dml.kg.dbpediahelper.logic.TripleImporter
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.MapRule
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
import ir.ac.iust.dml.kg.utils.PrefixService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class KGTripleImporter {

  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var tripleDao: FkgTripleDao
  @Autowired private lateinit var entityToClassLogic: EntityToClassLogic
  private val transformers = MappingTransformers()

  fun writeTriples(storeType: TripleImporter.StoreType = TripleImporter.StoreType.none) {
    holder.writeToKS()
    holder.loadFromKS()

    val path = ConfigReader.getPath("wiki.triple.input.folder", "~/.pkg/data/triples")
    if (!Files.exists(path.parent)) Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
      throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }

    val store = when (storeType) {
      TripleImporter.StoreType.file -> FileFkgTripleDaoImpl(path.resolve("mapped"))
      TripleImporter.StoreType.mysql -> tripleDao
      TripleImporter.StoreType.virtuoso -> VirtuosoFkgTripleDaoImpl()
      else -> KnowledgeStoreFkgTripleDaoImpl()
    }
    val maxNumberOfTriples = ConfigReader.getInt("test.mode.max.triples", "10000000")

    store.deleteAll()

    val entityTree = mutableMapOf<String, MutableSet<String>>()
    val notSeenTemplates = mutableMapOf<String, Int>()
    val notSeenProperties = mutableMapOf<String, Int>()
    var numberOfMapped = 0
    var numberOfNotMapped = 0

    val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
    val startTime = System.currentTimeMillis()
    entityToClassLogic.reloadTreeCache()
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

            //generate template-specific rules in first time of object
            val templateMapping = holder.getTemplateMapping(normalizedTemplate)

            val newClassTree = entityToClassLogic.getTree(templateMapping.ontologyClass)!!
            entityTree.getOrPut(triple.subject!!, { mutableSetOf() }).add(newClassTree)


            if (!entityTree.contains(triple.subject!!)) {
              if (templateMapping.rules!!.isEmpty()) {
                val old = notSeenTemplates.getOrDefault(normalizedTemplate, 0)
                notSeenTemplates[normalizedTemplate] = old + 1
              }
              templateMapping.rules!!.forEach {
                numberOfMapped++
                store.saveTriple(source = triple.source!!, subject = triple.subject!!,
                    objeck = triple.objekt!!, rule = it)
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
              numberOfNotMapped++
              store.saveRawTriple(source = triple.source!!, subject = triple.subject!!,
                  objeck = triple.objekt!!, property = property)
            } else {
              numberOfMapped++
              propertyMapping.rules.forEach {
                store.saveTriple(source = triple.source!!, subject = triple.subject!!,
                    objeck = triple.objekt!!, rule = it)
              }
            }
          } catch (th: Throwable) {
            logger.info("triple: $triple")
            logger.error(th)
          }
        }
      }
    }

    entityTree.forEach { entity, ontologyClass ->
      var longestTree = listOf<String>("Thing")
      val allClasses = mutableSetOf<String>()

      ontologyClass.forEach {
        val t = it.split('/')
        if (t.size > longestTree.size) longestTree = t
        allClasses.addAll(t)
      }

      store.saveRawTriple(entity, entity, PrefixService.getFkgOntologyClass(longestTree.last()),
          "fkgo:instanceOf")

      allClasses.forEach {
        store.saveRawTriple(entity, entity, PrefixService.getFkgOntologyClass(it), "rdf:type")
      }
    }

    if (store is KnowledgeStoreFkgTripleDaoImpl) store.flush()
    if (store is VirtuosoFkgTripleDaoImpl) store.close()

    logger.warn("number of not seen templates ${notSeenTemplates.size}")
    logger.warn("number of not seen properties ${notSeenProperties.size}")
    logger.warn("number of not mapped properties $numberOfMapped")
    logger.warn("number of mapped is $numberOfMapped")
  }

  private fun FkgTripleDao.saveTriple(source: String, subject: String, objeck: String, rule: MapRule) {
    val value = if (rule.transform != null) {
      MappingTransformers::class.java.getMethod(rule.transform, String::class.java).invoke(transformers, objeck)
    } else if (rule.constant != null) rule.constant
    else objeck
    this.save(FkgTriple(source = source, subject = PrefixService.convertFkgResource(subject),
        predicate = PrefixService.prefixToUri(rule.predicate), objekt = value.toString()), null)
  }

  private fun FkgTripleDao.saveRawTriple(source: String, subject: String, objeck: String, property: String) {
    this.save(FkgTriple(source = source, subject = PrefixService.convertFkgResource(subject),
        predicate = PrefixService.convertFkgProperty(property), objekt = objeck), null)
  }
}