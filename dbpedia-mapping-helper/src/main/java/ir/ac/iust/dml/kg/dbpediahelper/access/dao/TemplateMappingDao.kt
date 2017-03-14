package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.WikipediaTemplateRedirect
import ir.ac.iust.dml.kg.utils.PagedData

interface TemplateMappingDao {
   fun list(pageSize: Int = 20, page: Int = 10): PagedData<WikipediaTemplateRedirect>

   fun read(nameFa: String? = null, typeFa: String? = null, nameEn: String? = null,
            typeEn: String? = null, like: Boolean = false): MutableList<WikipediaTemplateRedirect>
}