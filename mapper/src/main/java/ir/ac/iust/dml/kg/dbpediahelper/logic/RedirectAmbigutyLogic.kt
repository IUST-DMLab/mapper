package ir.ac.iust.dml.kg.dbpediahelper.logic

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.utils.PrefixService
import org.apache.log4j.Logger
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Files


@Service
class RedirectAmbigutyLogic {

   val logger = Logger.getLogger(this.javaClass)!!
   val knowledgeStoreDao = KnowledgeStoreFkgTripleDaoImpl()

   class Ambiguity {
      var title: String? = null
      val field: MutableList<String> = mutableListOf()
   }

   @Throws(Exception::class)
   fun write() {
      val redirectsFolder = ConfigReader.getPath("extractor.redirect.folder", "~/.pkg/data/redirects")
      Files.createDirectories(redirectsFolder.parent)
      if (!Files.exists(redirectsFolder)) {
         throw Exception("There is no file ${redirectsFolder.toAbsolutePath()} existed.")
      }

      val disambiguationFolder = ConfigReader.getPath("extractor.disambiguations.folder", "~/.pkg/data/disambiguations")
      Files.createDirectories(disambiguationFolder.parent)
      if (!Files.exists(disambiguationFolder)) {
         throw Exception("There is no file ${disambiguationFolder.toAbsolutePath()} existed.")
      }

      val gson = Gson()
      var type = object : TypeToken<Map<String, String>>() {}.type

      val maxNumberOfRedirects = ConfigReader.getInt("test.mode.max.redirects", "10000000")
      val maxNumberOfDisambiguation = ConfigReader.getInt("test.mode.max.disambiguation", "10000000")

      var files = PathWalker.getPath(redirectsFolder)
      var i = 0
      files.forEach {
         try {
            BufferedReader(InputStreamReader(FileInputStream(it.toFile()), "UTF8")).use { reader ->
               val map: Map<String, String> = gson.fromJson(reader, type)
               map.forEach { t, u ->
                  i++
                  if (i < maxNumberOfRedirects) {
                     if (i % 1000 == 0) logger.info("writing redirect $i: $t to $u")
                     knowledgeStoreDao.save(FkgTriple(
                           subject = PrefixService.getFkgResourceUrl(u),
                           predicate = "dbo:wikiPageRedirects",
                           objekt = t
                     ), null)
                  }
               }
            }
         } catch (th: Throwable) {
            logger.error(th)
         }
      }

      type = object : TypeToken<List<Ambiguity>>() {}.type
      files = PathWalker.getPath(disambiguationFolder)
      i = 0
      files.forEach {
         try {
            BufferedReader(InputStreamReader(FileInputStream(it.toFile()), "UTF8")).use { reader ->
               val map: List<Ambiguity> = gson.fromJson(reader, type)
               map.forEach { a ->
                  a.field.forEach { f ->
                     i++
                     if (i < maxNumberOfDisambiguation) {
                        if (i % 1000 == 0) logger.info("writing disambiguation $i: $a to $f")
                        knowledgeStoreDao.save(FkgTriple(
                              subject = PrefixService.getFkgResourceUrl(f),
                              predicate = "dbo:wikiDisambiguatedFrom",
                              objekt = a.title
                        ), null)
                     }
                  }
               }
            }
         } catch (th: Throwable) {
            logger.error(th)
         }
      }

   }
}