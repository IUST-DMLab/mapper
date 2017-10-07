/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.ontology

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.mapper.logic.mapping.KSMappingHolder
import ir.ac.iust.dml.kg.mapper.logic.utils.StoreProvider
import ir.ac.iust.dml.kg.raw.utils.Module
import ir.ac.iust.dml.kg.raw.utils.PropertyNormaller
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
  private val VERSION = 1

  fun writePredicates(resolveAmbiguity: Boolean) {
    holder.writeToKS()
    holder.loadFromKS()
    writePredicates(storeProvider.getStore(StoreType.ontologyStore), resolveAmbiguity)
  }

  private fun save(store: FkgTripleDao, source: String, subject: String, property: String, objekt: String) {
    store.convertAndSave(source, subject, property, objekt, Module.expert.name, VERSION)
  }

  fun writePredicates(store: FkgTripleDao, resolveAmbiguity: Boolean) {
    data class PredicateData(var labels: MutableMap<String, Double> = mutableMapOf(),
                             var domains: MutableSet<String> = mutableSetOf())

    val predicateData = mutableMapOf<String, PredicateData>()

    holder.all().forEach { templateMapping ->
      templateMapping.properties!!.values.forEach { (property, weight, rules) ->
        val label = property!!.toLowerCase().replace('_', ' ')
        rules.forEach {
          if (it.predicate == null) return@forEach
          val data = predicateData.getOrPut(it.predicate!!, { PredicateData() })
          data.labels[label] = (data.labels[label] ?: 0.0) + (weight ?: 0.0)
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
      save(store, pu, pu, URIs.type, URIs.defaultTypeOfOntologyProperties)
      save(store, pu, pu, URIs.type, URIs.typeOfAnyProperties)

      val result = store.read(subject = pu, predicate = URIs.name)
      if (result.isEmpty()) {
        val name = URIs.getFkgOntologyNameFromUri(pu)
        save(store, pu, pu, URIs.name, name)
      }

      if (labels.isNotEmpty() && store.read(pu, URIs.label, null).isEmpty())
        save(store, pu, pu, URIs.label, PropertyNormaller.removeDigits(labels[0].first))
      labels.forEach {
        save(store, pu, pu, URIs.variantLabel, it.first)
        if (resolveAmbiguity) {
          val searched = store.read(predicate = URIs.variantLabel, objekt = it.first)
              .filter { triple -> triple.objekt == it.first && triple.subject != pu }
          if (searched.isNotEmpty()) {
            save(store, pu, pu, URIs.disambiguatedFrom, it.first)
            searched.forEach {
              save(store, it.source ?: it.subject!!, it.subject!!, URIs.disambiguatedFrom, it.objekt!!)
            }
          }
        }
      }

      val searched = store.read(subject = pu, predicate = URIs.wasDerivedFrom)
      if (searched.isEmpty()) save(store, pu, pu, URIs.wasDerivedFrom, pu)

      var commonRoot: String
      try {
        commonRoot = ontologyLogic.findCommonRoot(data.domains)!!
        logger.info("calculating root for ${data.domains} is $commonRoot")
      } catch (th: Throwable) {
        commonRoot = URIs.getFkgOntologyClassUri("Thing")
        logger.error("error in calculating root for ${data.domains}")
      }
      val oldAutoDomains = store.read(subject = pu, predicate = URIs.propertyAutoDomain)
      oldAutoDomains.forEach { store.delete(it.subject!!, it.predicate!!, it.objekt!!) }
      save(store, pu, pu, URIs.propertyAutoDomain, commonRoot)

      val oldDomains = store.read(subject = pu, predicate = URIs.propertyDomain)
      if (oldDomains.isEmpty()) {
        val thing = URIs.getFkgOntologyClassUri("Thing")
        save(store, pu, pu, URIs.propertyDomain, thing)
      }
    }
    store.flush()
  }
}