package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.StoreProvider
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ManualInsertLogic {
  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired lateinit var provider: StoreProvider
  @Autowired private lateinit var ontologyLogic: OntologyLogic
  private var initialized = false
  private lateinit var virtuosoDao: FkgTripleDao
  private lateinit var kgDao: FkgTripleDao

  fun init() {
    initialized = true
    virtuosoDao = provider.getStore(StoreType.virtuoso)
    kgDao = provider.getStore(StoreType.knowledgeStore)
    ontologyLogic.reloadTreeCache()
  }

  fun saveResource(resourceUrl: String, ontologyClass: String?, label: String?,
                   variantLabel: String?, permanent: Boolean): Boolean {
    if (!initialized) init()
    val url = if (!resourceUrl.startsWith("http://")) URIs.getFkgResourceUri(resourceUrl) else resourceUrl
    saveTriple(url, URIs.type, URIs.typeOfAllResources, permanent)
    if (label != null) {
      saveTriple(url, URIs.label, label, permanent)
      saveTriple(url, URIs.variantLabel, label, permanent)
      checkAmbiguity(url, label)
    }
    if (variantLabel != null) {
      saveTriple(url, URIs.variantLabel, variantLabel, permanent)
      checkAmbiguity(url, variantLabel)
    }
    if (ontologyClass != null) {
      val tree = ontologyLogic.getTree(ontologyClass)
      tree?.split("/")?.forEach { saveTriple(url, URIs.type, URIs.convertAnyUrisToFkgOntologyUri(it), permanent) }
    }
    return true
  }

  fun savePredicate(predicateUrl: String, label: String?, variantLabel: String?, permanent: Boolean): Boolean {
    if (!initialized) init()
    val url = if (!predicateUrl.startsWith("http://")) URIs.getFkgOntologyPropertyUri(predicateUrl) else predicateUrl
    saveTriple(url, URIs.type, URIs.typeOfAllProperties, permanent)
    if (label != null) {
      saveTriple(url, URIs.label, label, permanent)
      saveTriple(url, URIs.variantLabel, label, permanent)
      checkAmbiguity(url, label)
    }
    if (variantLabel != null) {
      saveTriple(url, URIs.variantLabel, variantLabel, permanent)
      checkAmbiguity(url, variantLabel)
    }
    return true
  }

  // TODO share this piece of code with PredicateImporter
  fun checkAmbiguity(subject: String, label: String) {
    if (!initialized) init()
    val result = kgDao.read(predicate = URIs.variantLabel, objekt = label)
        .filter { triple -> triple.objekt == label && triple.subject != subject }
    if (result.isNotEmpty()) {
      saveAll(source = subject, subject = subject, property = URIs.disambiguatedFrom, objeck = label)
      result.forEach {
        saveAll(source = it.source ?: it.subject!!, subject = it.subject!!,
            property = URIs.disambiguatedFrom, objeck = it.objekt!!)
      }
    }
  }

  fun saveAll(source: String, subject: String, objeck: String, property: String) {
    if (!initialized) init()
    kgDao.save(source, subject, objeck, property)
    kgDao.flush()
    virtuosoDao.save(source, subject, objeck, property)
  }

  fun saveTriple(subjectUrl: String, predicateUrl: String, objectUrl: String, permanent: Boolean): Boolean {
    if (!initialized) init()
    val sourceUrl = URIs.getFkgManualUri(subjectUrl.substringAfterLast('/'))
    logger.info("save permanent: $permanent")
    virtuosoDao.save(sourceUrl, subjectUrl, objectUrl, predicateUrl)
    kgDao.save(sourceUrl, subjectUrl, objectUrl, predicateUrl)
    kgDao.flush()
    return true
  }
}