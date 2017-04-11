package ir.ac.iust.dml.kg.access.dao.knowldegestore

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.services.client.ApiClient
import ir.ac.iust.dml.kg.services.client.swagger.V1triplesApi
import ir.ac.iust.dml.kg.services.client.swagger.model.TripleData
import ir.ac.iust.dml.kg.services.client.swagger.model.TypedValueData
import java.util.*

class KnowledgeStoreFkgTripleDaoImpl : FkgTripleDao {

   val tripleApi: V1triplesApi

   init {
      val client = ApiClient()
      client.basePath = ConfigReader.getString("knowledge.store.url", "http://localhost:8080/rs")
      tripleApi = V1triplesApi(client)
   }

   override fun save(t: FkgTriple, mapping: FkgPropertyMapping?) {
      val data = TripleData()
      data.context = "default"
      data.module = "web"
      data.urls = Collections.singletonList(t.source)
      data.subject = t.subject
      data.predicate = t.predicate

      val objectData = TypedValueData()
      objectData.lang = "fa"
      objectData.type =
            if (t.objekt!!.contains("://")) TypedValueData.TypeEnum.RESOURCE
            else TypedValueData.TypeEnum.STRING
      objectData.value = t.objekt
      data.`object` = objectData

      if (mapping != null) {
         data.precession = mapping.status?.getPrecession()
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

      tripleApi.insert1(data)
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