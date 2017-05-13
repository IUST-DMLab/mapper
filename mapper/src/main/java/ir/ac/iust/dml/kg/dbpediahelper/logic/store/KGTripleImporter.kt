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
    var numberOfMappedInTree = 0
    var numberOfNotMapped = 0

    val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
    val startTime = System.currentTimeMillis()
    entityToClassLogic.reloadTreeCache()

    val classMaps = mutableMapOf<String, MapRule>()
    holder.all().forEach { templateMapping ->
      templateMapping.properties!!.forEach { property, mapping ->
        val tree = entityToClassLogic.getChildren(templateMapping.ontologyClass) ?: mutableListOf()
        tree.forEach {
          ontologyClass ->
          if (mapping.rules.size == 1)
            classMaps[ontologyClass + "~" + property] = mapping.rules.first()
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
                store.saveRawTriple(source = triple.source!!, subject = subject,
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

    val PROPERTY_LABEL_URL = PrefixService.prefixToUri(PrefixService.PROPERTY_LABEL_URL)!!
    val RESOURCE_LABEL_URL = PrefixService.prefixToUri(PrefixService.RESOURCE_LABEL_URL)!!
    val PROPERTY_INSTANCE_OF_URL_URL = PrefixService.prefixToUri(PrefixService.INSTANCE_OF_URL)!!
    val PROPERTY_TYPE_URI = PrefixService.prefixToUri(PrefixService.PROPERTY_URI)!!
    val PROPERTY_DOMAIN_URL = PrefixService.prefixToUri(PrefixService.PROPERTY_DOMAIN_URL)!!
    val TYPE_URL = PrefixService.prefixToUri(PrefixService.TYPE_URL)!!
    val PROPERTY_VARIANT_LABEL_URL = PrefixService.prefixToUri(PrefixService.PROPERTY_VARIANT_LABEL_URL)!!

    entityTree.forEach { entity, ontologyClass ->
      var longestTree = listOf<String>("Thing")
      val allClasses = mutableSetOf<String>()

      ontologyClass.forEach {
        val t = it.split('/')
        if (t.size > longestTree.size) longestTree = t
        allClasses.addAll(t)
      }

      store.saveRawTriple(entity, entity, entity.substringAfterLast('/').replace('_', ' ').trim(),
          RESOURCE_LABEL_URL)

      store.saveRawTriple(entity, entity, PrefixService.getFkgOntologyClass(longestTree.first()),
          PROPERTY_INSTANCE_OF_URL_URL)

      allClasses.forEach {
        store.saveRawTriple(entity, entity, PrefixService.getFkgOntologyClass(it), "rdf:type")
      }
    }

    data class PredicateData(var labels: MutableMap<String, Double> = mutableMapOf(),
                             var domains: MutableSet<String> = mutableSetOf())

    val predicateData = mutableMapOf<String, PredicateData>()

    holder.all().forEach { templateMapping ->
      templateMapping.properties!!.values.forEach { propertyMapping ->
        val label = propertyMapping.property!!.toLowerCase().replace('_', ' ')
        propertyMapping.rules.forEach {
          val data = predicateData.getOrPut(it.predicate!!, { PredicateData() })
          data.labels[label] = (data.labels[label] ?: 0.0) + (propertyMapping.weight ?: 0.0)
          data.domains.add(templateMapping.ontologyClass)
        }
      }
    }

    predicateData.forEach { predicate, data ->
      val labels = data.labels.map { Pair(it.key, it.value) }.sortedByDescending { it.second }
      val pu = PrefixService.prefixToUri(predicate)!!
      if (!pu.contains("://")) {
        logger.error("wrong predicate: $pu")
        return@forEach
      }
      store.saveRawTriple(source = pu, subject = pu, property = TYPE_URL, objeck = PROPERTY_TYPE_URI)
      if (labels.isNotEmpty())
        store.saveRawTriple(source = pu, subject = pu, property = PROPERTY_LABEL_URL, objeck = labels[0].first)
      labels.forEach {
        store.saveRawTriple(source = pu, subject = pu, property = PROPERTY_VARIANT_LABEL_URL, objeck = it.first)
      }
      data.domains.forEach {
        store.saveRawTriple(source = pu, subject = pu, property = PROPERTY_DOMAIN_URL,
            objeck = PrefixService.getFkgOntologyClassUrl(it))
      }
    }

    if (store is KnowledgeStoreFkgTripleDaoImpl) store.flush()
    if (store is VirtuosoFkgTripleDaoImpl) store.close()

    logger.info("number of not seen templates ${notSeenTemplates.size}")
    logger.info("number of not seen properties ${notSeenProperties.size}")
    logger.info("number of not mapped properties $numberOfMapped")
    logger.info("number of mapped in tree $numberOfMappedInTree")
    logger.info("number of mapped is $numberOfMapped")
  }

  private fun FkgTripleDao.saveTriple(source: String, subject: String, objeck: String, rule: MapRule) {
    val value = if (rule.transform != null) {
      MappingTransformers::class.java.getMethod(rule.transform, String::class.java).invoke(transformers, objeck)
    } else if (rule.constant != null) rule.constant
    else objeck
    this.save(FkgTriple(source = source, subject = subject,
        predicate = PrefixService.prefixToUri(rule.predicate),
        objekt = PrefixService.prefixToUri(value.toString())), null)
  }

  private fun FkgTripleDao.saveRawTriple(source: String, subject: String, objeck: String, property: String) {
    this.save(FkgTriple(source = source, subject = subject,
        predicate = PrefixService.convertFkgProperty(property),
        objekt = PrefixService.prefixToUri(objeck)), null)
  }
}