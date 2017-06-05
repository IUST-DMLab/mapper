package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PredicateImporter {

  @Autowired private lateinit var holder: KSMappingHolder
  private val logger = Logger.getLogger(this.javaClass)!!

  private val TYPE_OF_ALL_PROPERTIES = PrefixService.prefixToUri(PrefixService.TYPE_OF_ALL_PROPERTIES)!!
  private val PROPERTY_DOMAIN_URL = PrefixService.prefixToUri(PrefixService.PROPERTY_DOMAIN_URL)!!
  private val VARIANT_LABEL_URL = PrefixService.prefixToUri(PrefixService.VARIANT_LABEL_URL)!!
  private val LABEL = PrefixService.prefixToUri(PrefixService.LABEL_URL)!!
  private val TYPE_URL = PrefixService.prefixToUri(PrefixService.TYPE_URL)!!

  fun writePredicates(store: FkgTripleDao) {
    data class PredicateData(var labels: MutableMap<String, Double> = mutableMapOf(),
                             var domains: MutableSet<String> = mutableSetOf())

    val predicateData = mutableMapOf<String, PredicateData>()

    holder.all().forEach { templateMapping ->
      templateMapping.properties!!.values.forEach { propertyMapping ->
        val label = propertyMapping.property!!.toLowerCase().replace('_', ' ')
        propertyMapping.rules.forEach {
          val data = predicateData.getOrPut(it.predicate!!, { PredicateData() })
          data.labels[label] = (data.labels[label] ?: 0.0) + (propertyMapping.weight ?: 0.0)
          data.domains.add(templateMapping.ontologyClass)
        }
      }
    }

    predicateData.forEach { predicate, data ->
      val labels = data.labels.map { Pair(it.key, it.value) }.sortedByDescending { it.second }
      val pu = PrefixService.prefixToUri(predicate)!!
      if (!pu.contains("://")) {
        logger.error("wrong predicate: $pu")
        return@forEach
      }
      store.convertAndSave(source = pu, subject = pu, property = TYPE_URL, objeck = TYPE_OF_ALL_PROPERTIES)

      if (labels.isNotEmpty())
        store.convertAndSave(source = pu, subject = pu, property = LABEL, objeck = labels[0].first)
      labels.forEach {
        store.convertAndSave(source = pu, subject = pu, property = VARIANT_LABEL_URL, objeck = it.first)
      }
      data.domains.forEach {
        store.convertAndSave(source = pu, subject = pu, property = PROPERTY_DOMAIN_URL,
            objeck = PrefixService.getFkgOntologyClassUrl(it))
      }
    }
  }
}