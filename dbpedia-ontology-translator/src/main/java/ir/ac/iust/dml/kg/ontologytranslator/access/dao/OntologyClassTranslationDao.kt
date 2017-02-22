package ir.ac.iust.dml.kg.ontologytranslator.access.dao

import ir.ac.iust.dml.kg.ontologytranslator.access.entities.OntologyClassTranslation
import ir.ac.iust.dml.kg.utils.PagedData

interface OntologyClassTranslationDao {
   fun save(p: OntologyClassTranslation)

   fun read(name: String, parentId: Long? = null): OntologyClassTranslation?

   fun search(name: String?, parentId: Long? = null, like: Boolean = false, pageSize: Int = 20, page: Int = 10):
         PagedData<OntologyClassTranslation>
}