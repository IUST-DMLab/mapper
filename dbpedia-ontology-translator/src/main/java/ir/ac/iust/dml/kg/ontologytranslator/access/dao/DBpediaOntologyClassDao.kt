package ir.ac.iust.dml.kg.ontologytranslator.access.dao

import ir.ac.iust.dml.kg.ontologytranslator.access.entities.DBpediaOntologyClass
import ir.ac.iust.dml.kg.utils.PagedData

interface DBpediaOntologyClassDao {
   fun save(p: DBpediaOntologyClass)

   fun read(id: Long): DBpediaOntologyClass?

   fun read(name: String, parentId: Long? = null): DBpediaOntologyClass?

   fun search(name: String? = null, parentId: Long? = null, like: Boolean = false, pageSize: Int = 20, page: Int = 10):
         PagedData<DBpediaOntologyClass>
}