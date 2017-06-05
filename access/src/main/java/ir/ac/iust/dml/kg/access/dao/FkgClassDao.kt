package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgOntologyClass
import ir.ac.iust.dml.kg.raw.utils.PagedData

interface FkgClassDao {
   fun save(p: FkgOntologyClass)

   fun read(id: Long? = null): FkgOntologyClass?

   fun read(name: String, parentId: Long? = null): FkgOntologyClass?

   fun readRoot(): FkgOntologyClass?

   fun search(name: String? = null, parentId: Long? = null, like: Boolean = false, approved: Boolean? = null,
              hasFarsi: Boolean? = null, pageSize: Int = 20, page: Int = 0):
       PagedData<FkgOntologyClass>

   fun getChildren(id: Long): List<FkgOntologyClass>
}