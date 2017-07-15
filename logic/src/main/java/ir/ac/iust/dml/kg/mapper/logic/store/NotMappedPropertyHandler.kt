package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.test.TestUtils
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NotMappedPropertyHandler {

  private val notMappedProperties = mutableSetOf<String>()
  @Autowired private lateinit var storeProvider: StoreProvider
  private val logger = Logger.getLogger(this.javaClass)!!

  private val TYPE_URL = PrefixService.prefixToUri(PrefixService.TYPE_URL)!!
  private val LABEL = PrefixService.prefixToUri(PrefixService.LABEL_URL)!!
  private val VARIANT_LABEL_URL = PrefixService.prefixToUri(PrefixService.VARIANT_LABEL_URL)!!
  private val TYPE_OF_ALL_NOT_MAPPED_PROPERTIES = PrefixService.prefixToUri(PrefixService.TYPE_OF_ALL_NOT_MAPPED_PROPERTIES)!!
  private val DISAMBIGUATED_FROM_URL = PrefixService.prefixToUri(PrefixService.DISAMBIGUATED_FROM_URI)!!
  val SOURCE_URL = "http://fkg.iust.ac.ir/mapper"

  fun addToNotMapped(property: String) {
    notMappedProperties.add(property)
  }

  fun writeNotMappedProperties(storeType: StoreType = StoreType.none) {
    val store = storeProvider.getStore(storeType)
    var maxNumberOfTriples = TestUtils.getMaxTuples()
    val startTime = System.currentTimeMillis()
    notMappedProperties.forEachIndexed { index, property ->
      maxNumberOfTriples++
      val name = property.substringAfterLast("/")
      val propertyUrl = PrefixService.convertFkgProperty(name)!!
      store.save(SOURCE_URL, propertyUrl, TYPE_OF_ALL_NOT_MAPPED_PROPERTIES, TYPE_URL)
      store.save(SOURCE_URL, propertyUrl, name, LABEL)
      store.save(SOURCE_URL, propertyUrl, name, VARIANT_LABEL_URL)

      val result = store.read(predicate = VARIANT_LABEL_URL, objekt = name)
          .filter { triple -> triple.objekt == name && triple.subject != propertyUrl }
      if (result.isNotEmpty()) {
        store.convertAndSave(source = propertyUrl, subject = propertyUrl,
            property = DISAMBIGUATED_FROM_URL, objeck = name)
        result.forEach {
          store.convertAndSave(source = it.source ?: it.subject!!,
              subject = it.subject!!, property = DISAMBIGUATED_FROM_URL, objeck = it.objekt!!)
        }
      }
      logger.warn("$index of ${notMappedProperties.size} properties written. " +
          "time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
    }

    (store as? KnowledgeStoreFkgTripleDaoImpl)?.flush()
    (store as? VirtuosoFkgTripleDaoImpl)?.close()
  }
}