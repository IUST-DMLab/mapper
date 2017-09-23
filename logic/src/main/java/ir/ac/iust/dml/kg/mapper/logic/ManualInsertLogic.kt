/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.mapper.logic.ontology.OntologyLogic
import ir.ac.iust.dml.kg.mapper.logic.utils.StoreProvider
import ir.ac.iust.dml.kg.raw.utils.Module
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
  private val version = 1

  private fun init() {
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
    }
    if (variantLabel != null) {
      saveTriple(url, URIs.variantLabel, variantLabel, permanent)
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
    saveTriple(url, URIs.type, URIs.typeOfAnyProperties, permanent)
    saveTriple(url, URIs.type, URIs.defaultTypeOfOntologyProperties, permanent)
    if (label != null) {
      saveTriple(url, URIs.label, label, permanent)
      saveTriple(url, URIs.variantLabel, label, permanent)
    }
    if (variantLabel != null) {
      saveTriple(url, URIs.variantLabel, variantLabel, permanent)
    }
    return true
  }

  fun saveAll(source: String, subject: String, objeck: String, property: String) {
    if (!initialized) init()
    kgDao.save(source, subject, property, objeck, Module.manual.name, version)
    kgDao.flush()
    virtuosoDao.save(source, subject, property, objeck, Module.manual.name, version)
  }

  fun saveTriple(subjectUrl: String, predicateUrl: String, objectUrl: String, permanent: Boolean): Boolean {
    if (!initialized) init()
    val sourceUrl = URIs.getFkgManualUri(subjectUrl.substringAfterLast('/'))
    logger.info("save permanent: $permanent")
    virtuosoDao.save(sourceUrl, subjectUrl, predicateUrl, objectUrl, Module.manual.name, version)
    kgDao.save(sourceUrl, subjectUrl, predicateUrl, objectUrl, Module.manual.name, version)
    kgDao.flush()
    return true
  }
}