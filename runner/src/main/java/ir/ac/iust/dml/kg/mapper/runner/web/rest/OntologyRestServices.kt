package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.OntologyLogic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ontology/rest/v1/")
@Api(tags = arrayOf("ontology"), description = "سرویس‌های ویرایش آنتولوژی")
class OntologyRestServices {
  @Autowired lateinit var logic: OntologyLogic

  @RequestMapping("classes", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun classes(@RequestParam(required = false, defaultValue = "0") page: Int?,
              @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
              @RequestParam(required = false) keyword: String?) = logic.classes(page!!, pageSize!!, keyword)

  @RequestMapping("classData", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun classData(@RequestParam(required = false) classUrl: String?) = logic.classData(classUrl!!)

  @RequestMapping("properties", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun properties(@RequestParam(required = false, defaultValue = "0") page: Int?,
                 @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
                 @RequestParam(required = false) keyword: String?) =
      logic.properties(page!!, pageSize!!, keyword)

  @RequestMapping("propertyData", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun propertyData(@RequestParam(required = false) propertyData: String?)
      = logic.propertyData(propertyData!!)

}