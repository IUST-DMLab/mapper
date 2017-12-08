/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.ontology

import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.mapper.logic.utils.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.utils.TestUtils
import ir.ac.iust.dml.kg.raw.utils.PropertyNormaller
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NotMappedPropertyHandler {

  private val notMappedProperties = mutableSetOf<String>()
  @Autowired private lateinit var storeProvider: StoreProvider
  private val logger = Logger.getLogger(this.javaClass)!!

  private val sourceUrl = URIs.prefixedToUri("fkg:mapper")!!

  fun addToNotMapped(property: String) {
    notMappedProperties.add(property)
  }

  fun writeNotMappedProperties(module: String, version: Int, resolveAmbiguity: Boolean) {
    val store = storeProvider.getStore(StoreType.ontologyStore)
    var maxNumberOfTriples = TestUtils.getMaxTuples()
    val startTime = System.currentTimeMillis()
    notMappedProperties.forEachIndexed { index, property ->
      maxNumberOfTriples++
      val name = property.substringAfterLast("/")
      val propertyUrl = URIs.convertToNotMappedFkgPropertyUri(name) ?: return@forEachIndexed
      store.save(sourceUrl, propertyUrl, URIs.type, URIs.typeOfAnyProperties, module, version)
      if (store.read(propertyUrl, URIs.label, null).isEmpty())
        store.save(sourceUrl, propertyUrl, URIs.label, PropertyNormaller.removeDigits(name), module, version)
      store.save(sourceUrl, propertyUrl, URIs.variantLabel, PropertyNormaller.removeDigits(name), module, version)

      if (resolveAmbiguity) {
        val result = store.read(predicate = URIs.variantLabel, objekt = name)
            .filter { triple -> triple.objekt == name && triple.subject != propertyUrl }
        if (result.isNotEmpty()) {
          store.convertAndSave(propertyUrl, propertyUrl, URIs.disambiguatedFrom, name, module, version)
          result.forEach {
            store.convertAndSave(it.source ?: it.subject!!,
                it.subject!!, URIs.disambiguatedFrom, it.objekt!!, module, version)
          }
        }
      }
      logger.warn("$index of ${notMappedProperties.size} properties written. " +
          "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
    }

    store.flush()
  }
}