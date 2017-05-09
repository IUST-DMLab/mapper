package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.MapRule
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.PropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.TemplateMapping
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.entities.ValueType
import ir.ac.iust.dml.kg.services.client.swagger.model.MapRuleData
import ir.ac.iust.dml.kg.services.client.swagger.model.PropertyData
import ir.ac.iust.dml.kg.services.client.swagger.model.TemplateData

internal object KSMappingConverter {
  internal fun convert(tm: TemplateMapping): TemplateData {
    val td = TemplateData()
    td.template = tm.template
    td.weight = tm.weight
    td.properties = mutableListOf()
    tm.properties?.forEach { t, u -> td.properties.add(convert(u)) }
    tm.rules?.forEach { td.rules.add(convert(it)) }
    return td
  }

  internal fun convert(pm: PropertyMapping): PropertyData {
    val pd = PropertyData()
    pd.property = pm.property!!
    pd.weight = pm.weight
    pd.rules = mutableListOf()
    pm.rules.forEach { pd.rules.add(convert(it)) }
    pd.recommendations = mutableListOf()
    pm.recommendations.forEach { pd.recommendations.add(convert(it)) }
    return pd
  }

  internal fun convert(mr: MapRule): MapRuleData {
    val mrd = MapRuleData()
    mrd.predicate = mr.predicate
    mrd.constant = mr.constant
    mrd.type = MapRuleData.TypeEnum.valueOf(mr.type!!.toString().toUpperCase())
    mrd.transform = mr.transform
    mrd.unit = mr.unit
    return mrd
  }

  internal fun convert(it: ir.ac.iust.dml.kg.services.client.swagger.model.MapRule)
      = MapRule(predicate = it.predicate,
      transform = it.transform, constant = it.constant, unit = it.unit,
      type = ValueType.valueOf(it.type.name.toLowerCase().capitalize()))
}