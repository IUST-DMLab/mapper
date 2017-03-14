package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgTripleStatistics

interface FkgTripleStatisticsDao {

   fun save(t: FkgTripleStatistics)

   fun deleteAll()
}