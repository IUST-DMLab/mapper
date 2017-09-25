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
import ir.ac.iust.dml.kg.services.client.swagger.V2triplesApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TripleData
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import org.apache.log4j.Logger
import java.util.regex.Pattern

class KnowledgeStoreFkgTripleDaoImpl : FkgTripleDao() {

  private val logger = Logger.getLogger(this.javaClass)!!
  private val FLUSH_SIZE = 1000
  private val tripleApi: V2triplesApi
  private val buffer = mutableListOf<TripleData>()

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 4800000
    tripleApi = V2triplesApi(client)
  }

  override fun flush() {
    while (buffer.isNotEmpty()) {
      try {
        logger.info("flushing ...")
        if (tripleApi.batchInsert5(buffer)) buffer.clear()
      } catch (e: Throwable) {
        logger.error(e)
      }
    }
  }

  override fun newVersion(module: String) = tripleApi.newVersion2(module)

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
    val data = TripleData()
    data.context = URIs.defaultContext
    data.module = t.module
    data.version = t.version
    data.url = t.source
    data.subject = t.subject
    data.predicate = if (!t.predicate!!.contains("://")) URIs.prefixedToUri(t.predicate) else t.predicate
    data.precession = t.accuracy
    if (!URIs.isHttpUriFast(t.predicate)) {
      logger.error("wrong subject format: " + data.predicate + ": " + t.predicate)
      return
    }
    if (!URIs.isHttpUriFast(t.subject)) {
      logger.error("wrong subject format: " + data.subject + ": " + t.subject)
      return
    }

    val objectData = convertTypedValue(t.objekt!!, t.valueType, t.language)
    data.`object` = objectData
    data.approved = t.approved != null && t.approved!!

    t.properties.forEach { data.properties[it.predicate] = convertTypedValue(it.objekt!!, it.valueType, it.language) }

    if (t.dataType != null) data.parameters["unit"] = t.dataType
    if (t.extractionTime != null) data.parameters["extractionTime"] = t.extractionTime.toString()
    if (t.rawText != null) data.parameters["rawText"] = t.rawText

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
    if (buffer.size > FLUSH_SIZE) {
      try {
        logger.info("batch insert ...")
        if (tripleApi.batchInsert5(buffer)) buffer.clear()
      } catch (th: Throwable) {
        logger.error(th)
      }
    }
  }

  private fun convertTypedValue(objekt: String, valueType: ValueType?, language: String?): TypedValueData {
    val objectData = TypedValueData()
    objectData.type =
        if (URIs.isHttpUriFast(objekt))
          TypedValueData.TypeEnum.RESOURCE
        else {
          if (valueType != null) convert(valueType)
          else TypedValueData.TypeEnum.STRING
        }

    objectData.value = objekt
    objectData.lang = language ?: LanguageChecker.detectLanguage(objectData.value)
    return objectData
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
  }

  override fun delete(subject: String, predicate: String, `object`: String) {
    tripleApi.remove3(subject, predicate, `object`, URIs.defaultContext)
  }

  override fun deleteAll() {
    // TODO not implementeds
  }

  override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
    // TODO not implemented
    return PagedData(mutableListOf(), 0, 0, 0, 0)
  }

  override fun read(subject: String?, predicate: String?, objekt: String?): MutableList<FkgTriple> {
    // TODO not implemented
    return mutableListOf()
  }
}