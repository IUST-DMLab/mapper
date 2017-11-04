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
import java.net.URI
import java.net.URL

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

  fun findWrongResources() {
    var page = 0
    do {
      val triples = ontologyApi.search2(null, null, null, null,
          null, null, null, null, null, page++, 10000)
      triples.data.forEach {
        if (it.`object`.type == TypedValue.TypeEnum.RESOURCE)
          try {
            URI(it.`object`.value)
            URL(it.`object`.value)
          } catch (th: Throwable) {
            logger.info("wrong resource: ${it.subject}, ${it.predicate} ${it.`object`.value}")
            it.`object`.type = TypedValue.TypeEnum.STRING
            val data = convert(it)
            ontologyApi.insert6(data)
          }
      }
    } while (triples.page < triples.pageCount)
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
      fixLabelIfItsPossible(it.subject, faLabels, "fa")
    }
    if (enLabels.size > 1) {
      logger.info("${it.subject} ==>\t${enLabels.joinToString(";\t")}")
      fixLabelIfItsPossible(it.subject, enLabels, "en")
    }
  }

  private fun fixLabelIfItsPossible(url: String, oldLabels: List<String>, lang: String) {
    if (oldLabels.any { LanguageChecker.detectLanguage(it) != lang }) return
    val uniqueFaLabels = oldLabels.map { PropertyNormaller.removeDigits(it) }.toSet()
    if (uniqueFaLabels.size == 1 || uniqueFaLabels.size == 2) {
      val sorted = uniqueFaLabels.sortedByDescending { it.length }
      if (sorted.size == 2 && (sorted[0].length - sorted[1].length < 2 || (lang == "fa"))) return
      val fixedLabel = if (uniqueFaLabels.size == 1) uniqueFaLabels.iterator().next()
      else sorted.first()
      oldLabels.forEach {
        ontologyApi.remove2(url, URIs.label, it, URIs.defaultContext)
      }
      insertOntologyLiteral(url, URIs.label, fixedLabel)
      logger.info("label of $url, has been to $fixedLabel")
    }
  }

  fun convert(ontology: Ontology): OntologyData? {
    if (ontology.`object`.value.isBlank()) return null
    val tripleData = OntologyData()
    tripleData.context = URIs.defaultContext
    tripleData.subject = ontology.subject
    tripleData.predicate = ontology.predicate
    tripleData.`object` = TypedValueData()
    tripleData.`object`.lang = LanguageChecker.detectLanguage(ontology.`object`.value)
    tripleData.`object`.type = TypedValueData.TypeEnum.valueOf(ontology.`object`.type.name)
    tripleData.`object`.value = ontology.`object`.value
    tripleData.approved = true
    return tripleData
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