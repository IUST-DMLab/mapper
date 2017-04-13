package ir.ac.iust.dml.kg.dbpediahelper.logic

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
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

      val disambiguatyFolder = ConfigReader.getPath("extractor.disambiguations.folder", "~/.pkg/data/disambiguations")
      Files.createDirectories(disambiguatyFolder.parent)
      if (!Files.exists(disambiguatyFolder)) {
         throw Exception("There is no file ${disambiguatyFolder.toAbsolutePath()} existed.")
      }

      val gson = Gson()
      var type = object : TypeToken<Map<String, String>>() {}.type

      var files = PathWalker.getPath(redirectsFolder)
      files.forEach {
         try {
            BufferedReader(InputStreamReader(FileInputStream(it.toFile()), "UTF8")).use { reader ->
               val map: Map<String, String> = gson.fromJson(reader, type)
               map.forEach { t, u ->
                  logger.info("writing redirect $t to $u")
                  knowledgeStoreDao.save(FkgTriple(
                        subject = "http://fa.wikipedia.org/wiki/${u.replace(' ', '_')}",
                        predicate = "dbo:wikiPageRedirects",
                        objekt = t
                  ), null)
               }
            }
         } catch (th: Throwable) {
            logger.error(th)
         }
      }

      type = object : TypeToken<List<Ambiguity>>() {}.type
      files = PathWalker.getPath(disambiguatyFolder)
      files.forEach {
         try {
            BufferedReader(InputStreamReader(FileInputStream(it.toFile()), "UTF8")).use { reader ->
               val map: List<Ambiguity> = gson.fromJson(reader, type)
               map.forEach { a ->
                  a.field.forEach { f ->
                     knowledgeStoreDao.save(FkgTriple(
                           subject = "http://fa.wikipedia.org/wiki/${f.replace(' ', '_')}",
                           predicate = "dbo:wikiDisambiguityFrom",
                           objekt = a.title
                     ), null)
                  }
               }
            }
         } catch (th: Throwable) {
            logger.error(th)
         }
      }

   }
}