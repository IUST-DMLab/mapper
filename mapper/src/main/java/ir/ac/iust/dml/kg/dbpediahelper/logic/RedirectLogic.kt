package ir.ac.iust.dml.kg.dbpediahelper.logic

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.dbpediahelper.logic.test.TestUtils
import ir.ac.iust.dml.kg.dbpediahelper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.PrefixService
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
  @Autowired private lateinit var tripleDao: FkgTripleDao

  class Ambiguity {
    var title: String? = null
    val field: MutableList<String> = mutableListOf()
  }

  @Throws(Exception::class)
  fun write(storeType: StoreType = StoreType.knowledgeStore) {
    val redirectsFolder = ConfigReader.getPath("wiki.folder.redirects", "~/.pkg/data/redirects")
    if (!Files.exists(redirectsFolder.parent)) Files.createDirectories(redirectsFolder.parent)
    if (!Files.exists(redirectsFolder)) {
      throw Exception("There is no file ${redirectsFolder.toAbsolutePath()} existed.")
    }

    val store = when (storeType) {
      StoreType.mysql -> tripleDao
      StoreType.virtuoso -> VirtuosoFkgTripleDaoImpl()
      else -> KnowledgeStoreFkgTripleDaoImpl()
    }

    val gson = Gson()
    val type = object : TypeToken<Map<String, String>>() {}.type

    val maxNumberOfRedirects = TestUtils.getMaxTuples()

    val VARIANT_LABEL = PrefixService.prefixToUri(PrefixService.VARIANT_LABEL_URL)
    val REDIRECT = PrefixService.prefixToUri(PrefixService.REDIRECTS_URI)

    val files = PathWalker.getPath(redirectsFolder, Regex("[01]-redirects.json"))
    var i = 0
    files.forEach {
      try {
        BufferedReader(InputStreamReader(FileInputStream(it.toFile()), "UTF8")).use { reader ->
          val map: Map<String, String> = gson.fromJson(reader, type)
          map.forEach { t, u ->
            i++
            if (i < maxNumberOfRedirects) {
              if (i % 1000 == 0) logger.info("writing redirect $i: $t to $u")
              store.save(FkgTriple(
                  subject = PrefixService.getFkgResourceUrl(u),
                  predicate = REDIRECT,
                  objekt = "http://fa.wikipedia.org/wiki/" + t.replace(' ', '_')
              ), null, true)
              store.save(FkgTriple(
                  subject = PrefixService.getFkgResourceUrl(u),
                  predicate = VARIANT_LABEL,
                  objekt = t.replace('_', ' ')
              ), null, true)
            }
          }
        }
      } catch (th: Throwable) {
        logger.error(th)
        th.printStackTrace()
      }
    }

    if (store is KnowledgeStoreFkgTripleDaoImpl) store.flush()
    if (store is VirtuosoFkgTripleDaoImpl) store.close()
  }
}