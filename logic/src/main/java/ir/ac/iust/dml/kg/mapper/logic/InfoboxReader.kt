package ir.ac.iust.dml.kg.mapper.logic

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.ac.iust.dml.kg.mapper.logic.utils.PathUtils
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

object InfoboxReader {

  private val logger = Logger.getLogger(this.javaClass)!!
  private val type = object : TypeToken<Map<String, Map<String, List<Map<String, String>>>>>() {}.type!!
  private val gson = Gson()

  fun read(listener: (infobox: String, entity: String, properties: Map<String, String>) -> Unit) {
    val path = PathUtils.getWithInfoboxPath()
    val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
    val startTime = System.currentTimeMillis()
    result.forEachIndexed { index, p ->
      logger.info("file $index of ${result.size} starts (${p.toAbsolutePath()}) " +
          "after ${System.currentTimeMillis() - startTime} miliseconds ..")
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val infoBoxes: Map<String, Map<String, List<Map<String, String>>>> = gson.fromJson(it, type)
          infoBoxes.forEach { infoBox, entityInfo ->
            entityInfo.forEach { entity, properties ->
              properties.forEach { p -> listener(infoBox, entity, p) }
            }
          }
        }
      }
    }
    logger.info("all infoboxes has been completed in ${System.currentTimeMillis() - startTime} miliseconds")
  }
}