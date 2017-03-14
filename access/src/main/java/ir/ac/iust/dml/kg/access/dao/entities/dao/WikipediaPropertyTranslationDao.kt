package ir.ac.iust.dml.kg.access.dao.entities.dao

import ir.ac.iust.dml.kg.access.dao.entities.WikipediaPropertyTranslation
import ir.ac.iust.dml.kg.utils.PagedData

interface WikipediaPropertyTranslationDao {
    fun save(p: WikipediaPropertyTranslation)

    fun list(pageSize: Int = 20, page: Int = 10): PagedData<WikipediaPropertyTranslation>

    fun readByFaTitle(type: String? = null, faProperty: String): MutableList<WikipediaPropertyTranslation>

    fun readByEnTitle(type: String? = null, enProperty: String): MutableList<WikipediaPropertyTranslation>
}