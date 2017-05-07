package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.MapRule
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.PropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.ValueType
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1mappingsApi
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class KSMappingLoader {
  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var holder: MappingHolder
  private val mappingApi: V1mappingsApi

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    mappingApi = V1mappingsApi(client)
  }

  fun load() {
    val start = System.currentTimeMillis()
    val all = mappingApi.readAll1(0, null).data
    all.forEach {
      val tm = holder.getTemplateMapping(it.template)
      tm.weight = null //TODO add template weight to knowledge store
      tm.template = it.template
      tm.rules = it.rules.map { convert(it) }.toMutableSet()
      it.properties.forEach {
        val v = it.value!!
        tm.properties!![it.key] = PropertyMapping(
            property = v.property, weight = v.weight, rules = v.rules.map { convert(it) }.toMutableSet()
        )
      }
    }
    logger.info("mapping are loaded in ${System.currentTimeMillis() - start} milliseconds: ${holder.maps.values.size}")
  }

  private fun convert(it: ir.ac.iust.dml.kg.services.client.swagger.model.MapRule)
      = MapRule(predicate = it.predicate, state = null, //TODO add status to knowledge store
      transform = it.transform, constant = it.constant, unit = it.unit,
      type = ValueType.valueOf(it.type.name.toLowerCase().capitalize()))
}