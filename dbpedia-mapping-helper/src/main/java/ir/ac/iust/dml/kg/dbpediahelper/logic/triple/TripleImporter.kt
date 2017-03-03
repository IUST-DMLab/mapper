package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.DBpediaPropertyMappingDao
import ir.ac.iust.dml.kg.dbpediahelper.access.dao.KnowledgeBaseTripleDao
import ir.ac.iust.dml.kg.dbpediahelper.access.dao.StatisticalEventDao
import ir.ac.iust.dml.kg.dbpediahelper.access.dao.TemplateMappingDao
import ir.ac.iust.dml.kg.dbpediahelper.access.dao.file.FileKnowledgeBaseTripleDaoImpl
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.DBpediaPropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.KnowledgeBaseTriple
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.MappingStatus
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
   @Autowired
   lateinit var tripleDao: KnowledgeBaseTripleDao
   @Autowired
   lateinit var mappingDao: DBpediaPropertyMappingDao
   @Autowired
   lateinit var templateMappingDao: TemplateMappingDao
   @Autowired
   lateinit var prefixService: PrefixService
   @Autowired
   lateinit var event: StatisticalEventDao

   public enum class StoreType {
      none, file, mysql
   }

   @Throws(Exception::class)
   fun traverse(storeType: StoreType = StoreType.none) {

      val WIKI_DUMP_ARTICLE = "wiki.triple.input.folder"
      val config = ConfigReader.getConfig(mapOf(WIKI_DUMP_ARTICLE to "~/.pkg/data/triples"))
      val path = ConfigReader.getPath(config[WIKI_DUMP_ARTICLE]!! as String)
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      val store = when (storeType) {
         StoreType.file -> FileKnowledgeBaseTripleDaoImpl(path.resolve("mapped"))
         StoreType.mysql -> tripleDao
         else -> null
      }

      // deletes all old triples
      store?.deleteAll()
      val result = PathWalker.getPath(path, Regex("\\d+\\.json"))
      for (p in result) {
         event.fileProcessed(p.toString())
         var tripleNumber = 0
         TripleJsonFileReader(p).use { reader ->
            while (reader.hasNext()) {
               try {
                  event.tripleRead()
                  val data = reader.next()
                  tripleNumber++
                  if (data.templateName == null) continue
                  if (data.templateName != "infobox" && !data.templateName!!.startsWith("جعبه")) continue
                  event.tripleProcessed()
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
                  val templateMappings = templateMappingDao.read(nameFa = data.templateType!!)
                  val englishTemplateType =
                        if (templateMappings.isNotEmpty()) templateMappings[0].typeEn!!
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
                  val templateMapping = mappingDao.readByEnTitle(englishTemplateType, templatePredicate)
                  if (templateMapping.isNotEmpty()) {
                     notTranslatedTemplatePredicate = templatePredicate
                     templatePredicate = templateMapping[0].faProperty!!
                  } else {
                     notTranslatedTemplatePredicate = templatePredicate
                  }
                  logger.trace("not translated template predicate is $notTranslatedTemplatePredicate")

                  // main part of algorithm is:
                  // 1- checking for exact template language/type/property mapping
                  // 2- checking for template language/*/property mapping
                  // 3- looking for not translated template predicate in database and use its mapping if existed
                  // 4- looking for translated template predicate in database and use its mapping if existed

                  val s = StoreData(store = store, rawProperty = rawProperty, data = data)
                  if (!findMap(s, englishTemplateType, templatePredicate, notTranslatedTemplatePredicate))
//                  if (!findMap(s, "en", englishTemplateType, notTranslatedTemplatePredicate))
//                     if (!checkCountAndAdd(s, notTranslatedTemplatePredicate))
//                        if (!checkCountAndAdd(s, templatePredicate))
                     createTriple(s, null, MappingStatus.NotMapped)

               } catch (th: Throwable) {
                  logger.error(th)
               }
            }
         }
      }
      saveLog(path)
      println(event.log())
   }

   private fun saveLog(path: Path) {
      Files.write(path.resolve("mapped").resolve("stats.txt"), event.log().toByteArray(Charset.forName("UTF-8")))
   }

   val DIGIT_END_REGEX = Regex("(\\w+)\\d+")
   fun targetProperty(property: String): String {
      var result = property.replace("_", " ")
      if (DIGIT_END_REGEX.matches(result))
         result = DIGIT_END_REGEX.matchEntire(result)!!.groups[1]!!.value
      return result
   }

//   fun checkCountAndAdd(s: StoreData, templateProperty: String): Boolean {
//      val list = mappingDao.readOntologyProperty(templateProperty)
//      // we have less than two mappings for template property. in almost all cases we have a mapping
//      if (list.isNotEmpty() && list.size <= 2) {
//         logger.info("we have less than two mappings for template property $templateProperty")
//         if (list.contains("dbo:" + s.rawProperty)) {
//            s.data.predicate = "dbo:" + s.rawProperty
//            createTriple(s, null, MappingStatus.NotApproved)
//         } else
//            for (string in list) {
//               s.data.predicate = string
//               createTriple(s, null, MappingStatus.Multiple)
//            }
//         return true
//      }
//      return false
//   }

   fun findMap(s: StoreData, englishTemplateType: String,
               templatePredicate: String, secondTemplatePredicate: String): Boolean {
      var map = mappingDao.read(language = null, type = englishTemplateType,
            templateProperty = templatePredicate, secondTemplateProperty = secondTemplatePredicate)
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
         map = mappingDao.read(language = null, templateProperty = templatePredicate,
               secondTemplateProperty = secondTemplatePredicate, status = MappingStatus.Translated)
         if (map.isNotEmpty()) {
            for (m in map) createTriple(s, m, MappingStatus.Translated)
            return true
         }
      }
      return false
   }

   data class StoreData(val store: KnowledgeBaseTripleDao?, val rawProperty: String, val data: TripleData)

   fun createTriple(triple: StoreData, mapping: DBpediaPropertyMapping?, status: MappingStatus?) {
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
            val t = KnowledgeBaseTriple(
                  source = data.source,
                  subject = data.subject, predicate = predicate, objekt = data.objekt,
                  status = status ?: mapping!!.status, templateType = data.templateType,
                  rawProperty = rawProperty,
                  language = if (data.templateName == "infobox") "en" else "fa"
            )

            store?.save(t)

            event.propertyUsed(t.predicate!!)
            event.statusGenerated(t.status!!)
            event.typeUsed(t.templateType!!)
            event.typeAndEntityUsed(t.templateType!!, t.subject!!)
            event.typeAndPropertyUsed(t.templateType!!, t.predicate!!)

         } catch (e: Throwable) {
            logger.error("error create triple $data:", e)
         }
      }
   }
}