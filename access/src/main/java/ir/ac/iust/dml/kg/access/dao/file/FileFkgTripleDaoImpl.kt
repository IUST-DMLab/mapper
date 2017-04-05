package ir.ac.iust.dml.kg.access.dao.file

import com.google.gson.GsonBuilder
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.PagedData
import org.apache.commons.io.FileUtils
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path

class FileFkgTripleDaoImpl(val path: Path, val flushSize: Int = 1000) : FkgTripleDao {

   init {
      if (!Files.exists(path)) Files.createDirectories(path)
   }

   var fileIndex = 0;
   var notFlushedTriples = mutableListOf<FkgTriple>()
   var gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

   override fun save(t: FkgTriple) {
      notFlushedTriples.add(t)
      synchronized(notFlushedTriples) {
         if (notFlushedTriples.size > flushSize) {
            val p = path.resolve("${fileIndex / 100}").resolve("$fileIndex.json")
            if (!Files.exists(p.parent)) Files.createDirectories(p.parent)
            gson.toJson(notFlushedTriples, BufferedWriter(OutputStreamWriter(
                  FileOutputStream(p.toFile()), "UTF-8")))
            notFlushedTriples.clear()
            fileIndex++
         }
      }
   }

   override fun deleteAll() {
      FileUtils.deleteDirectory(path.toFile())
      Files.createDirectories(path)
   }

   override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
      throw UnsupportedOperationException("not implemented")
   }

   override fun read(subject: String?, predicate: String?, objekt: String?, status: MappingStatus?): MutableList<FkgTriple> {
      throw UnsupportedOperationException("not implemented")
   }

}