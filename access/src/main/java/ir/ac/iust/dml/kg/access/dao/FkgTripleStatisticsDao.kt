package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgTripleStatistics

interface FkgTripleStatisticsDao {

  fun save(t: FkgTripleStatistics)

  fun deleteAll()

  fun readType(templateType: String): FkgTripleStatistics?

  fun readProperty(property: String): FkgTripleStatistics?

  fun readTypedProperty(templateType: String, property: String): FkgTripleStatistics?

  fun readTypedEntity(templateType: String, entity: String): FkgTripleStatistics?
}