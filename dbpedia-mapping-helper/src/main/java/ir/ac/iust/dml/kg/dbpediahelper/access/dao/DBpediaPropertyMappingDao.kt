package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.WikipediaPropertyTranslation
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.utils.PagedData

interface DBpediaPropertyMappingDao {
   fun save(p: FkgPropertyMapping)

   fun deleteAll()

   fun list(pageSize: Int = 20, page: Int = 10, hasClass: Boolean = true): PagedData<FkgPropertyMapping>

   fun read(language: String?, clazz: String? = null, type: String? = null, like: Boolean = false,
            hasClass: Boolean = false, templateProperty: String? = null,
            secondTemplateProperty: String? = null, ontologyProperty: String? = null,
            status: MappingStatus? = null):
         MutableList<FkgPropertyMapping>

   fun readOntologyProperty(templateProperty: String): List<String>

   fun readByEnTitle(type: String? = null, enProperty: String): MutableList<WikipediaPropertyTranslation>

   fun listTemplatePropertyMapping(pageSize: Int = 20, page: Int = 10): PagedData<WikipediaPropertyTranslation>

   fun listUniqueProperties(language: String?, pageSize: Int = 20, page: Int = 10): List<String>

   fun listUniqueOntologyProperties(templateProperty: String): List<String>

   fun countTemplateProperties(templateProperty: String): Long

   fun countOntologyProperties(templateProperty: String, ontologyProperty: String): Long
}