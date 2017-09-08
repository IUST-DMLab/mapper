package ir.ac.iust.dml.kg.mapper.logic

import ir.ac.iust.dml.kg.access.entities.FkgTripleStatistics
import ir.ac.iust.dml.kg.access.entities.enumerations.TripleStatisticsType
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.Closeable
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Path

class StatisticsLogReader(path: Path) : Iterator<FkgTripleStatistics>, Closeable {

  val logger = Logger.getLogger(this.javaClass)!!
  val reader = BufferedReader(InputStreamReader(FileInputStream(path.toFile()), "UTF8"))
  var lastTriples: FkgTripleStatistics? = null
  var lastType: TripleStatisticsType? = null

  init {
    lastTriples = fetchNextStats()
  }

  private fun fetchNextStats(): FkgTripleStatistics? {
    while (true) {
      val line = reader.readLine() ?: break
      if (line == "types:") {
        lastType = TripleStatisticsType.type
        continue
      } else if (line == "property:") {
        lastType = TripleStatisticsType.property
        continue
      } else if (line == "type and property:") {
        lastType = TripleStatisticsType.typedProperty
        continue
      } else if (line == "type and entities:") {
        lastType = TripleStatisticsType.typedEntity
        continue
      }

      if (lastType == null) continue
      val splits = line.split("\t")
      if (splits.size < 2) continue
      val count = splits[0].toInt()

      val stats: FkgTripleStatistics
      if (lastType == TripleStatisticsType.type) {
        stats = FkgTripleStatistics(templateName = splits[1])
      } else if (lastType == TripleStatisticsType.property) {
        stats = FkgTripleStatistics(property = splits[1])
      } else if (lastType == TripleStatisticsType.typedProperty) {
        val typeAndProperty = splits[1].split(" >> ")
        stats = FkgTripleStatistics(templateName = typeAndProperty[0], property = typeAndProperty[1])
      } else { //if(lastType == TripleStatisticsType.typedEntity) {
        val typeAndEntity = splits[1].split(" >> ")
        stats = FkgTripleStatistics(templateName = typeAndEntity[0], entity = typeAndEntity[1])
      }
      stats.countType = lastType
      stats.count = count
      return stats
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

  override fun next(): FkgTripleStatistics {
    val oldTriples = lastTriples!!
    lastTriples = fetchNextStats()
    return oldTriples
  }
}