package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.test.TestUtils
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NotMappedPropertyHandler {

  private val notMappedProperties = mutableSetOf<String>()
  @Autowired private lateinit var storeProvider: StoreProvider
  private val logger = Logger.getLogger(this.javaClass)!!

  val SOURCE_URL = "http://fkg.iust.ac.ir/mapper"

  fun addToNotMapped(property: String) {
    notMappedProperties.add(property)
  }

  fun writeNotMappedProperties(storeType: StoreType = StoreType.none, resolveAmbiguity: Boolean) {
    val store = storeProvider.getStore(storeType)
    var maxNumberOfTriples = TestUtils.getMaxTuples()
    val startTime = System.currentTimeMillis()
    notMappedProperties.forEachIndexed { index, property ->
      maxNumberOfTriples++
      val name = property.substringAfterLast("/")
      val propertyUrl = URIs.convertToNotMappedFkgPropertyUri(name)!!
      store.save(SOURCE_URL, propertyUrl, URIs.typeOfAnyProperties, URIs.type)
      store.save(SOURCE_URL, propertyUrl, name, URIs.label)
      store.save(SOURCE_URL, propertyUrl, name, URIs.variantLabel)

      if (resolveAmbiguity) {
        val result = store.read(predicate = URIs.variantLabel, objekt = name)
            .filter { triple -> triple.objekt == name && triple.subject != propertyUrl }
        if (result.isNotEmpty()) {
          store.convertAndSave(source = propertyUrl, subject = propertyUrl,
              property = URIs.disambiguatedFrom, objeck = name)
          result.forEach {
            store.convertAndSave(source = it.source ?: it.subject!!,
                subject = it.subject!!, property = URIs.disambiguatedFrom, objeck = it.objekt!!)
          }
        }
      }
      logger.warn("$index of ${notMappedProperties.size} properties written. " +
          "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
    }

    store.flush()
  }
}