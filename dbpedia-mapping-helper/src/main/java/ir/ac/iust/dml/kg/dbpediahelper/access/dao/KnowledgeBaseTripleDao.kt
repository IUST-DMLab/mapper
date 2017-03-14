package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.FkgTriple
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.utils.PagedData

interface KnowledgeBaseTripleDao {
   fun save(t: FkgTriple)

   fun deleteAll()

   fun list(pageSize: Int = 20, page: Int = 10): PagedData<FkgTriple>

   fun read(subject: String? = null, predicate: String? = null, objekt: String? = null,
            status: MappingStatus? = null): MutableList<FkgTriple>

}