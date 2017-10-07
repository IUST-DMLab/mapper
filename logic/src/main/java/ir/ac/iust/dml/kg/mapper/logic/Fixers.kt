/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V2ontologyApi
import ir.ac.iust.dml.kg.services.client.swagger.model.Ontology
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValue
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

  fun findOntologyMoreThanOneLables() {
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
    if (faLabels.size > 1) logger.info("${it.subject}\tfa\t${faLabels.joinToString("\t")}")
//    if (enLabels.size > 1) logger.info("${it.subject}\ten\t${enLabels.joinToString("\t")}")
  }
}