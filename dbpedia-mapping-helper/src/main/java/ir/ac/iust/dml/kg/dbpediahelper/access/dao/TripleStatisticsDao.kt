package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.TripleStatistics

interface TripleStatisticsDao {

   fun save(t: TripleStatistics)

   fun deleteAll()
}