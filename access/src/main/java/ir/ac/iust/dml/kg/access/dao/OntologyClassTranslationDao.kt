package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgClass
import ir.ac.iust.dml.kg.utils.PagedData

interface OntologyClassTranslationDao {
   fun save(p: FkgClass)

   fun read(id: Long? = null): FkgClass?

   fun read(name: String, parentId: Long? = null): FkgClass?

   fun readRoot(): FkgClass?

   fun search(name: String? = null, parentId: Long? = null, like: Boolean = false, approved: Boolean? = null,
              hasFarsi: Boolean? = null, pageSize: Int = 20, page: Int = 0):
         PagedData<FkgClass>

   fun getChildren(id: Long): List<FkgClass>
}