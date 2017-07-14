package ir.ac.iust.dml.kg.mapper.logic.dump

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.Closeable
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Path

class EntityDataDumpReader(path: Path) : Iterator<EntityData>, Closeable {

   private val logger = Logger.getLogger(this.javaClass)!!
   private val reader = BufferedReader(InputStreamReader(FileInputStream(path.toFile()), "UTF8"))
   private var last: EntityData? = null
   private val gson = Gson()
   private val stringList = object : TypeToken<List<String>>() {}.type

   init {
      last = fetchNext()
   }

   override fun hasNext(): Boolean {
      return last != null
   }

   override fun next(): EntityData {
      val old = last!!
      last = fetchNext()
      return old
   }

   private fun fetchNext(): EntityData? {
      val buffer = StringBuffer()
      val data = EntityData()
      while (true) {
         val line = reader.readLine() ?: break
         if (line.contains(":")) {
            data.entityName = line.trim().substringBefore(':').substring(1).substringBeforeLast('"')
            buffer.setLength(0)
            buffer.append(line.substringAfter(':').trim())
            continue
         }
         if (line.contains("],")) {
            val text = buffer.append(line.substringBefore(",")).toString()
            try {
               data.infoboxes = gson.fromJson(text, stringList)
               if (data.entityName != null) return data
            } catch (e: Throwable) {
               logger.error(e)
            }
            buffer.setLength(0)
         } else buffer.append(line)
      }
      return null
   }

   override fun close() {
      try {
         reader.close()
      } catch (e: Throwable) {
         logger.error("ir couldn't close entity information dump file", e)
      }
   }

}