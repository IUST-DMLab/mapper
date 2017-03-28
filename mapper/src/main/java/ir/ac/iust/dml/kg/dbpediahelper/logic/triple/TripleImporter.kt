package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

import ir.ac.iust.dml.kg.access.dao.*
import ir.ac.iust.dml.kg.access.dao.file.FileFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.memory.StatisticalEventDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.access.entities.enumerations.TripleStatisticsType
import ir.ac.iust.dml.kg.dbpediahelper.logic.PrefixService
import ir.ac.iust.dml.kg.utils.ConfigReader
import ir.ac.iust.dml.kg.utils.PathWalker
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

@Service
class TripleImporter {

  val logger = Logger.getLogger(this.javaClass)!!
  @Autowired lateinit var tripleDao: FkgTripleDao
  @Autowired lateinit var mappingDao: FkgPropertyMappingDao
  @Autowired lateinit var wikiTemplateRedirectDao: WikipediaTemplateRedirectDao
  @Autowired lateinit var wikiPropertyTranslationDao: WikipediaPropertyTranslationDao
  @Autowired lateinit var fkgTripleStatisticsDao: FkgTripleStatisticsDao
  @Autowired lateinit var prefixService: PrefixService
  @Autowired lateinit var eventDao: StatisticalEventDaoImpl

  enum class StoreType {
    none, file, mysql, virtuoso
  }

  @Throws(Exception::class)
  fun writeStats() {
    val WIKI_DUMP_ARTICLE = "mapped.triple.stats.file"
    val config = ConfigReader.getConfig(mapOf(WIKI_DUMP_ARTICLE to "~/.pkg/data/triples/mapped/stats.txt"))
    val path = ConfigReader.getPath(config[WIKI_DUMP_ARTICLE]!! as String)
    Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
      throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }

    fkgTripleStatisticsDao.deleteAll()

