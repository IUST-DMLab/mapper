/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.PropertyNormaller
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V2ontologyApi
import ir.ac.iust.dml.kg.services.client.swagger.model.Ontology
import ir.ac.iust.dml.kg.services.client.swagger.model.OntologyData
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValue
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import org.apache.log4j.Logger
import org.springframework.stereotype.Service

/**
 * It's a temporary class which finds bugs in data and fix them before main release
 */
@Service
class Fixers {
  private val logger = Logger.getLogger(this.javaClass)!!
  private val ontologyApi: V2ontologyApi

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 1200000
    ontologyApi = V2ontologyApi(client)
  }

  fun findOntologyMoreThanOneLabels() {
    // list classes
    val classes = ontologyApi.search2(null, null, null, null,
        URIs.type, null, URIs.typeOfAllClasses, null, null, 0, 0)
    classes.data.forEach { findDuplicatedLabels(it) }
    val properties = ontologyApi.search2(null, null, null, null,
        URIs.type, null, URIs.typeOfAnyProperties, null, null, 0, 0)
    properties.data.forEach { findDuplicatedLabels(it) }
  }

  private fun findDuplicatedLabels(it: Ontology) {
    val labels = ontologyApi.search2(null, null, it.subject, null, URIs.label, null,
        null, null, null, 0, 0)
    if (labels.data.size < 2) return
    val faLabels = mutableListOf<String>()
    val enLabels = mutableListOf<String>()
    labels.data.forEach {
      if (it.`object` != null && TypedValue.TypeEnum.RESOURCE != it.`object`.type) {
        if ("en" == it.`object`.lang) enLabels.add(it.`object`.value)
        else if ("fa" == it.`object`.lang) faLabels.add(it.`object`.value)
      }
    }
    if (faLabels.size > 1) {
      logger.info("${it.subject} ==>\t${faLabels.joinToString(";\t")}")
      fixLabelIfItsPossible(it.subject, faLabels)
    }
    if (enLabels.size > 1) {
      logger.info("${it.subject} ==>\t${enLabels.joinToString(";\t")}")
      fixLabelIfItsPossible(it.subject, enLabels)
    }
  }

  private fun fixLabelIfItsPossible(url: String, oldLabels: List<String>) {
    val uniqueFaLabels = oldLabels.map { PropertyNormaller.removeDigits(it) }.toSet()
    if (uniqueFaLabels.size == 1) {
      val fixedLabel = uniqueFaLabels.iterator().next()
      oldLabels.forEach {
        ontologyApi.remove2(url, URIs.label, it, URIs.defaultContext)
      }
      insertOntologyLiteral(url, URIs.label, fixedLabel)
      logger.info("label of $url, has been to $fixedLabel")
    }
  }

  private fun insertOntologyLiteral(subject: String, predicate: String, `object`: String) {
    val o = OntologyData()
    o.subject = subject
    o.predicate = predicate
    o.`object` = TypedValueData()
    o.`object`.type = TypedValueData.TypeEnum.STRING
    o.`object`.lang = LanguageChecker.detectLanguage(`object`)
    o.`object`.value = `object`
    o.approved = true
    o.context = URIs.defaultContext
    ontologyApi.insert6(o)
  }
}