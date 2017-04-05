package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgTemplateMapping
import ir.ac.iust.dml.kg.raw.utils.PagedData

interface FkgTemplateMappingDao {
   fun save(t: FkgTemplateMapping)
   fun deleteAll()
   fun read(templateName: String, className: String?): FkgTemplateMapping?
   fun read(id: Long): FkgTemplateMapping?
   fun search(page: Int = 0, pageSize: Int = 20, templateName: String? = null,
              className: String? = null, like: Boolean = false,
              language: String? = null, approved: Boolean? = null,
              after: Long? = null, noUpdateEpoch: Boolean? = null):
         PagedData<FkgTemplateMapping>

   fun searchTemplateName(page: Int, pageSize: Int, keyword: String?): List<String>
   fun searchOntologyClass(page: Int, pageSize: Int, keyword: String?): List<String>
}