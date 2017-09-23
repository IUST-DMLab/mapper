/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.ontology

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ir.ac.iust.dml.kg.raw.utils.PageUtils
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.log4j.Logger
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import javax.annotation.PostConstruct

@Service
class DataTypeService {

  val logger = Logger.getLogger(this.javaClass)!!

  private val dataTypes = mutableListOf<String>()

  @PostConstruct
  fun load() {
    val gson = Gson()
    // loading offline file that downloaded from // http://dbpedia.org/data3/data/definitions.ttl.json
    val type = object : TypeToken<Map<String, Map<String, List<Map<String, String>>>>>() {}.type
    val map: Map<String, Map<String, List<Map<String, String>>>> = gson.fromJson(
        InputStreamReader(DataTypeService::class.java.getResourceAsStream("/definitions.ttl.json"),
            "UTF-8"), type)
    val fkgURL = URIs.prefixedToUri(URIs.fkgMainPrefix + ":")!!
    map["http://dbpedia.org/ontology/data/definitions.ttl"]!!["http://open.vocab.org/terms/describes"]!!.forEach {
      val value = it["value"]!!
      if (!value.contains("dbpedia.org/ontology"))
        dataTypes.add(value.replace("http://dbpedia.org/", fkgURL))
    }
    dataTypes.sortBy { it }
    logger.info("${dataTypes.size} data types has been loaded from dbpedia definitions")
  }

  fun getDataTypes(page: Int, pageSize: Int, keyword: String?): PagedData<String> {
    val lcKeyword = keyword?.toLowerCase()
    val filtered =
        if (lcKeyword == null || lcKeyword.isBlank()) dataTypes
        else dataTypes.filter { it.toLowerCase().contains(lcKeyword) }
    return PageUtils.asPages(page, pageSize, filtered)
  }
}