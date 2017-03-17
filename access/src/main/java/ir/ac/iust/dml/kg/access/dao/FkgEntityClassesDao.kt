package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgEntityClasses
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.utils.PagedData

interface FkgEntityClassesDao {
   fun save(p: FkgEntityClasses)
   fun delete(p: FkgEntityClasses)
   fun deleteAll()
   fun list(pageSize: Int = 20, page: Int = 10): PagedData<FkgEntityClasses>
   fun read(id: Long): FkgEntityClasses?
   fun read(entity: String, className: String): FkgEntityClasses?
   fun readClassesOfEntity(entityName: String): List<String>
   fun search(page: Int, pageSize: Int, language: String?,
              entityName: String? = null, className: String? = null, like: Boolean = false,
              status: MappingStatus? = null, approved: Boolean? = null,
              after: Long? = null):
           PagedData<FkgEntityClasses>
}