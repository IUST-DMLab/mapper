package ir.ac.iust.dml.kg.dbpediahelper.access.dao.memory

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.StatisticalEventDao
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.MappingStatus
import org.springframework.stereotype.Repository

@Repository
open class StatisticalEventDaoImpl : StatisticalEventDao {

   var numberOfTriples: Int = 0
   var numberOfProcessedTriples: Int = 0
   var address: String? = null
   var statusStore: CounterStore<MappingStatus> = CounterStore()
   var typeStore: CounterStore<String> = CounterStore()
   var propertyStore: CounterStore<String> = CounterStore()
   var typeAndPropertyStore: CounterStore<TypedString> = CounterStore()
   var typeAndEntityStore: CounterStore<TypedString> = CounterStore()

   override fun log() {
      println("number of triples: $numberOfTriples")
      println("number of processed triples: $numberOfProcessedTriples")
      println("last file: $address")
      println("status stats:")
      println(statusStore)
      println("------------------------------------")
      println("types:")
      println(typeStore)
      println("property:")
      println(propertyStore)
      println("------------------------------------")
      println("type and property:")
      println(typeAndPropertyStore)
      println("type and entities:")
      println(typeAndEntityStore)
   }

   override fun statusGenerated(status: MappingStatus) {
      statusStore.count(status)
   }

   override fun tripleRead() {
      numberOfTriples++
   }

   override fun tripleProcessed() {
      numberOfProcessedTriples++
   }

   override fun fileProcessed(address: String) {
      this.address = address
   }

   override fun typeUsed(type: String) {
      typeStore.count(type)
   }

   override fun propertyUsed(property: String) {
      propertyStore.count(property)
   }

   override fun typeAndPropertyUsed(type: String, property: String) {
      typeAndPropertyStore.count(TypedString(type = type, string = property))
   }

   override fun typeAndEntityUsed(type: String, entity: String) {
      typeAndEntityStore.count(TypedString(type = type, string = entity))
   }

}