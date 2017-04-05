package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.DBpediaClass
import ir.ac.iust.dml.kg.raw.utils.PagedData

interface DBpediaClassDao {
   fun save(p: DBpediaClass)

   fun read(id: Long): DBpediaClass?

   fun read(name: String, parentId: Long? = null): DBpediaClass?

   fun search(name: String? = null, parentId: Long? = null, like: Boolean = false, pageSize: Int = 20, page: Int = 10):
         PagedData<DBpediaClass>
}