package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

import com.google.gson.Gson
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.Closeable
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Path

class TripleJsonFileReader(path: Path) : Iterator<TripleData>, Closeable {

   val logger = Logger.getLogger(this.javaClass)!!
   val gson = Gson()
   val reader = BufferedReader(InputStreamReader(FileInputStream(path.toFile()), "UTF8"))
   var lastTriples: TripleData? = null

   init {
      lastTriples = fetchNextTriples()
   }

   private fun fetchNextTriples(): TripleData? {
      val buffer = StringBuffer()
      var started = false
      while (true) {
         val line = reader.readLine() ?: break
         if (line.trim().startsWith("{")) started = true
         else {
            if (started && line.contains("}")) {
               try {
                  return gson.fromJson("{" + buffer.toString() + "}", TripleData::class.java)
               } catch (e: Throwable) {
                  return TripleData(templateName = "incomplete")
               }
            }
            else if (started) buffer.append(line)
         }
      }
      return null
   }

   override fun close() {
      try {
         reader.close()
      } catch (e: Throwable) {
         logger.error("ir couldn't close triple dump data", e)
      }
   }

   override fun hasNext(): Boolean {
      return lastTriples != null
   }

   override fun next(): TripleData {
      val oldTriples = lastTriples!!
      lastTriples = fetchNextTriples()
      return oldTriples
   }
}