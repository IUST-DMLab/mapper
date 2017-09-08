package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.data.InfoBoxAndCount
import ir.ac.iust.dml.kg.mapper.logic.test.TestUtils
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.Module
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TableJsonFileReader
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class KGTableImporter {
  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var ontologyLogic: OntologyLogic
  @Autowired private lateinit var entityClassImporter: EntityClassImporter
  @Autowired private lateinit var storeProvider: StoreProvider

  private fun getTriplesPath(): Path {
    val path = ConfigReader.getPath("tables.folder", "~/.pkg/data/tables")
    if (!Files.exists(path.parent)) Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
      throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }
    return path
  }

  fun writeTriples(storeType: StoreType = StoreType.none) {
    val path = getTriplesPath()

    val store = storeProvider.getStore(storeType, path)
    val maxNumberOfTriples = TestUtils.getMaxTuples()

    val entityTree = mutableMapOf<String, MutableSet<String>>()

    val result = PathWalker.getPath(path, Regex(".*\\.json"))
    val subjects = mutableListOf<String>()

    ontologyLogic.reloadTreeCache()

    var tripleNumber = 0
    val extractionTime = System.currentTimeMillis()
    val version = 1

    result.forEachIndexed { index, p ->
      TableJsonFileReader(p).use { reader ->
        while (reader.hasNext() && tripleNumber++ < maxNumberOfTriples) {
          val triple = reader.next()
          try {
            if (triple.subject == null || triple.objekt == null) continue
            val subject = URIs.getFkgResourceUri(triple.subject!!)

            if (triple.predicate == "rdf:instanceOf") {
              val newClassTree = ontologyLogic.getTree(triple.objekt!!.substringAfterLast(":"))!!
              entityTree.getOrPut(subject, { mutableSetOf() }).add(newClassTree)
              continue
            }
            val predicate = URIs.prefixedToUri(triple.predicate!!)!!
            subjects.add(subject)
            store.save(triple.source!!, subject, triple.objekt!!, triple.module!!, predicate, null, null,
                extractionTime, version)
          } catch (e: Throwable) {
            logger.error(triple.toString(), e)
          }
        }
      }
    }

    subjects.forEach { subject ->
      val label = subject.substringAfterLast("/").replace("_", " ")
      store.save(subject, subject, label, Module.mapper_auto_labeling.name, URIs.label, null, null,
          extractionTime, version)
    }

    entityTree.forEach { entity, tress ->
      entityClassImporter.writeEntityTrees(entity,
          tress.map { InfoBoxAndCount(infoBox = "donCare", propertyCount = 1, tree = it.split("/")) }.toMutableSet(),
          store, Module.web_table_extractor.name, version)
    }

    store.flush()
  }
}