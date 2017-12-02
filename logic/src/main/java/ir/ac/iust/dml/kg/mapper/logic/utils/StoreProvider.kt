/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.utils

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.file.FileFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.knowldegestore.OntologyStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.none.EmptyFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.ttl.TtlFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class StoreProvider {

  @Autowired private lateinit var tripleDao: FkgTripleDao
  private var fileDao: FileFkgTripleDaoImpl? = null
  private var ttlDao: TtlFkgTripleDaoImpl? = null
  private var virtuosoDao: VirtuosoFkgTripleDaoImpl? = null
  private var ksDao: KnowledgeStoreFkgTripleDaoImpl? = null
  private var ontologyDao: OntologyStoreFkgTripleDaoImpl? = null
  private var emptyDao: EmptyFkgTripleDaoImpl? = null

  fun getStore(storeType: StoreType): FkgTripleDao {
    return when (storeType) {
      StoreType.file -> {
        if (fileDao == null) fileDao = FileFkgTripleDaoImpl(ConfigReader.getPath("store.file.root", "~/triple_store"))
        return fileDao!!
      }
      StoreType.ttl -> {
        if (ttlDao == null) ttlDao = TtlFkgTripleDaoImpl(ConfigReader.getPath("store.ttl.root", "~/ttl_store"))
        return ttlDao!!
      }
      StoreType.mysql -> tripleDao
      StoreType.virtuoso -> {
        if (virtuosoDao == null) virtuosoDao = VirtuosoFkgTripleDaoImpl()
        return virtuosoDao!!
      }
      StoreType.knowledgeStore -> {
        if (ksDao == null) ksDao = KnowledgeStoreFkgTripleDaoImpl()
        return ksDao!!
      }
      StoreType.ontologyStore -> {
        if (ontologyDao == null) ontologyDao = OntologyStoreFkgTripleDaoImpl()
        return ontologyDao!!
      }
      StoreType.none -> {
        if (emptyDao == null) emptyDao = EmptyFkgTripleDaoImpl()
        return emptyDao!!
      }
    }
  }
}