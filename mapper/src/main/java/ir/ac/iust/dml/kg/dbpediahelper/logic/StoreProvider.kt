package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.file.FileFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.dbpediahelper.logic.type.StoreType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class StoreProvider {

  @Autowired private lateinit var tripleDao: FkgTripleDao
  private var fileDao: FileFkgTripleDaoImpl? = null
  private var virtuosoDao: VirtuosoFkgTripleDaoImpl? = null
  private var ksDao: KnowledgeStoreFkgTripleDaoImpl? = null

  fun getStore(storeType: StoreType, path: Path? = null): FkgTripleDao {
    val store = when (storeType) {
      StoreType.file -> {
        if (fileDao == null) fileDao = FileFkgTripleDaoImpl(path!!.resolve("mapped"))
        return fileDao!!
      }
      StoreType.mysql -> tripleDao
      StoreType.virtuoso -> {
        if (virtuosoDao == null) virtuosoDao = VirtuosoFkgTripleDaoImpl()
        return virtuosoDao!!
      }
      else -> {
        if (ksDao == null) ksDao = KnowledgeStoreFkgTripleDaoImpl()
        return ksDao!!
      }
    }
    return store
  }
}