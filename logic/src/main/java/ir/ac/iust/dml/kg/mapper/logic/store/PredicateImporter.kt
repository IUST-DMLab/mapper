package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PredicateImporter {

  @Autowired private lateinit var holder: KSMappingHolder
  @Autowired private lateinit var ontologyLogic: OntologyLogic
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
          if (it.predicate == null) return@forEach
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

      val result = store.read(subject = pu, predicate = URIs.name)
      if (result.isEmpty()) {
        val name = pu.substring(pu.indexOf("/ontology/") + 10)
        store.convertAndSave(source = pu, subject = pu, property = URIs.name, objeck = name)
      }

      if (labels.isNotEmpty())
        store.convertAndSave(source = pu, subject = pu, property = URIs.label, objeck = labels[0].first)
      labels.forEach {
        store.convertAndSave(source = pu, subject = pu, property = URIs.variantLabel, objeck = it.first)
        if (resolveAmbiguity) {
          val searched = store.read(predicate = URIs.variantLabel, objekt = it.first)
              .filter { triple -> triple.objekt == it.first && triple.subject != pu }
          if (searched.isNotEmpty()) {
            store.convertAndSave(source = pu, subject = pu, property = URIs.disambiguatedFrom, objeck = it.first)
            searched.forEach {
              store.convertAndSave(source = it.source ?: it.subject!!,
                  subject = it.subject!!, property = URIs.disambiguatedFrom, objeck = it.objekt!!)
            }
          }
        }
      }

      var commonRoot: String
      try {
        commonRoot = ontologyLogic.findCommonRoot(data.domains)!!
        logger.info("calculating root for ${data.domains} is $commonRoot")
      } catch (th: Throwable) {
        commonRoot = URIs.getFkgOntologyClassUri("Thing")
        logger.error("error in calculating root for ${data.domains}")
      }
      store.convertAndSave(source = pu, subject = pu, property = URIs.propertyAutoDomain, objeck = commonRoot)
    }
    (store as? KnowledgeStoreFkgTripleDaoImpl)?.flush()
    (store as? VirtuosoFkgTripleDaoImpl)?.close()
  }
}