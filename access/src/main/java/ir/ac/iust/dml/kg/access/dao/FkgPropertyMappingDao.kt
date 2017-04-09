package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.PagedData

interface FkgPropertyMappingDao {
   fun save(p: FkgPropertyMapping)
   fun deleteAll()
   fun list(pageSize: Int = 20, page: Int = 10, hasClass: Boolean = true): PagedData<FkgPropertyMapping>
   fun read(id: Long): FkgPropertyMapping?
   fun searchTemplateName(page: Int, pageSize: Int, keyword: String?): List<String>
   fun searchOntologyClass(page: Int, pageSize: Int, keyword: String?): List<String>
   fun searchTemplatePropertyName(page: Int, pageSize: Int, keyword: String?): List<String>
   fun searchOntologyPropertyName(page: Int, pageSize: Int, keyword: String?): List<String>
   fun search(page: Int, pageSize: Int, language: String?,
              clazz: String? = null, type: String? = null, like: Boolean = false,
              hasClass: Boolean = false, templateProperty: String? = null,
              secondTemplateProperty: String? = null, ontologyProperty: String? = null,
              status: MappingStatus? = null, approved: Boolean? = null,
              after: Long? = null, noUpdateEpoch: Boolean? = null, noTemplatePropertyLanguage: Boolean? = null):
         PagedData<FkgPropertyMapping>

   fun readOntologyProperty(templateProperty: String): List<String>

   fun listUniqueProperties(page: Int = 0, pageSize: Int = 20, language: String?,
                            keyword: String? = null, ontologyClass: String? = null,
                            templateName: String? = null, status: MappingStatus? = null,
                            templatePropertyLanguage: String? = null): List<String>

   fun listUniqueOntologyProperties(templateProperty: String): List<String>
   fun countTemplateProperties(templateProperty: String): Long
   fun countOntologyProperties(templateProperty: String, ontologyProperty: String): Long
}