/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.dao.knowldegestore

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.knowledge.core.ValueType
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V2ontologyApi
import ir.ac.iust.dml.kg.services.client.swagger.model.OntologyData
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import org.apache.log4j.Logger
import java.util.regex.Pattern

class OntologyStoreFkgTripleDaoImpl : FkgTripleDao() {

  private val logger = Logger.getLogger(this.javaClass)!!
  private val flushSize = ConfigReader.getInt("store.batch.size", "1000")
  private val tripleApi: V2ontologyApi
  private val buffer = mutableListOf<OntologyData>()

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = ConfigReader.getInt("store.timeout", "4800000")
    tripleApi = V2ontologyApi(client)
  }

  override fun flush() {
    while (buffer.isNotEmpty()) {
      try {
        logger.info("flushing ...")
        if (tripleApi.batchInsert4(buffer).isNotEmpty()) buffer.clear()
      } catch (e: Throwable) {
        logger.error(e)
      }
    }
  }

  override fun newVersion(module: String) = 1

  override fun activateVersion(module: String, version: Int) = true

  private val p = Pattern.compile("[\\\\|`\"<>{}^\\[\\]]", Pattern.CASE_INSENSITIVE);
  private fun isValidUri(uri: String): Boolean {
    val m = p.matcher(uri)
    return !m.find()
  }

  override fun save(t: FkgTriple) {
    if (t.objekt == null || t.objekt!!.trim().isEmpty()) {
      logger.error("short triple here: ${t.source} ${t.predicate} ${t.objekt}")
      return
    }
    if (t.objekt!!.length > 250) {
      logger.error("too long triple here: ${t.source} ${t.predicate} ${t.objekt}")
      t.objekt = t.objekt!!.substring(0, 250)
    }
    val data = OntologyData()
    data.context = URIs.defaultContext
    data.subject = t.subject
    data.predicate = if (!t.predicate!!.contains("://")) URIs.prefixedToUri(t.predicate) else t.predicate
    if (!URIs.isHttpUriFast(t.predicate)) {
      logger.error("wrong subject format: " + data.predicate + ": " + t.predicate)
      return
    }
    if (!URIs.isHttpUriFast(t.subject)) {
      logger.error("wrong subject format: " + data.subject + ": " + t.subject)
      return
    }
    data.approved = true

    val objectData = TypedValueData()
    objectData.type =
        if (URIs.isHttpUriFast(t.objekt))
          TypedValueData.TypeEnum.RESOURCE
        else {
          if (t.valueType != null) convert(t.valueType!!)
          else TypedValueData.TypeEnum.STRING
        }

    objectData.value = t.objekt
    objectData.lang =
        if (t.language == null) LanguageChecker.detectLanguage(objectData.value)
        else t.language!!
    data.`object` = objectData

    // TODO:
    if ((!isValidUri(data.subject))) {
      logger.error("wrong subject url: " + data.subject)
      return
    }

    if ((!isValidUri(data.predicate))) {
      logger.error("wrong predicate url: " + data.predicate)
      return
    }

    if (data.`object`.type == TypedValueData.TypeEnum.RESOURCE && !isValidUri(data.`object`.value)) {
      logger.error("wrong object url: " + data.`object`.value)
      return
    }

    buffer.add(data)
    if (buffer.size > flushSize) {
      try {
        logger.info("batch insert ...")
        if (tripleApi.batchInsert4(buffer).isNotEmpty()) buffer.clear()
      } catch (th: Throwable) {
        logger.error(th)
      }
    }
  }

  private fun convert(valueType: ValueType) = when (valueType) {
    ValueType.String -> TypedValueData.TypeEnum.STRING
    ValueType.Integer -> TypedValueData.TypeEnum.INTEGER
    ValueType.Double -> TypedValueData.TypeEnum.DOUBLE
    ValueType.Resource -> TypedValueData.TypeEnum.RESOURCE
    ValueType.Boolean -> TypedValueData.TypeEnum.BOOLEAN
    ValueType.Byte -> TypedValueData.TypeEnum.BYTE
    ValueType.Float -> TypedValueData.TypeEnum.FLOAT
    ValueType.Long -> TypedValueData.TypeEnum.LONG
    ValueType.Short -> TypedValueData.TypeEnum.SHORT
    ValueType.Date -> TypedValueData.TypeEnum.DATE
  }

  override fun delete(subject: String, predicate: String, `object`: String) {
    tripleApi.remove2(subject, predicate, `object`, URIs.defaultContext)
  }

  override fun deleteAll() {
  }

  override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
    // TODO not implemented
    return PagedData(mutableListOf(), 0, 0, 0, 0)
  }

  override fun read(subject: String?, predicate: String?, objekt: String?): MutableList<FkgTriple> {
    val list = mutableListOf<FkgTriple>()
    val result = tripleApi.search2(null, null, subject, null, predicate,
        null, objekt, null, false, null, null)
    result.data.forEach {
      list.add(FkgTriple(source = it.subject, subject = it.subject, predicate = it.predicate,
          objekt = it.`object`?.value))
    }
    return list
  }
}