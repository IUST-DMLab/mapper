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
import ir.ac.iust.dml.kg.raw.utils.PageUtils
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import java.io.*
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.min

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
      if (validate && !TripleFixer.fix(t)) return
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
      th.printStackTrace()
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
        FileInputStream(subjectPath.toFile()).use {
          InputStreamReader(it, "UTF-8").use {
            BufferedReader(it).use {
              val l: List<FkgTriple> = gson.fromJson(it, type)
              oldList.addAll(l)
            }
          }
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

  private val currentFiles = mutableListOf<Path>()
  /**
   * size of return objects are not working properly. page and page size is
   * used in order of file (subject) not order of triples.
   */
  override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
    if (currentFiles.isEmpty())
      currentFiles.addAll(PathWalker.getPath(path, Regex(".*\\.json")))
    val start = page * pageSize
    if (start >= currentFiles.size) return PageUtils.asPages(page, pageSize, mutableListOf())
    val allSubjectsList = mutableListOf<FkgTriple>()
    for (i in start until (min(pageSize * (page + 1), currentFiles.size)))
      FileInputStream(currentFiles[i].toFile()).use {
        InputStreamReader(it, "UTF-8").use {
          BufferedReader(it).use {
            try {
              val subjectList: List<FkgTriple> = gson.fromJson(it, type)
              allSubjectsList.addAll(subjectList)
            } catch (th: Throwable) {
            }
          }
        }
      }
    val totalSize = currentFiles.size.toLong()
    val pageCount = (totalSize / pageSize) + (if (totalSize % pageSize == 0L) 0 else 1)
    return PagedData(allSubjectsList, page, pageSize, pageCount, totalSize)
  }

  override fun read(subject: String?, predicate: String?, objekt: String?): MutableList<FkgTriple> {
    val subjectPath = getPath(subject!!) ?: return mutableListOf()
    val oldList = mutableListOf<FkgTriple>()
    if (Files.exists(subjectPath)) {
      FileInputStream(subjectPath.toFile()).use {
        InputStreamReader(it, "UTF-8").use {
          BufferedReader(it).use {
            val l: List<FkgTriple> = gson.fromJson(it, type)
            oldList.addAll(l)
          }
        }
      }
    }
    return oldList.filter {
      it.predicate == predicate
          && it.objekt == objekt
    }.toMutableList()
  }

}