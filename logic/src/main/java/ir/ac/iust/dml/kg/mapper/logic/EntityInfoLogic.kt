/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.ac.iust.dml.kg.mapper.logic.utils.PathUtils
import ir.ac.iust.dml.kg.mapper.logic.utils.TestUtils
import ir.ac.iust.dml.kg.mapper.logic.wiki.DumpUtils
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

@Service
class EntityInfoLogic {

  val resources = mutableMapOf<String, MutableList<String>>()

  fun reload() {
    resources.clear()
    val gson = Gson()
    val path = PathUtils.getWithoutInfoboxPath()
    val maxNumberOfTriples = TestUtils.getMaxTuples()
    val result = PathWalker.getPath(path, Regex("\\d+-revision_ids\\.json"))
    val type = object : TypeToken<Map<String, String>>() {}.type
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

    DumpUtils.read({ infobox, entity, _ ->
      resources.getOrPut(entity, { mutableListOf() }).add(infobox)
    })
  }
}