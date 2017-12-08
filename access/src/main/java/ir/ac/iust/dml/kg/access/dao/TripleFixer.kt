/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.FkgTripleProperty
import ir.ac.iust.dml.kg.knowledge.core.ValueType
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import org.apache.log4j.Logger
import java.util.regex.Pattern

object TripleFixer {

  private val logger = Logger.getLogger(this.javaClass)!!

  private val p = Pattern.compile("[\\\\|`\"<>{}^\\[\\]]", Pattern.CASE_INSENSITIVE);
  private fun isValidUri(uri: String): Boolean {
    val m = p.matcher(uri)
    return !m.find()
  }

  fun fix(t: FkgTriple): Boolean {
    if (t.objekt == null || t.objekt!!.trim().isEmpty()) {
      logger.error("short triple here: ${t.source} ${t.predicate} ${t.objekt}")
      return false
    }
    if (t.predicate == null) return false
    if (!t.predicate!!.contains("://")) t.predicate = URIs.prefixedToUri(t.predicate)
    if (!URIs.isHttpUriFast(t.predicate)) {
      logger.error("wrong subject format: " + t.subject + ": " + t.predicate)
      return false
    }
    if (!URIs.isHttpUriFast(t.subject)) {
      logger.error("wrong subject format: " + t.subject + ": " + t.predicate)
      return false
    }

    // TODO:
    if ((!isValidUri(t.subject!!))) {
      logger.error("wrong subject url: " + t.subject)
      return false
    }

    if ((!isValidUri(t.predicate!!))) {
      logger.error("wrong predicate url: " + t.predicate)
      return false
    }

    if (t.valueType == TypedValueData.TypeEnum.RESOURCE && !isValidUri(t.objekt!!)) {
      logger.error("wrong object url: " + t.objekt!!)
      return false
    }

    if (t.valueType == null)
      t.valueType =
          if (URIs.isHttpUriFast(t.objekt!!)) ValueType.Resource
          else ValueType.String
    if (t.language == null)
      t.language = if (t.valueType == ValueType.Resource) null
      else LanguageChecker.detectLanguage(t.objekt)

    for (p in t.properties)
      if (!fix(p)) return false

    return true
  }

  private fun fix(t: FkgTripleProperty): Boolean {
    if (t.objekt == null || t.objekt!!.trim().isEmpty()) {
      logger.error("short triple here: ${t.parent.subject} ${t.predicate} ${t.objekt}")
      return false
    }
    if (t.predicate == null) return false
    if (!t.predicate!!.contains("://")) t.predicate = URIs.prefixedToUri(t.predicate)
    if (!URIs.isHttpUriFast(t.predicate)) {
      logger.error("wrong subject format: " + t.parent.subject + ": " + t.predicate)
      return false
    }

    if ((!isValidUri(t.predicate!!))) {
      logger.error("wrong predicate url: " + t.predicate)
      return false
    }

    if (t.valueType == TypedValueData.TypeEnum.RESOURCE && !isValidUri(t.objekt!!)) {
      logger.error("wrong object url: " + t.objekt!!)
      return false
    }

    if (t.valueType == null)
      t.valueType =
          if (URIs.isHttpUriFast(t.objekt!!)) ValueType.Resource
          else ValueType.String
    if (t.language == null)
      t.language = if (t.valueType == ValueType.Resource) null
      else LanguageChecker.detectLanguage(t.objekt)
    return true
  }
}