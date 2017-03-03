package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.MappingStatus

interface StatisticalEventDao {

   fun log(): String

   fun statusGenerated(status: MappingStatus)

   fun tripleRead()

   fun tripleProcessed()

   fun fileProcessed(address: String)

   fun typeUsed(type: String)

   fun propertyUsed(property: String)

   fun typeAndPropertyUsed(type: String, property: String)

   fun typeAndEntityUsed(type: String, entity: String)
}