package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.TemplateToClassMapping
import ir.ac.iust.dml.kg.utils.PagedData

interface TemplateToClassDao {
   fun save(t: TemplateToClassMapping)
   fun deleteAll()
   fun read(templateName: String, className: String?): TemplateToClassMapping?
   fun read(id: Long): TemplateToClassMapping?
   fun search(page: Int = 0, pageSize: Int = 20, templateName: String? = null,
              className: String? = null, like: Boolean = false,
              language: String? = null, approved: Boolean? = null,
              after: Long? = null, noUpdateEpoch: Boolean? = null):
         PagedData<TemplateToClassMapping>

   fun searchTemplateName(page: Int, pageSize: Int, keyword: String?): List<String>
   fun searchClassName(page: Int, pageSize: Int, keyword: String?): List<String>
}