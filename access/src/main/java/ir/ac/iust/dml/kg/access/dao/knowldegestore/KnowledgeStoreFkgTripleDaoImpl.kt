package ir.ac.iust.dml.kg.access.dao.knowldegestore

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1triplesApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TripleData
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import ir.ac.iust.dml.kg.utils.PrefixService
import org.apache.log4j.Logger
import java.util.*

class KnowledgeStoreFkgTripleDaoImpl : FkgTripleDao {

   private val logger = Logger.getLogger(this.javaClass)!!
   val tripleApi: V1triplesApi
   val buffer = mutableListOf<TripleData>()

   init {
      val client = ApiClient()
      client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8091/rs")
      tripleApi = V1triplesApi(client)
   }

   fun flush() {
      while (buffer.isNotEmpty()) {
         try {
            logger.info("flushing ...")
            tripleApi.batchInsert1(buffer)
            buffer.clear()
         } catch (e: Throwable) {
            logger.error(e)
         }
      }
   }

   override fun save(t: FkgTriple, mapping: FkgPropertyMapping?) {
      if (t.objekt == null || t.objekt!!.trim().isEmpty()) {
         println("short triple here: ${t.source} ${t.predicate} ${t.objekt}")
         return
      }
      val data = TripleData()
      data.context = "http://fkg.iust.ac.ir/"
      data.module = "wiki"
      data.urls = Collections.singletonList(t.source)
      data.subject = t.subject
      data.predicate = if (!t.predicate!!.contains("://")) PrefixService.prefixToUri(t.predicate) else t.predicate
      if (!data.predicate.contains("://")) {
         logger.error(data.predicate + ": " + t.predicate)
         return
      }

      val objectData = TypedValueData()
      objectData.type =
            if (t.objekt!!.contains("://") && !t.objekt!!.contains(' '))
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

      buffer.add(data)
      if (buffer.size > 10000) {
         try {
            logger.info("batch insert ...")
            tripleApi.batchInsert1(buffer)
            buffer.clear()
         } catch (th: Throwable) {
            logger.error(th)
         }
      }
   }

   override fun deleteAll() {
   }

   override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
      // TODO not implemented
      return PagedData(mutableListOf(), 0, 0, 0, 0)
   }

   override fun read(subject: String?, predicate: String?, objekt: String?, status: MappingStatus?): MutableList<FkgTriple> {
      // TODO not implemented
      return mutableListOf()
   }
}