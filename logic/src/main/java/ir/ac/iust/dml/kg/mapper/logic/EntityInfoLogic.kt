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
  val resources = mutableMapOf<String, List<String>>()

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
    result = PathWalker.getPath(path, Regex("\\d+\\.json"))
    type = object : TypeToken<Map<String, List<String>>>() {}.type

    result.forEachIndexed { _, p ->
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use {
        BufferedReader(it).use {
          val infoBoxes: Map<String, List<String>> = gson.fromJson(it, type)
          resources.putAll(infoBoxes)
        }
        if (resources.size > maxNumberOfTriples * 2) return@forEachIndexed
      }
    }
  }
}