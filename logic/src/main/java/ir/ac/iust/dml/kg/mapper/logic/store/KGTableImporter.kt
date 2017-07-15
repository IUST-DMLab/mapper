package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.mapper.logic.EntityToClassLogic
import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.data.InfoBoxAndCount
import ir.ac.iust.dml.kg.mapper.logic.test.TestUtils
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TableJsonFileReader
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class KGTableImporter {
  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var entityToClassLogic: EntityToClassLogic
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

    store.deleteAll()

    val entityTree = mutableMapOf<String, MutableSet<String>>()

    val result = PathWalker.getPath(path, Regex(".*\\.json"))

    // TODO: mastmal
    val propertyMap = mapOf(
        "دانشکده" to "fkgo:faculty",
        "رتبه دانشگاهی" to "fkgo:grade",
        "website" to "fkgo:website",
        "تصویر" to "fkgo:image",
        "پست الکترونیکی" to "fkgo:email",
        "آدرس" to "fkgo:address",
        "تلفن" to "fkgo:phone",
        "فکس" to "fkgo:fax",
        "گروه" to "fkgo:educationGroup",
        "نام" to "foaf:firstName",
        "نام خانوادگی" to "foaf:familyName"
    )

    entityToClassLogic.reloadTreeCache()

    var tripleNumber = 0
    result.forEachIndexed { index, p ->
      TableJsonFileReader(p).use { reader ->
        while (reader.hasNext() && tripleNumber++ < maxNumberOfTriples) {
          val triple = reader.next()
          try {
            if (triple.subject == null || triple.objekt == null) continue
            val subject = PrefixService.getFkgResourceUrl(triple.subject!!)
            val ontologyClass = triple.ontologyClass!!

            val newClassTree = entityToClassLogic.getTree(ontologyClass)!!
            entityTree.getOrPut(subject, { mutableSetOf() }).add(newClassTree)
            val predicate =
                if (propertyMap.containsKey(triple.predicate)) PrefixService.prefixToUri(propertyMap[triple.predicate!!])!!
                else PrefixService.convertFkgProperty(triple.predicate!!)!!

            store.save(triple.source!!, subject, triple.objekt!!, predicate)
          } catch (e: Throwable) {
            logger.error(triple.toString(), e)
          }
        }
      }
    }

    entityTree.forEach { entity, tress ->
      entityClassImporter.writeEntityTrees(entity,
          tress.map { InfoBoxAndCount(infoBox = "donCare", propertyCount = 1, tree = it.split("/")) }.toMutableSet(),
          store)
    }

    (store as? KnowledgeStoreFkgTripleDaoImpl)?.flush()
    (store as? VirtuosoFkgTripleDaoImpl)?.close()
  }
}