package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgTripleStatistics
import ir.ac.iust.dml.kg.access.entities.enumerations.TripleStatisticsType
import ir.ac.iust.dml.kg.raw.utils.PagedData

interface FkgTripleStatisticsDao {

  fun save(t: FkgTripleStatistics)

  fun deleteAll()

  fun search(page: Int, pageSize: Int, countType: TripleStatisticsType?): PagedData<FkgTripleStatistics>

  fun readType(templateType: String): FkgTripleStatistics?

  fun readProperty(property: String): FkgTripleStatistics?

  fun readTypedProperty(templateType: String, property: String): FkgTripleStatistics?

  fun readTypedEntity(templateType: String, entity: String): FkgTripleStatistics?
}