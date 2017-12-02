/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.dao.file

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.TripleFixer
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import java.io.*
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path

class FileFkgTripleDaoImpl(private val path: Path, private val flushSize: Int = 100) : FkgTripleDao() {
  private val logger = Logger.getLogger(this.javaClass)!!

  override fun newVersion(module: String) = 1

  override fun activateVersion(module: String, version: Int) = true

  init {
    if (!Files.exists(path)) Files.createDirectories(path)
  }

  private var notFlushedTriples = mutableMapOf<String, MutableList<FkgTriple>>()
  private var gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation()
      .setPrettyPrinting().disableHtmlEscaping().create()
  private val type = object : TypeToken<List<FkgTriple>>() {}.type!!

  override fun save(t: FkgTriple) {
    synchronized(notFlushedTriples) {
      if (!TripleFixer.fix(t)) return
      notFlushedTriples.getOrPut(t.subject!!, { mutableListOf() }).add(t)
      if (notFlushedTriples.size > flushSize) {
        flush()
      }
    }
  }

  private val prefixedUriSplicer = Regex("[:/]")
  private fun getPath(uri: String): Path? {
    val prefixedUri = URIs.replaceAllPrefixesInString(uri)
    var subjectPath: Path
    try {
      if (prefixedUri != uri && prefixedUri != null) {
        val parts = prefixedUri.split(prefixedUriSplicer)
        subjectPath = path.resolve(parts[0])
        val l = parts.last()
        subjectPath = subjectPath.resolve(if (l.length > 1) l.substring(0, 2) else l.substring(0, 1))
        for (i in 1 until parts.size - 1) subjectPath = subjectPath.resolve(parts[i])
        subjectPath = subjectPath.resolve(parts.last() + ".json")
      } else {
        subjectPath = path.resolve("no-prefix").resolve(URLEncoder.encode(uri, "UTF-8") + ".json")
      }
    } catch (th: Throwable) {
      subjectPath = path.resolve("error").resolve(URLEncoder.encode(uri, "UTF-8") + ".json")
    }
    return try {
      val subjectFolder = subjectPath.toAbsolutePath().parent
      if (!Files.exists(subjectFolder)) Files.createDirectories(subjectFolder)
      subjectPath
    } catch (th: Throwable) {
      logger.error(th)
      null
    }
  }

  override fun flush() {
    notFlushedTriples.forEach { subject, triples ->
      val subjectPath = getPath(subject) ?: return@forEach
      val oldList = mutableListOf<FkgTriple>()
      if (Files.exists(subjectPath)) {
        BufferedReader(InputStreamReader(FileInputStream(subjectPath.toFile()), "UTF8")).use {
          val l: List<FkgTriple> = gson.fromJson(it, type)
          oldList.addAll(l)
        }
      }
      oldList.addAll(triples)
      try {
        FileOutputStream(subjectPath.toFile()).use {
          OutputStreamWriter(it, "UTF-8").use {
            BufferedWriter(it).use {
              logger.trace("writing $subjectPath")
              gson.toJson(oldList, it)
            }
          }
        }
      } catch (th: Throwable) {
        logger.error(th)
      }
    }
    notFlushedTriples.clear()
  }

  override fun deleteAll() {
    FileUtils.deleteDirectory(path.toFile())
    Files.createDirectories(path)
  }

  override fun delete(subject: String, predicate: String, `object`: String) {
    TODO("not implemented")
  }

  override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
    throw UnsupportedOperationException("not implemented")
  }

  override fun read(subject: String?, predicate: String?, objekt: String?): MutableList<FkgTriple> {
    throw UnsupportedOperationException("not implemented")
  }

}