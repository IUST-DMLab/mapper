package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.mapper.logic.EntityToClassLogic
import ir.ac.iust.dml.kg.mapper.logic.PathUtils
import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.test.TestUtils
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RawTripleImporter {
  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var entityToClassLogic: EntityToClassLogic
  @Autowired private lateinit var storeProvider: StoreProvider

  fun writeTriples(storeType: StoreType = StoreType.none) {
    val path = PathUtils.getPath("raw.folder.input", "~/raw/triples")
    val maxNumberOfTriples = TestUtils.getMaxTuples()
    val store = storeProvider.getStore(storeType, path)

    val result = PathWalker.getPath(path, Regex("\\d+\\.json"))
    val startTime = System.currentTimeMillis()
    var tripleNumber = 0
    result.forEachIndexed { index, p ->
      ir.ac.iust.dml.kg.raw.triple.RawTripleImporter(p).use { reader ->
        while (reader.hasNext()) {
          val triple = reader.next()
          tripleNumber++
          if (tripleNumber > maxNumberOfTriples) break
          try {
            if (tripleNumber % 1000 == 0)
              logger.warn("triple number is $tripleNumber. $index file is $p. " +
                  "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            val subject: String
            val objekt: String
            val predicate: String
            if (triple.isNeedsMapping) {
              subject = PrefixService.getFkgResourceUrl(triple.subject)
              objekt = ""
              predicate = ""
            } else {
              subject = triple.subject
              predicate = triple.predicate
              objekt = triple.`object`
            }
            store.save(source = triple.sourceUrl, subject = subject,
                objeck = objekt, property = predicate, version = triple.version,
                extractionTime = triple.extractionTime, module = triple.module, rawText = triple.rawText,
                accuracy = triple.accuracy)
          } catch (th: Throwable) {
          }
        }
      }
    }
    (store as? KnowledgeStoreFkgTripleDaoImpl)?.flush()
    (store as? VirtuosoFkgTripleDaoImpl)?.close()
  }
}