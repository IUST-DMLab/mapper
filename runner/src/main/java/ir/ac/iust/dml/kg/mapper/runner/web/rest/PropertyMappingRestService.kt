package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.mapper.logic.PropertyMappingLogic
import ir.ac.iust.dml.kg.mapper.logic.data.FkgPropertyMappingData
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mapping/rest/v1/")
@Api(tags = arrayOf("propertyMapping"), description = "سرویس‌های نگاشت خصیصه")
class PropertyMappingRestService {

  @Autowired lateinit var propertyMappingLogic: PropertyMappingLogic

  @RequestMapping("exportAll", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun exportAll(@RequestParam(required = false) after: Long?)
      = propertyMappingLogic.exportAll(after)

  @RequestMapping("search", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun search(@RequestParam(required = false, defaultValue = "0") page: Int?,
             @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
             @RequestParam(required = false) templateName: String?,
             @RequestParam(required = false) className: String?,
             @RequestParam(required = false) templateProperty: String?,
             @RequestParam(required = false) ontologyProperty: String?,
             @RequestParam(required = false, defaultValue = "false") like: Boolean?,
             @RequestParam(required = false) language: String?,
             @RequestParam(required = false) approved: Boolean?,
             @RequestParam(required = false) status: MappingStatus?,
             @RequestParam(required = false) after: Long? = null)
      = propertyMappingLogic.search(page = page!!, pageSize = pageSize!!,
      templateName = templateName, className = className,
      templateProperty = templateProperty, ontologyProperty = ontologyProperty, like = like!!,
      language = language, approved = approved, after = after, status = status)

  @RequestMapping("data", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun getEditData(@RequestParam(required = false) id: Long?) = propertyMappingLogic.getEditData(id)

  @RequestMapping("editByGet", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun editData(@RequestParam(required = false) id: Long?,
               @RequestParam templateName: String,
               @RequestParam(required = false) className: String?,
               @RequestParam templateProperty: String?,
               @RequestParam(required = false) ontologyProperty: String?,
               @RequestParam language: String,
               @RequestParam approved: Boolean,
               @RequestParam(required = false) status: MappingStatus?) =
      propertyMappingLogic.edit(FkgPropertyMappingData(
          id = id, language = language, templateName = templateName,
          ontologyClass = className, approved = approved, status = status,
          templateProperty = templateProperty, ontologyProperty = ontologyProperty))

  @RequestMapping("edit", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun editData(@RequestBody data: FkgPropertyMappingData) = propertyMappingLogic.edit(data)

  @RequestMapping("searchTemplateName", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun searchTemplateName(@RequestParam(required = false, defaultValue = "0") page: Int?,
                         @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
                         @RequestParam keyword: String?) =
      propertyMappingLogic.searchTemplateName(page!!, pageSize!!, keyword)

  @RequestMapping("searchOntologyClass", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun searchClassName(@RequestParam(required = false, defaultValue = "0") page: Int?,
                      @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
                      @RequestParam keyword: String?) =
      propertyMappingLogic.searchOntologyClass(page!!, pageSize!!, keyword)

  @RequestMapping("searchTemplatePropertyName", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun searchTemplatePropertyName(@RequestParam(required = false, defaultValue = "0") page: Int?,
                                 @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
                                 @RequestParam keyword: String?) =
      propertyMappingLogic.searchTemplatePropertyName(page!!, pageSize!!, keyword)

  @RequestMapping("searchOntologyPropertyName", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun searchOntologyPropertyName(@RequestParam(required = false, defaultValue = "0") page: Int?,
                                 @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
                                 @RequestParam keyword: String?) =
      propertyMappingLogic.searchOntologyPropertyName(page!!, pageSize!!, keyword)

  @RequestMapping("prefixes", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun prefixes() = URIs.prefixAddresses

  @RequestMapping("predicateExport", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun predicateExport(@RequestParam(required = false, defaultValue = "0") page: Int?,
                      @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
                      @RequestParam(required = false) keyword: String?,
                      @RequestParam(required = false) language: String?,
                      @RequestParam(required = false) ontologyClass: String?,
                      @RequestParam(required = false) templateName: String?,
                      @RequestParam(required = false) status: MappingStatus?) =
      propertyMappingLogic.predicateExport(page!!, pageSize!!, keyword, ontologyClass, templateName, status, language)
}