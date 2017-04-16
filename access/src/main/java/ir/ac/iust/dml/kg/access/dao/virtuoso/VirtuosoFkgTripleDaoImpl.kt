package ir.ac.iust.dml.kg.access.dao.virtuoso

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.virtuoso.connector.VirtuosoConnector

class VirtuosoFkgTripleDaoImpl : FkgTripleDao {

   val connector = VirtuosoConnector("http://fkg.iust.ac.ir/")

   override fun save(t: FkgTriple, mapping: FkgPropertyMapping?) {
      if (t.objekt == null || t.objekt!!.trim().isEmpty()) {
         println("short triple here: ${t.source} ${t.predicate} ${t.objekt}")
         return
      }
      if (t.objekt!!.contains("://") && !t.objekt!!.contains(' '))
         connector.addResource(t.subject, t.predicate, t.objekt)
      else connector.addLiteral(t.subject, t.predicate, t.objekt)
   }

   fun close() {
      connector.close()
   }

   override fun deleteAll() {
      connector.clear()
   }

   override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
      // TODO not implemented
      return PagedData(mutableListOf(), 0, 0, 0, 0)
   }

   override fun read(subject: String?, predicate: String?, objekt: String?, status: MappingStatus?): MutableList<FkgTriple> {
      // TODO not implemented
      return mutableListOf()
   }

}
