package ir.ac.iust.dml.kg.access.dao.virtuoso

import com.hp.hpl.jena.graph.Node
import com.hp.hpl.jena.graph.Triple
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.PagedData
import virtuoso.jena.driver.VirtGraph

class VirtuosoFkgTripleDaoImpl : FkgTripleDao {

   val graph = VirtGraph("http://fkg.iust.ac.ir/", "jdbc:virtuoso://194.225.227.161:1111", "dba", "dba")

   override fun save(t: FkgTriple) {
      val sn = if (t.subject!!.startsWith("http://")) Node.createURI(t.subject) else Node.createLiteral("raw://" + t.subject)
      val pn = if (t.predicate!!.startsWith("http://")) Node.createURI(t.predicate) else Node.createURI("raw://" + t.predicate)
      val on = if (t.objekt!!.startsWith("http://")) Node.createURI(t.objekt) else Node.createLiteral("raw://" + t.objekt)
      graph.add(Triple.create(sn, pn, on))
   }

   override fun deleteAll() {
//      graph.clear()
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
