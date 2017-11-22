/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.dao.file

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.util.UriEncoder
import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.raw.utils.PagedData
import org.apache.commons.io.FileUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.Path

class FileFkgTripleDaoImpl(val path: Path, private val flushSize: Int = 10) : FkgTripleDao() {

  override fun newVersion(module: String) = 1

  override fun activateVersion(module: String, version: Int) = true

  init {
    if (!Files.exists(path)) Files.createDirectories(path)
  }

  private var notFlushedTriples = mutableMapOf<String, MutableList<FkgTriple>>()
  private var gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
  private val type = object : TypeToken<List<FkgTriple>>() {}.type!!

  override fun save(t: FkgTriple) {
    synchronized(notFlushedTriples) {
      notFlushedTriples.getOrPut(t.subject!!, { mutableListOf() }).add(t)
      if (notFlushedTriples.size > flushSize) {
        flush()
      }
    }
  }

  override fun flush() {

    notFlushedTriples.forEach { subject, triples ->
      val subjectName = subject.substringAfterLast('/')
      val folder = if (subjectName.length > 1) subjectName.substring(0, 2) else subjectName
      val subjectFolder = path.resolve(folder)
      if (!Files.exists(subjectFolder)) Files.createDirectories(subjectFolder)
      val subjectFile = subjectFolder.resolve(UriEncoder.encode(subject) + ".json")
      val oldList = mutableListOf<FkgTriple>()
      if (Files.exists(subjectFile)) {
        BufferedReader(InputStreamReader(FileInputStream(subjectFile.toFile()), "UTF8")).use {
          val l: List<FkgTriple> = gson.fromJson(it, type)
          oldList.addAll(l)
        }
      }
      oldList.addAll(triples)
      gson.toJson(oldList, BufferedWriter(OutputStreamWriter(
          FileOutputStream(subjectFile.toFile()), "UTF-8")))
    }
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