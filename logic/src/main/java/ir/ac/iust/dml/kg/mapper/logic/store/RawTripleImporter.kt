package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.mapper.logic.EntityInfoLogic
import ir.ac.iust.dml.kg.mapper.logic.PathUtils
import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.store.entities.MapRule
import ir.ac.iust.dml.kg.mapper.logic.test.TestUtils
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RawTripleImporter {
  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var storeProvider: StoreProvider
  @Autowired private lateinit var entityInfoLogic: EntityInfoLogic
  @Autowired private lateinit var notMappedPropertyHandler: NotMappedPropertyHandler

  fun writeTriples(storeType: StoreType = StoreType.none) {
    val path = PathUtils.getPath("raw.folder.input", "~/raw/triples")
    val maxNumberOfTriples = TestUtils.getMaxTuples()
    val store = storeProvider.getStore(storeType, path)

    val result = PathWalker.getPath(path, Regex(".*\\.json"))
    val startTime = System.currentTimeMillis()
    var tripleNumber = 0

    entityInfoLogic.reload()
    holder.writeToKS()
    holder.loadFromKS()

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
            val subject = URIs.getFkgResourceUri(triple.subject)
            val objekt = if (entityInfoLogic.resources.containsKey(triple.`object`))
              URIs.getFkgResourceUri(triple.`object`) else triple.`object`
            val predicate: String
            if (triple.isNeedsMapping) {
              val subjectInfoBoxes = entityInfoLogic.resources[subject]
              val defaultProperty = URIs.convertToNotMappedFkgPropertyUri(triple.predicate)!!
              predicate =
                  if (subjectInfoBoxes == null) defaultProperty
                  else {
                    var m = mutableSetOf<MapRule>()
                    subjectInfoBoxes.forEach {
                      val pm = holder.examinePropertyMapping(it, defaultProperty)
                      if (pm != null && pm.rules.isNotEmpty()) {
                        m = pm.rules
                        return@forEach
                      }
                    }
                    if (m.isNotEmpty()) m.iterator().next().predicate!!
                    else defaultProperty
                  }
              if (predicate == defaultProperty) notMappedPropertyHandler.addToNotMapped(triple.predicate)
            } else predicate = triple.predicate
            store.save(source = triple.sourceUrl, subject = subject,
                objeck = objekt, property = predicate, version = triple.version,
                extractionTime = triple.extractionTime, module = triple.module, rawText = triple.rawText,
                accuracy = triple.accuracy)
          } catch (th: Throwable) {
          }
        }
      }
    }
    store.flush()
  }
}