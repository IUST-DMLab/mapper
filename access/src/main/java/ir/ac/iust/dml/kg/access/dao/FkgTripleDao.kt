package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.PagedData

interface FkgTripleDao {
   fun save(t: FkgTriple, mapping: FkgPropertyMapping?, approved: Boolean = false)

   fun deleteAll()

   fun list(pageSize: Int = 20, page: Int = 10): PagedData<FkgTriple>

   fun read(subject: String? = null, predicate: String? = null, objekt: String? = null,
            status: MappingStatus? = null): MutableList<FkgTriple>

}