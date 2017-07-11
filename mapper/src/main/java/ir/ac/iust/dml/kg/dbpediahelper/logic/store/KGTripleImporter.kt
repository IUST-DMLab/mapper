package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.dbpediahelper.logic.EntityToClassLogic
import ir.ac.iust.dml.kg.dbpediahelper.logic.StoreProvider
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.MapRule
import ir.ac.iust.dml.kg.dbpediahelper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.*
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path

@Service
class KGTripleImporter {

  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var entityToClassLogic: EntityToClassLogic
  @Autowired private lateinit var storeProvider: StoreProvider
  @Autowired private lateinit var entityClassImporter: EntityClassImporter
  private val transformers = Transformers()

  private val invalidPropertyRegex = Regex("\\d+")

  fun writeAbstracts(storeType: StoreType = StoreType.none) {
    val path = getPath("wiki.abstracts.input.folder", "~/.pkg/data/abstract_tuples")
    val maxNumberOfTriples = ConfigReader.getInt("test.mode.max.triples", "10000000")
    val store = storeProvider.getStore(storeType, path)

    val result = PathWalker.getPath(path, Regex("\\d+\\.json"))
    val startTime = System.currentTimeMillis()
    var tripleNumber = 0
    val ABSTRACT_PREDICATE = PrefixService.getFkgOntologyPropertyUrl("abstract")
    result.forEachIndexed { index, p ->
      TripleJsonFileReader(p).use { reader ->
        while (reader.hasNext()) {
          val triple = reader.next()
          tripleNumber++
          if (tripleNumber > maxNumberOfTriples) break
          try {
            if (triple.objekt == null) continue
            if (tripleNumber % 1000 == 0)
              logger.warn("triple number is $tripleNumber. $index file is $p. " +
                  "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            val subject = PrefixService.convertFkgResourceUrl(triple.subject!!)
            store.save(source = triple.source!!, subject = subject,
                objeck = triple.objekt!!, property = ABSTRACT_PREDICATE)
          } catch (th: Throwable) {
          }
        }
      }
    }
    (store as? KnowledgeStoreFkgTripleDaoImpl)?.flush()
    (store as? VirtuosoFkgTripleDaoImpl)?.close()
  }

  fun writeEntitiesWithoutInfoBox(storeType: StoreType = StoreType.none) {
    val path = getPath("wiki.without.info.box.input.folder", "~/.pkg/data/without_infobox")
    val maxNumberOfFiles = ConfigReader.getInt("test.mode.max.files", "1")
    val store = storeProvider.getStore(storeType, path)

    val result = PathWalker.getPath(path, Regex("\\d+-revision_ids\\.json"))
    val startTime = System.currentTimeMillis()

    val type = object : TypeToken<Map<String, String>>() {}.type
    val gson = Gson()

    result.forEachIndexed { index, p ->
      if (index > maxNumberOfFiles) return@forEachIndexed
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val revisionIdMap: Map<String, String> = gson.fromJson(it, type)
          revisionIdMap.keys.forEach { entity ->
            entityClassImporter.addResourceAsThing(entity, store)
          }
          logger.warn("$index file is $p. time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
        }
      }
    }

    (store as? KnowledgeStoreFkgTripleDaoImpl)?.flush()
    (store as? VirtuosoFkgTripleDaoImpl)?.close()
  }

  fun writeEntitiesWithInfoBox(storeType: StoreType = StoreType.none) {
    holder.writeToKS()
    holder.loadFromKS()

    val path = getPath("wiki.with.info.box.input.folder", "~/.pkg/data/with_infobox")
    val maxNumberOfFiles = ConfigReader.getInt("test.mode.max.files", "1000")
    val store = storeProvider.getStore(storeType, path)

    val result = PathWalker.getPath(path, Regex("\\d+\\.json"))
    val startTime = System.currentTimeMillis()

    val type = object : TypeToken<Map<String, List<String>>>() {}.type
    val gson = Gson()
    var entityIndex = 0

    result.forEachIndexed { index, p ->
      if (index > maxNumberOfFiles) return@forEachIndexed
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val infoBoxes: Map<String, List<String>> = gson.fromJson(it, type)
          infoBoxes.forEach { entity, infoboxes ->
            entityIndex++
            if (entityIndex % 1000 == 0)
              logger.warn("$$entityIndex entities has been done." +
                  " time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            try {
              val tress = mutableSetOf<String>()
              infoboxes.forEach {
                val normalizedTemplate = it.toLowerCase().replace('_', ' ')
                val templateMapping = holder.getTemplateMapping(normalizedTemplate)
                var tree = entityToClassLogic.getTree(templateMapping.ontologyClass)
                if (tree == null) tree = "Thing"
                tress.add(tree)
              }
              entityClassImporter.writeEntityTrees(entity, tress, store)
            } catch (th: Throwable) {
              println("entity: >>>> $entity")
              logger.error(th)
              th.printStackTrace()
            }
          }
          logger.warn("$index file is $p. time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
        }
      }
    }

    (store as? KnowledgeStoreFkgTripleDaoImpl)?.flush()
    (store as? VirtuosoFkgTripleDaoImpl)?.close()
  }

  fun writeTriples(storeType: StoreType = StoreType.none) {
    holder.writeToKS()
    holder.loadFromKS()

    val path = getTriplesPath()

    val store = storeProvider.getStore(storeType, path)
    val maxNumberOfTriples = ConfigReader.getInt("test.mode.max.triples", "10000000")

    store.deleteAll()

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
            if (property.trim().isBlank() || property.matches(invalidPropertyRegex)) continue
            val subject = PrefixService.convertFkgResourceUrl(triple.subject!!)
            val objekt = PrefixService.convertFkgResourceUrl(triple.objekt!!)

            // generate template-specific rules in first time of object
            val templateMapping = holder.getTemplateMapping(normalizedTemplate)

            templateMapping.rules!!.forEach {
              numberOfMapped++
              store.saveTriple(source = triple.source!!, subject = subject, objeck = objekt, rule = it)
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

    (store as? KnowledgeStoreFkgTripleDaoImpl)?.flush()
    (store as? VirtuosoFkgTripleDaoImpl)?.close()

    logger.info("number of not seen properties ${notSeenProperties.size}")
    logger.info("number of not mapped properties $numberOfMapped")
    logger.info("number of mapped in tree $numberOfMappedInTree")
    logger.info("number of mapped is $numberOfMapped")
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

  private fun getTriplesPath() = getPath("wiki.triple.input.folder", "~/.pkg/data/triples")

  private fun getPath(key: String, defaultValue: String): Path {
    val path = ConfigReader.getPath(key, defaultValue)
    if (!Files.exists(path.parent)) Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
      throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }
    return path
  }
}