package ir.ac.iust.dml.kg.mapper.logic

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.mapper.logic.utils.TestUtils
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.Module
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Files


@Service
class RedirectLogic {

  val logger = Logger.getLogger(this.javaClass)!!
  @Autowired lateinit var storeProvider: StoreProvider

  class Ambiguity {
    var title: String? = null
    val field: MutableList<String> = mutableListOf()
  }

  @Throws(Exception::class)
  fun write(version: Int, storeType: StoreType = StoreType.knowledgeStore) {
    val redirectsFolder = ConfigReader.getPath("wiki.folder.redirects", "~/.pkg/data/redirects")
    if (!Files.exists(redirectsFolder.parent)) Files.createDirectories(redirectsFolder.parent)
    if (!Files.exists(redirectsFolder)) {
      throw Exception("There is no file ${redirectsFolder.toAbsolutePath()} existed.")
    }

    val store = storeProvider.getStore(storeType)

    val gson = Gson()
    val type = object : TypeToken<Map<String, String>>() {}.type

    val maxNumberOfRedirects = TestUtils.getMaxTuples()

    val files = PathWalker.getPath(redirectsFolder, Regex("[01]-redirects.json"))
    var i = 0
    files.forEach {
      try {
        BufferedReader(InputStreamReader(FileInputStream(it.toFile()), "UTF8")).use { reader ->
          val map: Map<String, String> = gson.fromJson(reader, type)
          map.forEach { redirect, mainResource ->
            i++
            if (i < maxNumberOfRedirects) {
              if (i % 1000 == 0) logger.info("writing redirect $i: $redirect to $mainResource")
              store.save(FkgTriple(
                  subject = URIs.getFkgResourceUri(mainResource),
                  predicate = URIs.redirect,
                  objekt = "http://fa.wikipedia.org/wiki/" + redirect.replace(' ', '_')
              ), Module.wiki.name, version, null, true)
              store.save(FkgTriple(
                  subject = URIs.getFkgResourceUri(mainResource),
                  predicate = URIs.variantLabel,
                  objekt = redirect.replace('_', ' ')
              ), Module.wiki.name, version, null, true)
            }
          }
        }
      } catch (th: Throwable) {
        logger.error(th)
        th.printStackTrace()
      }
    }

    store.flush()
  }
}