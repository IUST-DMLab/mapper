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
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleData
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

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

  val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
  fun convertToPersian(num: String): String {
    val builder = StringBuilder()
    for (ch in num) builder.append(persianDigits[ch.toInt() - '0'.toInt()])
    return builder.toString()
  }

  // check all triples of a subject and give them as collections.
  // it handles numbered keys. for example put all (a1,b1,c1) to one collection
  fun collectTriples(triplesOfSubject: MutableList<TripleData>): List<List<TripleData>> {
    val result = mutableListOf<List<TripleData>>()
    var index = triplesOfSubject.size
    while (index > 0) {
      val englishDigit = "$index"
      val persianDigit = convertToPersian(englishDigit)
      val indexCollection = mutableListOf<TripleData>()
      triplesOfSubject.forEach {
        if (it.predicate?.endsWith(englishDigit) == true
            || it.predicate?.endsWith(persianDigit) == true) indexCollection.add(it)
      }
      if (indexCollection.isNotEmpty()) {
        result.add(indexCollection)
        triplesOfSubject.removeAll(indexCollection)
      }
      index--
    }
    result.reverse()
    triplesOfSubject.forEach { result.add(listOf(it)) }
    return result
  }

  private val invalidPropertyRegex = Regex("\\d+")
  fun getTriples(listener: (triples: MutableList<TripleData>) -> Unit) {
    val maxNumberOfTriples = TestUtils.getMaxTuples()
    val path = PathUtils.getTriplesPath()
    val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
    val startTime = System.currentTimeMillis()
    var tripleNumber = 0
    val tripleCache = mutableListOf<TripleData>()
    var lastSubject: String? = null
    result.forEachIndexed { index, p ->
      TripleJsonFileReader(p).use { reader ->
        while (reader.hasNext()) {
          val triple = reader.next()
          tripleNumber++
          if (tripleNumber > maxNumberOfTriples) break
          try {
            if (triple.templateType == null || triple.templateNameFull == null) continue
            if (triple.objekt!!.startsWith("fa.wikipedia.org/wiki"))
              triple.objekt = "http://" + triple.objekt
            if (tripleNumber % 1000 == 0)
              logger.warn("triple number is $tripleNumber. $index file is $p. " +
                  "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            val property = triple.predicate!!
            // some properties are invalid based on rdf standards
            if (property.trim().isBlank() || property.matches(invalidPropertyRegex)) continue
            tripleCache.add(triple)
            if (lastSubject != triple.subject && tripleCache.isNotEmpty()) {
              listener(tripleCache)
              tripleCache.clear()
            }
            lastSubject = triple.subject
          } catch (th: Throwable) {
            logger.info("triple: $triple")
            logger.error(th)
          }
        }
      }
    }
    if (tripleCache.isNotEmpty()) listener(tripleCache)
  }
}