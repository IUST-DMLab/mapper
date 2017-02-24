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
      val result = PathWalker.getPath(path, Regex("infobox\\.json"))
      for (p in result) {
         TripleJsonFileReader(p).use { reader ->
            while (reader.hasNext()) {
               val data = reader.next()
               data.subject = prefixService.replacePrefixes(data.subject!!)
               data.predicate = prefixService.replacePrefixes(data.predicate!!)
               data.objekt = prefixService.replacePrefixes(data.objekt!!)

               var templatePredicate: String
               if (data.predicate!!.contains(":"))
                  templatePredicate = data.predicate!!.substringAfter(':')
               else templatePredicate = data.predicate!!

               val notTranslatedTemplatePredicate: String
               val templateMapping = mappingDao.readByEnTitle(data.infoboxType, templatePredicate)
               if (templateMapping.isNotEmpty()) {
                  notTranslatedTemplatePredicate = templatePredicate
                  templatePredicate = templateMapping[0].faProperty!!
               } else notTranslatedTemplatePredicate = templatePredicate

               if (!findMap("fa", data, templatePredicate))
                  if (!findMap("en", data, notTranslatedTemplatePredicate))
                     if (!checkCountAndAdd(data, notTranslatedTemplatePredicate))
                        if (!checkCountAndAdd(data, templatePredicate))
                           createTriple(data, null, MappingStatus.NotMapped)

            }
         }
      }
   }

   fun checkCountAndAdd(data: TripleData, templateProperty: String): Boolean {
      val list = mappingDao.readOntologyProperty(templateProperty.replace("_", " "))
      if (list.isNotEmpty() && list.size <= 2) {
         for (string in list) {
            data.predicate = string
            createTriple(data, null, MappingStatus.NotApproved)
         }
         return true
      }
      return false
   }

   fun findMap(language: String, data: TripleData, templatePredicate: String): Boolean {
      var map = mappingDao.read(language = language, type = data.infoboxType,
            templateProperty = templatePredicate)
      if (map.isNotEmpty()) {
         if (map.size > 1)
            logger.info("multiple mapping for $language/${data.infoboxType}/${data.predicate}")
         for (m in map) createTriple(data, m, null)
         return true
      } else {
         map = mappingDao.read(language = language, templateProperty = templatePredicate)
         if (map.isNotEmpty()) {
            for (m in map) createTriple(data, m, MappingStatus.NotApproved)
            return true
         }
      }
      return false
   }

   fun createTriple(data: TripleData, mapping: DBpediaPropertyMapping?,
                    status: MappingStatus?) {
      val predicate =
            if (mapping != null && mapping.ontologyProperty != null)
               mapping.ontologyProperty
            else data.predicate
      tripleDao.save(
            KnowledgeBaseTriple(
                  source = data.source,
                  subject = data.subject, predicate = predicate, objekt = data.objekt,
                  status = status ?: mapping!!.status
            ))
   }
}