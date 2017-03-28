package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.WikipediaTemplateRedirect
import ir.ac.iust.dml.kg.utils.PagedData

interface WikipediaTemplateRedirectDao {
   fun list(pageSize: Int = 20, page: Int = 10): PagedData<WikipediaTemplateRedirect>

  fun read(nameFa: String? = null, nameEn: String? = null, like: Boolean = false): MutableList<WikipediaTemplateRedirect>
  fun save(e: WikipediaTemplateRedirect)
}