    StatisticsLogReader(path).use {
      var lineNumber = 0
      while (it.hasNext()) {
        lineNumber++
        if (lineNumber % 1000 == 0) logger.info("line number $lineNumber processed")
        val stats = it.next()
        try {
          if (stats.countType == TripleStatisticsType.typedEntity) break
          fkgTripleStatisticsDao.save(stats)
        } catch (e: Throwable) {
          logger.error("error in $stats", e)
        }
      }
    }
  }

  @Throws(Exception::class)
  fun fixWikiTemplateMapping(): Boolean {
    var page = 0
    val MULTI_SPACE_REGEX = Regex("\\s+")
    do {
      val pagedData = wikiTemplateRedirectDao.list(page = page++, pageSize = 100)
      pagedData.data.forEach {
        it.nameEn = it.nameEn!!.toLowerCase().replace("-", " ").replace(MULTI_SPACE_REGEX, " ").trim()
        wikiTemplateRedirectDao.save(it)
      }
    } while (pagedData.data.isNotEmpty())
    return true
  }

  @Throws(Exception::class)
  fun processTripleInputFiles(storeType: StoreType = StoreType.none) {

    val WIKI_DUMP_ARTICLE = "wiki.triple.input.folder"
    val config = ConfigReader.getConfig(mapOf(WIKI_DUMP_ARTICLE to "~/.pkg/data/triples"))
    val path = ConfigReader.getPath(config[WIKI_DUMP_ARTICLE]!! as String)
    Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
      throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }

    val store = when (storeType) {
      StoreType.file -> FileFkgTripleDaoImpl(path.resolve("mapped"))
      StoreType.mysql -> tripleDao
      StoreType.virtuoso -> VirtuosoFkgTripleDaoImpl()
      else -> null
    }

    // deletes all old triples
    store?.deleteAll()
    val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
    val startTime = System.currentTimeMillis()
    var tripleNumber = 0
    var numberOfEndedFiles = 0
    result.forEachIndexed { index, p ->
      Thread({
        eventDao.fileProcessed(p.toString())
        try {
          TripleJsonFileReader(p).use { reader ->
            while (reader.hasNext()) {
              try {
                eventDao.tripleRead()
                val data = reader.next()
                tripleNumber++
                if (data.templateType == null || data.templateNameFull == null) continue
                if (data.templateType != "infobox" && !data.templateType!!.startsWith("جعبه")) continue
                data.templateName = data.templateNameFull!!.substring(data.templateType!!.length + 1).trim()
//              val start = System.currentTimeMillis()
                eventDao.tripleProcessed()
                if (tripleNumber % 100 == 0)
                  logger.trace("triple number is $tripleNumber")

                if (tripleNumber % 1000 == 0)
                  logger.info("triple number is $tripleNumber. $index file is $p. time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")

                if (tripleNumber % 10000 == 0) {
                  logger.info("triple number is $tripleNumber. saving log")
                  saveLog(path)
                }
//              logger.info("1: " + (System.currentTimeMillis() - start))
                /**
                 * we like to change persian template name to english template name. because we have
                 * mapping for english template names in tables of
                 * template_property_mapping and dbpedia_property_mapping
                 */
                val templateRedirects = wikiTemplateRedirectDao.read(nameFa = data.templateName!!)
                val englishTemplateName =
                        if (templateRedirects.isNotEmpty()) {
                          logger.trace("I found it: " + data.templateName)
                          templateRedirects[0].nameEn!!
                        } else data.templateName!!
                logger.trace("english template type is $englishTemplateName (if we have english type)")
//              logger.info("2: " + (System.currentTimeMillis() - start))
                // replace URIs by prefixes
                val rawProperty = data.predicate!!
                data.subject = prefixService.replacePrefixes(data.subject!!)
                data.predicate = prefixService.replacePrefixes(data.predicate!!)
                data.predicate = PropertyNormaller.removeDigits(data.predicate!!)
                data.objekt = prefixService.replacePrefixes(data.objekt!!)

//              logger.info("3: " + (System.currentTimeMillis() - start))
                // template predicate is predicate without URI for example dbo:writer -> writer
                var templatePredicate: String
                if (data.predicate!!.contains(":"))
                  templatePredicate = data.predicate!!.substringAfter(':')
                else templatePredicate = data.predicate!!
                templatePredicate = PropertyNormaller.removeDigits(templatePredicate)
                logger.trace("template predicate is $templatePredicate")
//              logger.info("4: " + (System.currentTimeMillis() - start))
                /**
                 * we change template predicate to translated template predicate by table
                 * template_property_mapping
                 */
                val notTranslatedTemplatePredicate: String
                val templateMapping = wikiPropertyTranslationDao.readByEnTitle(englishTemplateName, templatePredicate, false)
                if (templateMapping.isNotEmpty()) {
                  notTranslatedTemplatePredicate = templatePredicate
                  templatePredicate = templateMapping[0].faProperty!!
                } else {
                  notTranslatedTemplatePredicate = templatePredicate
                }
                logger.trace("not translated template predicate is $notTranslatedTemplatePredicate")
//              logger.info("5: " + (System.currentTimeMillis() - start))
                val s = StoreData(store = store, rawProperty = rawProperty, data = data)
                if (!findMap(s, englishTemplateName, templatePredicate, notTranslatedTemplatePredicate)) {
//                logger.info("6: " + (System.currentTimeMillis() - start))
                  createTriple(s, null, MappingStatus.NotMapped)
                }
//              logger.info("7: " + (System.currentTimeMillis() - start))
              } catch (th: Throwable) {
                logger.error(th)
              }
            }
          }
        } catch (th: Throwable) {
          logger.error(th)
        } finally {
          numberOfEndedFiles++
        }
      }).start()
    }
    while (numberOfEndedFiles < result.size) Thread.sleep(10000)
    saveLog(path)
    println(eventDao.log())
  }

  private fun saveLog(path: Path) {
    synchronized(this) {
      Files.write(path.resolve("mapped").resolve("stats.txt"), eventDao.log().toByteArray(Charset.forName("UTF-8")))
    }
  }

  fun findMap(s: StoreData, englishTemplateType: String,
              templatePredicate: String, secondTemplatePredicate: String): Boolean {
    var map = mappingDao.search(page = 0, pageSize = 0, language = null, type = englishTemplateType,
            templateProperty = templatePredicate, secondTemplateProperty = secondTemplatePredicate).data
    /**
     * we may have two cases:
     * 1- when we haven't any mapping for template `language/type/property`
     * 2- we have more than one mapping for template `language/type/property`
     */
    if (map.isNotEmpty()) {
      // more than one mapping for template `language/type/property`
      if (map.size > 2) return false
      if (map.size > 1)
        logger.trace("multiple mapping for $englishTemplateType/${s.data.predicate}")
      val writtenMap = mutableSetOf<String>()
      for (m in map) {
        if (writtenMap.contains(m.ontologyProperty)) continue
        createTriple(s, m, null)
        writtenMap.add(m.ontologyProperty!!)
      }
      return true
    } else {
      /**
       * no mapping for template `language/type/property`,
       * we now search for `language/property` in any types.
       */
      map = mappingDao.search(page = 0, pageSize = 0, language = null, templateProperty = templatePredicate,
              secondTemplateProperty = secondTemplatePredicate, status = MappingStatus.Translated).data
      if (map.isNotEmpty()) {
        for (m in map) createTriple(s, m, MappingStatus.Translated)
        return true
      }
    }
    return false
  }

  data class StoreData(val store: FkgTripleDao?, val rawProperty: String, val data: TripleData)

  val MULTI_SPACES = Regex("\\s+")
  fun createTriple(triple: StoreData, mapping: FkgPropertyMapping?, status: MappingStatus?) {
    with(triple) {
      // predicate = ontology property if existed. data.predicate if not.
      var predicate =
              if (mapping != null && mapping.ontologyProperty != null)
                mapping.ontologyProperty
              else data.predicate

      // we add dbp to predicate if it is not a URL or prefix
      if (!predicate!!.contains(":") && !predicate.contains("//"))
        predicate = "dbp:" + PropertyNormaller.removeDigits(predicate).replace(" ", "_")

      try {
        val t = FkgTriple(
                source = data.source,
                subject = data.subject, predicate = predicate, objekt = data.objekt,
                status = status ?: mapping!!.status ?: MappingStatus.NotMapped, templateName = data.templateName,
                rawProperty = rawProperty,
                language = if (data.templateType == "infobox") "en" else "fa"
        )

        if (store is VirtuosoFkgTripleDaoImpl) {
          t.subject = prefixService.prefixToUri(t.subject)
          t.predicate = prefixService.prefixToUri(t.predicate)
          t.objekt = prefixService.prefixToUri(t.objekt)
        }

        store?.save(t)

        eventDao.propertyUsed(PropertyNormaller.removeDigits(t.rawProperty!!).replace(MULTI_SPACES, "_"))
        eventDao.statusGenerated(t.status!!)
        eventDao.typeUsed(t.templateName!!)
        eventDao.typeAndEntityUsed(t.templateName!!, t.subject!!)
        eventDao.typeAndPropertyUsed(t.templateName!!, t.predicate!!)

      } catch (e: Throwable) {
        logger.error("error create triple $data:", e)
      }
    }
  }
}