package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.KnowledgeBaseTriple
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.MappingStatus
import ir.ac.iust.dml.kg.utils.PagedData

interface KnowledgeBaseTripleDao {
   fun save(t: KnowledgeBaseTriple)

   fun deleteAll()

   fun list(pageSize: Int = 20, page: Int = 10): PagedData<KnowledgeBaseTriple>

   fun read(subject: String? = null, predicate: String? = null, objekt: String? = null,
            status: MappingStatus? = null): MutableList<KnowledgeBaseTriple>

}