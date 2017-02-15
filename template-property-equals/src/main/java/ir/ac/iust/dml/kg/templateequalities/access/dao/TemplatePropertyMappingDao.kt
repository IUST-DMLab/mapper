package ir.ac.iust.dml.kg.templateequalities.access.dao

import ir.ac.iust.dml.kg.templateequalities.access.entities.TemplatePropertyMapping
import ir.ac.iust.dml.kg.utils.PagedData

interface TemplatePropertyMappingDao {
    fun save(p: TemplatePropertyMapping)

    fun list(pageSize: Int = 20, page: Int = 10): PagedData<TemplatePropertyMapping>

    fun readByFaTitle(type: String? = null, faProperty: String): MutableList<TemplatePropertyMapping>

    fun readByEnTitle(type: String? = null, enProperty: String): MutableList<TemplatePropertyMapping>
}