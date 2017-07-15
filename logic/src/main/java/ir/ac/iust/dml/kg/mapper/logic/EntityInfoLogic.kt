package ir.ac.iust.dml.kg.mapper.logic

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.ac.iust.dml.kg.mapper.logic.test.TestUtils
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import org.apache.log4j.Logger
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

@Service
class EntityInfoLogic {

  private val logger = Logger.getLogger(this.javaClass)!!
  val resources = mutableMapOf<String, MutableList<String>>()

  fun reload() {
    resources.clear()
    val gson = Gson()
    var path = PathUtils.getWithoutInfoboxPath()
    val maxNumberOfTriples = TestUtils.getMaxTuples()
    var result = PathWalker.getPath(path, Regex("\\d+-revision_ids\\.json"))
    var type = object : TypeToken<Map<String, String>>() {}.type
    result.forEachIndexed { _, p ->
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val revisionIdMap: Map<String, String> = gson.fromJson(it, type)
          revisionIdMap.keys.forEach {
            resources.put(it, mutableListOf())
          }
          if (resources.size > maxNumberOfTriples) return@forEachIndexed
        }
      }
    }

    path = PathUtils.getWithInfoboxPath()
    result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
    val startTime = System.currentTimeMillis()
    type = object : TypeToken<Map<String, Map<String, List<Map<String, String>>>>>() {}.type

    result.forEachIndexed { index, p ->
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val infoBoxes: Map<String, Map<String, List<Map<String, String>>>> = gson.fromJson(it, type)
          infoBoxes.forEach { infoBox, entityInfo ->
            entityInfo.forEach { entity, properties ->
              resources.getOrPut(entity, { mutableListOf() }).add(infoBox)
            }
          }
        }
      }
      logger.warn("$index file is $p. time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
    }
  }
}