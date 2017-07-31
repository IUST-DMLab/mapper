package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PredicateImporter {

  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var storeProvider: StoreProvider
  private val logger = Logger.getLogger(this.javaClass)!!

  fun writePredicates(type: StoreType, resolveAmbiguity: Boolean) {
    holder.writeToKS()
    holder.loadFromKS()
    writePredicates(storeProvider.getStore(type), resolveAmbiguity)
  }

  fun writePredicates(store: FkgTripleDao, resolveAmbiguity: Boolean) {
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

    val size = predicateData.size
    var number = 0
    val startTime = System.currentTimeMillis()
    predicateData.forEach { predicate, data ->
      number++
      logger.info("predicate $number form $size: $predicate " +
          "in ${(System.currentTimeMillis() - startTime) / 1000} seconds.")
      val labels = data.labels.map { Pair(it.key, it.value) }.sortedByDescending { it.second }
      val pu = URIs.prefixedToUri(predicate)!!
      if (!pu.contains("://")) {
        logger.error("wrong predicate: $pu")
        return@forEach
      }
      store.convertAndSave(source = pu, subject = pu, property = URIs.type, objeck = URIs.typeOfAllProperties)

      if (labels.isNotEmpty())
        store.convertAndSave(source = pu, subject = pu, property = URIs.label, objeck = labels[0].first)
      labels.forEach {
        store.convertAndSave(source = pu, subject = pu, property = URIs.variantLabel, objeck = it.first)
        if (resolveAmbiguity) {
          val result = store.read(predicate = URIs.variantLabel, objekt = it.first)
              .filter { triple -> triple.objekt == it.first && triple.subject != pu }
          if (result.isNotEmpty()) {
            store.convertAndSave(source = pu, subject = pu, property = URIs.disambiguatedFrom, objeck = it.first)
            result.forEach {
              store.convertAndSave(source = it.source ?: it.subject!!,
                  subject = it.subject!!, property = URIs.disambiguatedFrom, objeck = it.objekt!!)
            }
          }
        }
      }

      data.domains.forEach {
        store.convertAndSave(source = pu, subject = pu, property = URIs.propertyDomain,
            objeck = URIs.getFkgOntologyClassUri(it))
      }
    }
  }
}