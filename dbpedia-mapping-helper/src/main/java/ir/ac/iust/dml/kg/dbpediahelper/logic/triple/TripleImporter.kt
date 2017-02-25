package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.DBpediaPropertyMappingDao
import ir.ac.iust.dml.kg.dbpediahelper.access.dao.KnowledgeBaseTripleDao
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.DBpediaPropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.KnowledgeBaseTriple
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.MappingStatus
import ir.ac.iust.dml.kg.dbpediahelper.logic.PrefixService
import ir.ac.iust.dml.kg.utils.ConfigReader
import ir.ac.iust.dml.kg.utils.PathWalker
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class TripleImporter {

   val logger = Logger.getLogger(this.javaClass)!!
   @Autowired
   lateinit var tripleDao: KnowledgeBaseTripleDao
   @Autowired
   lateinit var mappingDao: DBpediaPropertyMappingDao
   @Autowired
   lateinit var prefixService: PrefixService

   @Throws(Exception::class)
   fun traverse() {
      val WIKI_DUMP_ARTICLE = "wiki.triple.input.folder"
      val config = ConfigReader.getConfig(mapOf(WIKI_DUMP_ARTICLE to "~/.pkg/data/triples"))
      val path = ConfigReader.getPath(config[WIKI_DUMP_ARTICLE]!! as String)
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      tripleDao.deleteAll()
      val result = PathWalker.getPath(path, Regex("\\d+\\.json"))
      for (p in result) {
         var lineNumber = 0
         TripleJsonFileReader(p).use { reader ->
            while (reader.hasNext()) {
               val data = reader.next()
               lineNumber++
               if (data.templateType == null) continue
               if (data.templateType != "infobox"/* && !data.templateType!!.startsWith("جعبه")*/) continue
               if (lineNumber % 10000 == 0)
                  logger.trace("line number is $lineNumber")
               val rawProperty = data.predicate!!
               data.subject = prefixService.replacePrefixes(data.subject!!)
               data.predicate = prefixService.replacePrefixes(data.predicate!!)
               data.objekt = prefixService.replacePrefixes(data.objekt!!)

               var templatePredicate: String
               if (data.predicate!!.contains(":"))
                  templatePredicate = data.predicate!!.substringAfter(':')
               else templatePredicate = data.predicate!!

               data.predicate = targetProperty(data.predicate!!)
               templatePredicate = targetProperty(templatePredicate)

               val notTranslatedTemplatePredicate: String
               val templateMapping = mappingDao.readByEnTitle(data.infoboxType, templatePredicate)
               if (templateMapping.isNotEmpty()) {
                  notTranslatedTemplatePredicate = templatePredicate
                  templatePredicate = templateMapping[0].faProperty!!
               } else notTranslatedTemplatePredicate = templatePredicate

               if (!findMap(rawProperty, "fa", data, templatePredicate))
                  if (!findMap(rawProperty, "en", data, notTranslatedTemplatePredicate))
                     if (!checkCountAndAdd(rawProperty, data, notTranslatedTemplatePredicate))
                        if (!checkCountAndAdd(rawProperty, data, templatePredicate))
                           createTriple(rawProperty, data, null, MappingStatus.NotMapped)

            }
         }
      }
   }

   val DIGIT_END_REGEX = Regex("(\\w+)\\d+")
   fun targetProperty(property: String): String {
      var result = property.replace("_", " ")
      if (DIGIT_END_REGEX.matches(result))
         result = DIGIT_END_REGEX.matchEntire(result)!!.groups[1]!!.value
      return result
   }

   fun checkCountAndAdd(rawProperty: String, data: TripleData, templateProperty: String): Boolean {
      val list = mappingDao.readOntologyProperty(templateProperty)
      if (list.isNotEmpty() && list.size <= 2) {
         if (list.contains("dbo:" + rawProperty)) {
            data.predicate = "dbo:" + rawProperty
            createTriple(rawProperty, data, null, MappingStatus.NotApproved)
         } else
            for (string in list) {
               data.predicate = string
               createTriple(rawProperty, data, null, MappingStatus.Multiple)
            }
         return true
      }
      return false
   }

   fun findMap(rawProperty: String, language: String, data: TripleData, templatePredicate: String): Boolean {
      var map = mappingDao.read(language = language, type = data.infoboxType,
            templateProperty = templatePredicate)
      if (map.isNotEmpty()) {
         if (map.size > 2) return false
         if (map.size > 1)
            logger.info("multiple mapping for $language/${data.infoboxType}/${data.predicate}")
         val writtenMap = mutableSetOf<String>()
         for (m in map) {
            if (writtenMap.contains(m.ontologyProperty)) continue
            createTriple(rawProperty, data, m, null)
            writtenMap.add(m.ontologyProperty!!)
         }
         return true
      } else {
         map = mappingDao.read(language = language, templateProperty = templatePredicate)
         if (map.isNotEmpty()) {
            if (map.size > 2) return false
            val writtenMap = mutableSetOf<String>()
            for (m in map) {
               if (writtenMap.contains(m.ontologyProperty)) continue
               createTriple(rawProperty, data, m, MappingStatus.Multiple)
               writtenMap.add(m.ontologyProperty!!)
            }
            return true
         }
      }
      return false
   }

   fun createTriple(rawProperty: String, data: TripleData, mapping: DBpediaPropertyMapping?,
                    status: MappingStatus?) {
      var predicate =
            if (mapping != null && mapping.ontologyProperty != null)
               mapping.ontologyProperty
            else data.predicate
      if (!predicate!!.contains(":") && !predicate.contains("//"))
         predicate = "dbp:" + targetProperty(predicate)

      try {
         tripleDao.save(
               KnowledgeBaseTriple(
                     source = data.source,
                     subject = data.subject, predicate = predicate, objekt = data.objekt,
                     status = status ?: mapping!!.status, templateType = data.infoboxType,
                     rawProperty = rawProperty,
                     language = if (data.templateType == "infobox") "en" else "fa"
               ))
      } catch (e: Throwable) {
         logger.error("error create triple $data:", e)
      }
   }
}