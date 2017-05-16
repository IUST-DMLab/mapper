package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgTripleStatisticsDao
import ir.ac.iust.dml.kg.access.dao.memory.StatisticalEventDaoImpl
import ir.ac.iust.dml.kg.access.entities.enumerations.TripleStatisticsType
import ir.ac.iust.dml.kg.dbpediahelper.logic.dump.StatisticsLogReader
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.PropertyNormaller
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import javax.annotation.PreDestroy

@Service
class StatsLogic {

   val logger = Logger.getLogger(this.javaClass)!!
   @Autowired lateinit var eventDao: StatisticalEventDaoImpl
   @Autowired lateinit var fkgTripleStatisticsDao: FkgTripleStatisticsDao
   @Autowired lateinit var statsGenerationTaskExecutor: ThreadPoolTaskExecutor

   @PreDestroy
   fun shutdown() {
      statsGenerationTaskExecutor.shutdown()
   }

   fun createStatsFile() {
      val startTime = System.currentTimeMillis()
      val path = ConfigReader.getPath("wiki.triple.input.folder", "~/.pkg/data/triples")
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
      var tripleNumber = 0
      result.forEachIndexed { index, p ->
         statsGenerationTaskExecutor.execute {
            eventDao.fileProcessed(p.toString())
            TripleJsonFileReader(p).use { reader ->
               while (reader.hasNext()) {
                  val triple = reader.next()
                  try {
                     eventDao.tripleRead()
                     tripleNumber++

                     if (tripleNumber % 2000 == 0)
                        logger.info("triple number is $tripleNumber. $index file is $p. " +
                              "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} secs")

                     if (tripleNumber % 10000 == 0) {
                        logger.info("triple number is $tripleNumber. saving log")
                        saveLog(path)
                     }

                     if (triple.subject == null) continue
//                     val subject = prefixService.replacePrefixes(triple.subject!!)
                     val predicate = PropertyNormaller.removeDigits(triple.predicate!!)
                     val type = triple.templateNameFull!!
                     eventDao.propertyUsed(triple.predicate!!)
                     eventDao.typeUsed(type)
//                     eventDao.typeAndEntityUsed(type, subject)
                     eventDao.typeAndPropertyUsed(type, predicate)
                     eventDao.tripleProcessed()
                  } catch (th: Throwable) {
                     logger.info("triple: $triple")
                     logger.error(th)
                  }
               }
            }
         }
      }
      do {
         Thread.sleep(10000)
      } while (statsGenerationTaskExecutor.activeCount > 0)
      saveLog(path)
      println(eventDao.log())
   }

   private fun saveLog(path: Path) {
      synchronized(this) {
         val p = path.resolve("mapped").resolve("stats.txt")
         Files.createDirectories(p.parent)
         Files.write(p, eventDao.log().toByteArray(Charset.forName("UTF-8")))
         logger.info("log ${p.toAbsolutePath()} saved")
      }
   }

   @Throws(Exception::class)
   fun writeStats() {
      val path = ConfigReader.getPath("mapped.triple.stats.file", "~/.pkg/data/triples/mapped/stats.txt")
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      fkgTripleStatisticsDao.deleteAll()

      StatisticsLogReader(path).use {
         var lineNumber = 0
         while (it.hasNext()) {
            lineNumber++
            if (lineNumber % 1000 == 0) logger.info("line number $lineNumber processed")
            val stats = it.next()
            try {
               if (stats.countType == TripleStatisticsType.typedEntity) break
               fkgTripleStatisticsDao.save(stats)
            } catch (e: Throwable) {
               logger.error("error in $stats", e)
            }
         }
      }
   }

}