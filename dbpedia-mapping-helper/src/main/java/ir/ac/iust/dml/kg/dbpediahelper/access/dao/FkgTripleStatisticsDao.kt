package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.FkgTripleStatistics

interface FkgTripleStatisticsDao {

   fun save(t: FkgTripleStatistics)

   fun deleteAll()
}