package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

import ir.ac.iust.dml.kg.access.dao.*
import ir.ac.iust.dml.kg.access.dao.file.FileFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.memory.StatisticalEventDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.dbpediahelper.logic.PrefixService
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleData
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
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
      val path = ConfigReader.getPath("mapped.triple.stats.file", "~/.pkg/data/triples/mapped/stats.txt")
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      fkgTripleStatisticsDao.deleteAll()

      StatisticsLogReader(path).use {
         var lineNumber = 0
         while (it.hasNext()) {
            lineNumber++
            if (lineNumber % 1000 == 0) logger.trace("line number $lineNumber processed")
            val stats = it.next()
            try {
               fkgTripleStatisticsDao.save(stats)
            } catch (e: Throwable) {
               logger.error("error in $stats", e)
            }
         }
      }
   }

   @Throws(Exception::class)
   fun traverse(storeType: StoreType = StoreType.none) {
      val path = ConfigReader.getPath("wiki.triple.input.folder", "~/.pkg/data/triples")
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
      val result = PathWalker.getPath(path, Regex("\\d+\\.json"))
      for (p in result) {
         eventDao.fileProcessed(p.toString())
         var tripleNumber = 0
         try {
            TripleJsonFileReader(p).use { reader ->
               while (reader.hasNext()) {
                  try {
                     eventDao.tripleRead()
                     val data = reader.next()
                     tripleNumber++
                     if (data.templateName == null) continue
                     if (data.templateName != "infobox" && !data.templateName!!.startsWith("جعبه")) continue
                     eventDao.tripleProcessed()
                     if (tripleNumber % 100 == 0) {
                        logger.info("triple number is $tripleNumber")
                     }

                     if (tripleNumber % 10000 == 0) {
                        logger.info("triple number is $tripleNumber")
                        saveLog(path)
                     }

                     logger.info("data: $data")
                     /**
                      * we like to change persian template name to english template name. because we have
                      * mapping for english template names in tables of
                      * template_property_mapping and dbpedia_property_mapping
                      */
                     val templateRedirects = wikiTemplateRedirectDao.read(nameFa = data.templateType!!)
                     val englishTemplateType =
                           if (templateRedirects.isNotEmpty()) templateRedirects[0].typeEn!!
                           else data.templateType!!
                     logger.info("english template type is $englishTemplateType (if we have english type)")

                     // replace URIs by prefixes
                     val rawProperty = data.predicate!!
                     data.subject = prefixService.replacePrefixes(data.subject!!)
                     data.predicate = prefixService.replacePrefixes(data.predicate!!)
                     data.predicate = targetProperty(data.predicate!!)
                     data.objekt = prefixService.replacePrefixes(data.objekt!!)

                     // template predicate is predicate without URI for example dob:writer -> writer
                     var templatePredicate: String
                     if (data.predicate!!.contains(":"))
                        templatePredicate = data.predicate!!.substringAfter(':')
                     else templatePredicate = data.predicate!!
                     templatePredicate = targetProperty(templatePredicate)
                     logger.info("template predicate is $templatePredicate")

                     /**
                      * we change template predicate to translated template predicate by table
                      * template_property_mapping
                      */
                     val notTranslatedTemplatePredicate: String
                     val templateMapping = wikiPropertyTranslationDao.readByEnTitle(englishTemplateType, templatePredicate)
                     if (templateMapping.isNotEmpty()) {
                        notTranslatedTemplatePredicate = templatePredicate
                        templatePredicate = templateMapping[0].faProperty!!
                     } else {
                        notTranslatedTemplatePredicate = templatePredicate
                     }
                     logger.trace("not translated template predicate is $notTranslatedTemplatePredicate")

                     val s = StoreData(store = store, rawProperty = rawProperty, data = data)
                     if (!findMap(s, englishTemplateType, templatePredicate, notTranslatedTemplatePredicate))
                        createTriple(s, null, MappingStatus.NotMapped)

                  } catch (th: Throwable) {
                     logger.error(th)
                  }
               }
            }
         } catch (th: Throwable) {
            logger.error(th)
         }
      }
      saveLog(path)
      println(eventDao.log())
   }

   private fun saveLog(path: Path) {
      Files.write(path.resolve("mapped").resolve("stats.txt"), eventDao.log().toByteArray(Charset.forName("UTF-8")))
   }

   val DIGIT_END_REGEX = Regex("(\\w+)\\d+")
   fun targetProperty(property: String): String {
      var result = property.replace("_", " ")
      if (DIGIT_END_REGEX.matches(result))
         result = DIGIT_END_REGEX.matchEntire(result)!!.groups[1]!!.value
      return result
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
            logger.info("multiple mapping for $englishTemplateType/${s.data.predicate}")
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

   fun createTriple(triple: StoreData, mapping: FkgPropertyMapping?, status: MappingStatus?) {
      with(triple) {
         // predicate = ontology property if existed. data.predicate if not.
         var predicate =
               if (mapping != null && mapping.ontologyProperty != null)
                  mapping.ontologyProperty
               else data.predicate

         // we add dbp to predicate if it is not a URL or prefix
         if (!predicate!!.contains(":") && !predicate.contains("//"))
            predicate = "dbp:" + targetProperty(predicate)

         try {
            val t = FkgTriple(
                  source = data.source,
                  subject = data.subject, predicate = predicate, objekt = data.objekt,
                  status = status ?: mapping!!.status, templateType = data.templateType,
                  rawProperty = rawProperty,
                  language = if (data.templateName == "infobox") "en" else "fa"
            )

            if (store is VirtuosoFkgTripleDaoImpl) {
               t.subject = prefixService.prefixToUri(t.subject)
               t.predicate = prefixService.prefixToUri(t.predicate)
               t.objekt = prefixService.prefixToUri(t.objekt)
            }

            store?.save(t)

            eventDao.propertyUsed(t.predicate!!)
            eventDao.statusGenerated(t.status!!)
            eventDao.typeUsed(t.templateType!!)
            eventDao.typeAndEntityUsed(t.templateType!!, t.subject!!)
            eventDao.typeAndPropertyUsed(t.templateType!!, t.predicate!!)

         } catch (e: Throwable) {
            logger.error("error create triple $data:", e)
         }
      }
   }
}