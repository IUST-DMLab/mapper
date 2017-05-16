package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgPropertyMappingDao
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.WikipediaTemplateRedirectDao
import ir.ac.iust.dml.kg.access.dao.file.FileFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import ir.ac.iust.dml.kg.raw.utils.PropertyNormaller
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
//import ir.ac.iust.dml.kg.services.client.ApiException
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
         StoreType.virtuoso -> VirtuosoFkgTripleDaoImpl()
         else -> KnowledgeStoreFkgTripleDaoImpl()
      }

      val maxNumberOfTriples = ConfigReader.getInt("test.mode.max.triples", "10000000")

      store.deleteAll()
      val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
      val startTime = System.currentTimeMillis()
      var tripleNumber = 0
      result.forEachIndexed { index, p ->
         TripleJsonFileReader(p).use { reader ->
            while (reader.hasNext()) {
               val triple = reader.next()
               tripleNumber++
               if (tripleNumber > maxNumberOfTriples) break
               try {
                  if (triple.templateType == null || triple.templateNameFull == null) continue
                  if (triple.templateType != "infobox" && !triple.templateType!!.startsWith("جعبه")) continue
                  triple.templateName = triple.templateNameFull!!.substring(triple.templateType!!.length + 1).trim()

                  if (triple.objekt!!.startsWith("fa.wikipedia.org/wiki"))
                     triple.objekt = "http://" + triple.objekt

                  if (tripleNumber % 1000 == 0)
                     logger.warn("triple number is $tripleNumber. $index file is $p. " +
                           "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")

                  val predicate = PropertyNormaller.removeDigits(triple.predicate!!)
                  val mapping = mappingDao.read(
                        templateName = PropertyNormaller.removeDigits(triple.templateNameFull!!),
                        nearTemplateNames = false, templateProperty = predicate)
                  if (mapping != null
                        && mapping.status != MappingStatus.Multiple
                        && mapping.ontologyProperty != null
                        && mapping.ontologyProperty!!.contains(":")) {
                     if (mapping.status == null && mapping.language == "en") mapping.status = MappingStatus.Mapped
                     store.save(FkgTriple(
                           source = triple.source,
                         subject = PrefixService.convertFkgResourceUrl(triple.source!!),
                           predicate = PrefixService.prefixToUri(
                                 mapping.ontologyProperty!!.replace("dbo:", PrefixService.KG_ONTOLOGY_PREFIX + ":")
                           ),
                         objekt = PrefixService.convertFkgResourceUrl(triple.objekt!!),
                           status = mapping.status,
                           language = mapping.language!!,
                           rawProperty = triple.predicate, templateName = triple.templateName
                     ), mapping)
                  } else {
                     store.save(FkgTriple(
                           source = triple.source,
                         subject = PrefixService.convertFkgResourceUrl(triple.source!!),
                           predicate = PrefixService.convertFkgProperty(triple.predicate!!),
                         objekt = PrefixService.convertFkgResourceUrl(triple.objekt!!),
                           status = MappingStatus.AutoGenerated, language = "fa",
                           rawProperty = triple.predicate, templateName = triple.templateName
                     ), null)
                     logger.trace("$predicate: Mapping not found for $triple. " +
                           "Did you write mappings to database by precessing stats??")
                  }

               } catch (th: Throwable) {
                  logger.info("triple: $triple")
                  logger.error(th)
               }
            }
         }
      }
      if (store is KnowledgeStoreFkgTripleDaoImpl) store.flush()
      if (store is VirtuosoFkgTripleDaoImpl) store.close()
   }
}