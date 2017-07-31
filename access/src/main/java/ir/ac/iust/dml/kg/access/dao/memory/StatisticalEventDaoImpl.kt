package ir.ac.iust.dml.kg.access.dao.memory

import ir.ac.iust.dml.kg.access.dao.StatisticalEventDao
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
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

  override fun log(): String {
    val builder = StringBuilder()
    builder.append("number of triples: $numberOfTriples").append('\n')
    builder.append("number of processed triples: $numberOfProcessedTriples").append('\n')
    builder.append("last file: $address").append('\n')
    builder.append("status stats:").append('\n')
    builder.append(statusStore).append('\n')
    builder.append("------------------------------------").append('\n')
    builder.append("types:").append('\n')
    builder.append(typeStore).append('\n')
    builder.append("property:").append('\n')
    builder.append(propertyStore).append('\n')
    builder.append("------------------------------------").append('\n')
    builder.append("type and property:").append('\n')
    builder.append(typeAndPropertyStore).append('\n')
    builder.append("type and entities:").append('\n')
    builder.append(typeAndEntityStore).append('\n')
    return builder.toString()
  }

  @Synchronized
  override fun statusGenerated(status: MappingStatus) {
    statusStore.count(status)
  }

  @Synchronized
  override fun tripleRead() {
    numberOfTriples++
  }

  @Synchronized
  override fun tripleProcessed() {
    numberOfProcessedTriples++
  }

  @Synchronized
  override fun fileProcessed(address: String) {
    this.address = address
  }

  @Synchronized
  override fun typeUsed(type: String) {
    typeStore.count(type)
  }

  @Synchronized
  override fun propertyUsed(property: String) {
    propertyStore.count(property)
  }

  @Synchronized
  override fun typeAndPropertyUsed(type: String, property: String) {
    typeAndPropertyStore.count(TypedString(type = type, string = property))
  }

  @Synchronized
  override fun typeAndEntityUsed(type: String, entity: String) {
    typeAndEntityStore.count(TypedString(type = type, string = entity))
  }

}