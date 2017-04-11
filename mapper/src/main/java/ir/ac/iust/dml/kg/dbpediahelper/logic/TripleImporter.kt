package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgPropertyMappingDao
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.WikipediaTemplateRedirectDao
import ir.ac.iust.dml.kg.access.dao.file.FileFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
import ir.ac.iust.dml.kg.utils.PropertyNormaller
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import java.nio.file.Files
import javax.annotation.PreDestroy

@Service
class TripleImporter {

   val logger = Logger.getLogger(this.javaClass)!!
   @Autowired lateinit var tripleDao: FkgTripleDao
   @Autowired lateinit var mappingDao: FkgPropertyMappingDao
   @Autowired lateinit var wikiTemplateRedirectDao: WikipediaTemplateRedirectDao
   @Autowired lateinit var tripleGenerationTaskExecutor: ThreadPoolTaskExecutor

   @PreDestroy
   fun shutdown() {
      tripleGenerationTaskExecutor.shutdown()
   }

   enum class StoreType {
      none, file, mysql, virtuoso, knowledgeStore
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
//      StoreType.virtuoso -> VirtuosoFkgTripleDaoImpl()
         StoreType.knowledgeStore -> KnowledgeStoreFkgTripleDaoImpl()
         else -> null
      }

      // deletes all old triples
      store?.deleteAll()
      val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
      val startTime = System.currentTimeMillis()
      var tripleNumber = 0
      result.forEachIndexed { index, p ->
         tripleGenerationTaskExecutor.execute {
            TripleJsonFileReader(p).use { reader ->
               while (reader.hasNext()) {
                  val triple = reader.next()
                  tripleNumber++
                  try {
                     if (triple.templateType == null || triple.templateNameFull == null) continue
                     if (triple.templateType != "infobox" && !triple.templateType!!.startsWith("جعبه")) continue
                     triple.templateName = triple.templateNameFull!!.substring(triple.templateType!!.length + 1).trim()

                     if (tripleNumber % 1000 == 0)
                        logger.info("triple number is $tripleNumber. $index file is $p. " +
                              "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")

                     val predicate = PropertyNormaller.removeDigits(triple.predicate!!)
                     val mapping = mappingDao.read(templateName = triple.templateNameFull!!, nearTemplateNames = false,
                           templateProperty = predicate)
                     if (mapping != null) {
                        tripleDao.save(FkgTriple(
                              source = triple.source, subject = triple.source, predicate = mapping.ontologyProperty!!,
                              objekt = triple.objekt, status = mapping.status, language = mapping.language!!,
                              rawProperty = triple.predicate, templateName = triple.templateName
                        ), mapping)
                     } else
                        logger.error("Mapping not found for $triple. " +
                              "Did you write mappings to database by precessing stats??")

                  } catch (th: Throwable) {
                     logger.info("triple: $triple")
                     logger.error(th)
                  }
               }
            }
         }
      }
      do {
         Thread.sleep(10000)
      } while (tripleGenerationTaskExecutor.activeCount > 0)
   }
}