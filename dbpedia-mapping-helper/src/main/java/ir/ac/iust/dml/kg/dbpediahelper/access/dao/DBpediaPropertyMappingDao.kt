package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.DBpediaPropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.TemplatePropertyMapping
import ir.ac.iust.dml.kg.utils.PagedData

interface DBpediaPropertyMappingDao {
   fun save(p: DBpediaPropertyMapping)

   fun list(pageSize: Int = 20, page: Int = 10, hasClass: Boolean = true): PagedData<DBpediaPropertyMapping>

   fun read(language: String?, clazz: String? = null, type: String? = null, like: Boolean = false,
            hasClass: Boolean = false, templateProperty: String? = null, ontologyProperty: String? = null):
         MutableList<DBpediaPropertyMapping>

   fun listTemplatePropertyMapping(pageSize: Int = 20, page: Int = 10): PagedData<TemplatePropertyMapping>
}