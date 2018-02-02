/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2018)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.wiki

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.util.UriEncoder
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType
import ir.ac.iust.dml.kg.mapper.logic.utils.PathUtils
import ir.ac.iust.dml.kg.mapper.logic.utils.StoreProvider
import ir.ac.iust.dml.kg.raw.utils.Module
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

@Service
class SameAsLogic {

  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired private lateinit var storeProvider: StoreProvider

  private val builder = HttpClientBuilder.create()

  data class VirtuosoObject(var type: String? = null, var value: String? = null)
  data class VirtuosoBinding(var o: VirtuosoObject? = null)
  data class VirtuosoResults(var distinct: Boolean = false, var ordered: Boolean = false,
                             val bindings: MutableList<VirtuosoBinding> = mutableListOf())

  data class VirtuosoJsonResponse(var results: VirtuosoResults)

  fun writeSameAs(version: Int, storeType: StoreType = StoreType.none) {
    val gson = Gson()
    val type = object : TypeToken<Map<String, String>>() {}.type
    val path = PathUtils.getInterLinkPath()
    val store = storeProvider.getStore(storeType)
    val startTime = System.currentTimeMillis()
    builder.setDefaultRequestConfig(RequestConfig.DEFAULT)

    val sameAs = URIs.sameAs
    var numberOfSameAs = 0
    InputStreamReader(FileInputStream(path.toFile()), "UTF8").use {
      BufferedReader(it).use {
        val pages: Map<String, String> = gson.fromJson(it, type)
        for ((index, page) in pages.keys.withIndex()) {
          if (page.toLowerCase().contains("template") || page.toLowerCase().contains("الگو")) continue
          val englishPage = pages[page]
          val subject = URIs.getFkgResourceUri(page)
          val dbpediaAddress = "http://dbpedia.org/resource/${englishPage!!.replace(' ', '_')}"
          store.save(subject, sameAs, dbpediaAddress, Module.sameAs.name, version)
          numberOfSameAs++
          try {
            val dbpediaQuery = "http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org" +
                "&query=select+%3Fo+%7B%0D%0A%3C${UriEncoder.decode(dbpediaAddress)}%3E+" +
                "%3Chttp%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23sameAs%3E+%3Fo+.%0D%0A%7D" +
                "&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=" +
                "&timeout=30000" +
                "&run=+Run+Query+"
            val request = HttpGet(dbpediaQuery)
            request.addHeader("accept", "application/sparql-results+json")
            builder.build()
            try {
              builder.build().use { client ->
                val result = client.execute(request)
                val json = EntityUtils.toString(result.entity, "UTF-8")
                val response = gson.fromJson<VirtuosoJsonResponse>(json, VirtuosoJsonResponse::class.java)
                response.results.bindings.forEach {
                  if (it.o != null && it.o!!.value != null) {
                    store.save(subject, sameAs, it.o!!.value!!, Module.wiki.name, version)
                    numberOfSameAs++
                  }
                }
              }
            } catch (e: Exception) {
              logger.error(e)
            }
          } catch (e: Throwable) {
            e.printStackTrace()
          }
          logger.warn("$index file is $page ($numberOfSameAs same as triples)." +
              " time elapsed is ${(System.currentTimeMillis() - startTime) / 1000} seconds")
        }
      }
    }

    store.flush()
  }
}