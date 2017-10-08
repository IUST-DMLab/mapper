package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.mapper.logic.data.OntologyClassData
import ir.ac.iust.dml.kg.mapper.logic.data.OntologyPropertyData
import ir.ac.iust.dml.kg.mapper.logic.ontology.OntologyLogic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ontology/rest/v1/")
@Api(tags = arrayOf("ontology"), description = "سرویس‌های ویرایش آنتولوژی")
class OntologyRestServices {
  @Autowired lateinit var logic: OntologyLogic
  val sepratorRegex = Regex("[,\\s]+")

  @RequestMapping("reload", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun reload() = logic.reloadTreeCache()

  @RequestMapping("findCommonRoot", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun findCommonRoot(@RequestParam classes: String) = logic.findCommonRoot(classes.split(sepratorRegex))

  @RequestMapping("classes", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun classes(@RequestParam(required = false, defaultValue = "0") page: Int?,
              @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
              @RequestParam(required = false) keyword: String?)
      = logic.classes(page!!, pageSize!!, keyword)

  @RequestMapping("classTree", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun classTree(
      @RequestParam(required = false) root: String?,
      @RequestParam(required = false) depth: Int?,
      @RequestParam(required = false) labelLanguage: String?) = logic.classTree(root, depth, labelLanguage)

  @RequestMapping("classData", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun classData(@RequestParam(required = false) classUrl: String?) = logic.classData(classUrl!!)

  @RequestMapping("properties", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun properties(@RequestParam(required = false, defaultValue = "0") page: Int?,
                 @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
                 @RequestParam(required = false) keyword: String?,
                 @RequestParam(required = false) type: String?) =
      logic.properties(page!!, pageSize!!, keyword, type)

  @RequestMapping("ontologyPredicates", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun ontologyPredicates(@RequestParam keyword: String) =
      logic.ontologyPredicates(keyword)

  @RequestMapping("propertyData", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun propertyData(@RequestParam(required = false) propertyData: String?)
      = logic.propertyData(propertyData!!)

  @RequestMapping("removeClass", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun removeClass(@RequestParam classUrl: String)
      = logic.removeClass(classUrl)

  @RequestMapping("saveClass", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun saveClass(@RequestBody classData: OntologyClassData)
      = logic.saveClass(classData)

  @RequestMapping("saveProperty", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun saveProperty(@RequestBody propertyData: OntologyPropertyData)
      = logic.saveProperty(propertyData)

  @RequestMapping("removePropertyFromClass", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun removePropertyFromClass(
      @RequestParam propertyUrl: String,
      @RequestParam classUrl: String)
      = logic.removePropertyFromClass(propertyUrl, classUrl)

  @RequestMapping("removePropertyCompletely", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun removePropertyCompletely(@RequestParam propertyUrl: String)
      = logic.removePropertyCompletely(propertyUrl)

}