package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.PropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.TemplateMapping
import org.springframework.stereotype.Service

@Service
class MappingHolder {
  val maps = mutableMapOf<String, TemplateMapping>()

  fun isValidTemplate(template: String) = maps.containsKey(template)

  fun getTemplateMapping(template: String)
      = maps.getOrPut(template, { TemplateMapping(template, creationEpoch = System.currentTimeMillis()) })

  fun getPropertyMapping(template: String, property: String)
      = getTemplateMapping(template).properties!!.getOrPut(property, { PropertyMapping() })

  override fun toString() = buildString { maps.values.forEach { this.append(it).append('\n') } }
}