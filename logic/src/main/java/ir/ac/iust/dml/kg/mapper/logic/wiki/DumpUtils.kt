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

package ir.ac.iust.dml.kg.mapper.logic.wiki

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ir.ac.iust.dml.kg.mapper.logic.utils.PathUtils
import ir.ac.iust.dml.kg.mapper.logic.utils.TestUtils
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleData
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
import org.apache.log4j.Logger
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Path

object DumpUtils {

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
      InputStreamReader(FileInputStream(p.toFile()), "UTF8").use { stream ->
        BufferedReader(stream).use {
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

  private val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
  private fun convertToPersian(num: String): String {
    val builder = StringBuilder()
    for (ch in num) builder.append(persianDigits[ch.toInt() - '0'.toInt()])
    return builder.toString()
  }

  // check all triples of a subject and give them as collections.
  // it handles numbered keys. for example put all (a1,b1,c1) to one collection
  fun collectTriples(triplesOfSubject: MutableList<TripleData>): List<MutableList<TripleData>> {
    val result = mutableListOf<MutableList<TripleData>>()
    var index = triplesOfSubject.size
    while (index > 0) {
      val englishDigit = "$index"
      val persianDigit = convertToPersian(englishDigit)
      val indexCollection = mutableListOf<TripleData>()
      triplesOfSubject.forEach {
        if ((it.predicate?.endsWith(englishDigit) == true
            || it.predicate?.endsWith(persianDigit) == true)
            && (index != 2 || it.predicate?.endsWith("km2") == false))
          indexCollection.add(it)
      }
      if (indexCollection.isNotEmpty()) {
        val priorityIndexCollection = mutableListOf<TripleData>()
        var mainTriple: TripleData? = null
        for (triple in indexCollection) {
          val predicate = triple.predicate
          if (predicate?.startsWith("order") == true || predicate?.startsWith("office") == true) mainTriple = triple
          else priorityIndexCollection.add(triple)
        }
        priorityIndexCollection.sortBy { it.predicate }
        if (mainTriple != null) priorityIndexCollection.add(0, mainTriple)
        result.add(priorityIndexCollection)
        triplesOfSubject.removeAll(indexCollection)
      }
      index--
    }
    result.reverse()
    triplesOfSubject.forEach { result.add(mutableListOf(it)) }
    return result
  }

  fun tripleFilter(path: Path, pattern: String, filteredUris: Set<String>, outPath: Path) {
    val selected = mutableListOf<TripleData>()
    getTriples(path, pattern, { triples ->
      if (filteredUris.contains(triples.first().subject)) {
        selected.addAll(triples)
      }
    })
    OutputStreamWriter(FileOutputStream(outPath.toFile()), Charset.forName("UTF-8").newEncoder()).use {
      GsonBuilder().setPrettyPrinting().create().toJson(selected, it)
    }
  }

  private val invalidPropertyRegex = Regex("\\d+")
  fun getTriples(path: Path, pattern: String, listener: (triples: MutableList<TripleData>) -> Unit,
                 needsTemplate: Boolean = false) {
    val maxNumberOfTriples = TestUtils.getMaxTuples()
    val result = PathWalker.getPath(path, Regex(pattern))
    val startTime = System.currentTimeMillis()
    var tripleNumber = 0
    val tripleCache = mutableListOf<TripleData>()
    var lastSubject: String? = null
    result.forEachIndexed { index, p ->
      logger.info("reading file $p")
      TripleJsonFileReader(p).use { reader ->
        while (reader.hasNext()) {
          val triple = reader.next()
          tripleNumber++
          if (tripleNumber > maxNumberOfTriples) break
          try {
            if (needsTemplate && (triple.templateType == null || triple.templateNameFull == null)) continue
            if (triple.objekt!!.startsWith("fa.wikipedia.org/wiki"))
              triple.objekt = "http://" + triple.objekt
            if (triple.objekt.isNullOrBlank()) continue
            val property = triple.predicate!!
            // some properties are invalid based on rdf standards
            if (property.trim().isBlank() || property.matches(invalidPropertyRegex)) continue
            if (lastSubject != null && lastSubject != triple.subject && tripleCache.isNotEmpty()) {
              logger.trace("${tripleCache.size} triples of ${lastSubject} has been found. " +
                  "($tripleNumber triples since now in ${System.currentTimeMillis() - startTime} miliseconds)")
              listener(tripleCache)
              tripleCache.clear()
              tripleCache.add(triple)
            } else tripleCache.add(triple)
            lastSubject = triple.subject
          } catch (th: Throwable) {
            logger.trace("triple: $triple")
            logger.trace(th)
          }
        }
      }
    }
    if (tripleCache.isNotEmpty()) {
      logger.info("${tripleCache.size} triples of ${lastSubject} has been found. " +
          "($tripleNumber triples since now in ${System.currentTimeMillis() - startTime} miliseconds)")
      try {
        listener(tripleCache)
      } catch (th: Throwable) {
        th.printStackTrace()
      }
    }
  }
}