package ir.ac.iust.dml.kg.access.dao.knowldegestore

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1triplesApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TripleData
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import org.apache.log4j.Logger
import java.util.*
import java.util.regex.Pattern

class KnowledgeStoreFkgTripleDaoImpl : FkgTripleDao() {

  private val logger = Logger.getLogger(this.javaClass)!!
  val FLUSH_SIZE = 10000
  val tripleApi: V1triplesApi
  val buffer = mutableListOf<TripleData>()

  init {
    val client = ApiClient()
    client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
    client.connectTimeout = 4800000
    tripleApi = V1triplesApi(client)
  }

  override fun flush() {
    while (buffer.isNotEmpty()) {
      try {
        logger.info("flushing ...")
        if (tripleApi.batchInsert2(buffer)) buffer.clear()
      } catch (e: Throwable) {
        logger.error(e)
      }
    }
  }

  private val p = Pattern.compile("[\\\\|`\"<>{}^\\[\\]]", Pattern.CASE_INSENSITIVE);
  private fun isValidUri(uri: String): Boolean {
    val m = p.matcher(uri)
    return !m.find()
  }

  override fun save(t: FkgTriple, mapping: FkgPropertyMapping?, approved: Boolean) {
    if (t.objekt == null || t.objekt!!.trim().isEmpty()) {
      logger.error("short triple here: ${t.source} ${t.predicate} ${t.objekt}")
      return
    }
    if (t.objekt!!.length > 250) {
      logger.error("too long triple here: ${t.source} ${t.predicate} ${t.objekt}")
      t.objekt = t.objekt!!.substring(0, 250)
    }
    val data = TripleData()
    data.context = URIs.defaultContext
    data.module = t.module ?: "wiki"
    data.urls = Collections.singletonList(t.source)
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

    val objectData = TypedValueData()
    objectData.type =
        if (URIs.isHttpUriFast(t.objekt))
          TypedValueData.TypeEnum.RESOURCE
        else TypedValueData.TypeEnum.STRING

    objectData.value = t.objekt
    objectData.lang =
        if (t.language == null) LanguageChecker.detectLanguage(objectData.value)
        else t.language!!
    data.`object` = objectData

    if (mapping != null) {
      data.precession = if (mapping.language == "en") 1.0 else mapping.status?.getPrecession()
      data.parameters = mapOf(
          "templateName" to mapping.templateName.toString(),
          "templateProperty" to mapping.templateProperty.toString(),
          "templatePropertyLanguage" to mapping.templatePropertyLanguage.toString(),
          "ontologyClass" to mapping.ontologyClass.toString(),
          "ontologyProperty" to mapping.ontologyProperty.toString(),
          "language" to mapping.language.toString(),
          "status" to mapping.status.toString(),
          "tupleCount" to mapping.tupleCount.toString(),
          "approved" to mapping.approved.toString()
      )
    }

    if (t.version != null) data.parameters["version"] = t.version
    if (t.extractionTime != null) data.parameters["extractionTime"] = t.extractionTime.toString()
    if (t.rawText != null) data.parameters["rawText"] = t.rawText


    if (approved) {
      // TODO: approve and write to knowledge store`
    }

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
        if (tripleApi.batchInsert2(buffer)) buffer.clear()
      } catch (th: Throwable) {
        logger.error(th)
      }
    }
  }

  override fun delete(subject: String, predicate: String, `object`: String) {
    tripleApi.remove1(subject, predicate, `object`, URIs.defaultContext)
  }

  override fun deleteAll() {
  }

  override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
    // TODO not implemented
    return PagedData(mutableListOf(), 0, 0, 0, 0)
  }

  override fun read(subject: String?, predicate: String?, objekt: String?, status: MappingStatus?): MutableList<FkgTriple> {
    val list = mutableListOf<FkgTriple>()
    val result = tripleApi.search1(null, false, subject, false, predicate,
        false, objekt, false, null, null)
    result.data.forEach {
      list.add(FkgTriple(source = it.sources.firstOrNull()?.urls?.firstOrNull(),
          subject = it.subject, predicate = it.predicate, objekt = it.`object`?.value))
    }
    return list
  }
}