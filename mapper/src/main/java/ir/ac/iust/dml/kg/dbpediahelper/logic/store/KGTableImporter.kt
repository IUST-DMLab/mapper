package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.dbpediahelper.logic.EntityToClassLogic
import ir.ac.iust.dml.kg.dbpediahelper.logic.StoreProvider
import ir.ac.iust.dml.kg.dbpediahelper.logic.TripleImporter
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TableJsonFileReader
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class KGTableImporter {
  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var entityToClassLogic: EntityToClassLogic
  @Autowired private lateinit var entityClassImporter: EntityClassImporter
  @Autowired private lateinit var storeProvider: StoreProvider

  private fun getTriplesPath(): Path {
    val path = ConfigReader.getPath("wiki.table.input.folder", "~/.pkg/data/tables")
    if (!Files.exists(path.parent)) Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
      throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }
    return path
  }

  fun writeTriples(storeType: TripleImporter.StoreType = TripleImporter.StoreType.none) {
    holder.writeToKS()
    holder.loadFromKS()

    val path = getTriplesPath()

    val store = storeProvider.getStore(storeType, path)
    val maxNumberOfTriples = ConfigReader.getInt("test.mode.max.triples", "10000000")

    store.deleteAll()

    val entityTree = mutableMapOf<String, MutableSet<String>>()

    val result = PathWalker.getPath(path, Regex(".*\\.json"))

    var tripleNumber = 0
    result.forEachIndexed { index, p ->
      TableJsonFileReader(p).use { reader ->
        while (reader.hasNext()) {
          val triple = reader.next()
          println(triple)
        }
      }
    }

    entityClassImporter.writeEntityTrees(entityTree, store)

    if (store is KnowledgeStoreFkgTripleDaoImpl) store.flush()
    if (store is VirtuosoFkgTripleDaoImpl) store.close()
  }
}