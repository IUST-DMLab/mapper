package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.file.FileFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class StoreProvider {

  @Autowired private lateinit var tripleDao: FkgTripleDao

  fun getStore(storeType: TripleImporter.StoreType, path: Path): FkgTripleDao {
    val store = when (storeType) {
      TripleImporter.StoreType.file -> FileFkgTripleDaoImpl(path.resolve("mapped"))
      TripleImporter.StoreType.mysql -> tripleDao
      TripleImporter.StoreType.virtuoso -> VirtuosoFkgTripleDaoImpl()
      else -> KnowledgeStoreFkgTripleDaoImpl()
    }
    return store
  }
}