package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.mapper.logic.store.OntologyLogic
import ir.ac.iust.dml.kg.mapper.logic.store.data.OntologyClassData
import ir.ac.iust.dml.kg.mapper.logic.store.data.OntologyPropertyData
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

  @RequestMapping("classTree", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun classTree(
      @RequestParam(required = false) root: String?,
      @RequestParam(required = false) depth: Int?,
      @RequestParam(required = false, defaultValue = "false") label: Boolean) = logic.classTree(root, depth, label)

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

  @RequestMapping("saveClass", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun saveClass(@RequestBody classData: OntologyClassData)
      = logic.saveClass(classData)

  @RequestMapping("saveProperty", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun saveProperty(@RequestBody propertyData: OntologyPropertyData)
      = logic.saveProperty(propertyData)

}