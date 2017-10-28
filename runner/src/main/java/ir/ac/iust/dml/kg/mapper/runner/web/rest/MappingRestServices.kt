package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.mapper.logic.mapping.KSMappingLogic
import ir.ac.iust.dml.kg.mapper.logic.mapping.TransformService
import ir.ac.iust.dml.kg.mapper.logic.ontology.DataTypeService
import ir.ac.iust.dml.kg.services.client.swagger.model.TemplateData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mapping/rest/v2/")
@Api(tags = arrayOf("mapping"), description = "سرویس‌های ویرایش نگاشت")
class MappingRestServices {
  @Autowired lateinit var logic: KSMappingLogic
  @Autowired lateinit var transformService: TransformService
  @Autowired lateinit var dataTypeService: DataTypeService

  @RequestMapping("transforms", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun transforms() = transformService.getTransforms()

  @RequestMapping("transformNames", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun transformNames() = transformService.getTransformNames()

  @RequestMapping("dataTypes", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun dataTypes(@RequestParam(defaultValue = "0") page: Int,
                @RequestParam(defaultValue = "20") pageSize: Int,
                @RequestParam(required = false) keyword: String?) =
      dataTypeService.getDataTypes(page, pageSize, keyword)

  @RequestMapping("insert", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun insert(@RequestBody data: TemplateData) = logic.insert(data)

  @RequestMapping("search", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun search(@RequestParam(defaultValue = "0") page: Int,
             @RequestParam(defaultValue = "20") pageSize: Int,
             @RequestParam(required = false) templateName: String?,
             @RequestParam(required = false, defaultValue = "false") templateNameLike: Boolean,
             @RequestParam(required = false) className: String?,
             @RequestParam(required = false, defaultValue = "false") classNameLike: Boolean,
             @RequestParam(required = false) propertyName: String?,
             @RequestParam(required = false, defaultValue = "false") propertyNameLike: Boolean,
             @RequestParam(required = false) predicateName: String?,
             @RequestParam(required = false, defaultValue = "false") predicateNameLike: Boolean,
             @RequestParam(required = false) approved: Boolean?) =
      logic.search(page, pageSize, templateName, templateNameLike, className, classNameLike,
          propertyName, propertyNameLike, predicateName, predicateNameLike, approved)

  @RequestMapping("searchProperty", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun searchProperty(@RequestParam(defaultValue = "0") page: Int,
                     @RequestParam(defaultValue = "20") pageSize: Int,
                     @RequestParam(required = false) templateName: String?,
                     @RequestParam(required = false, defaultValue = "false") templateNameLike: Boolean,
                     @RequestParam(required = false) className: String?,
                     @RequestParam(required = false, defaultValue = "false") classNameLike: Boolean,
                     @RequestParam(required = false) propertyName: String?,
                     @RequestParam(required = false, defaultValue = "false") propertyNameLike: Boolean,
                     @RequestParam(required = false) predicateName: String?,
                     @RequestParam(required = false, defaultValue = "false") predicateNameLike: Boolean,
                     @RequestParam(required = false) allNull: Boolean?,
                     @RequestParam(required = false) oneNull: Boolean?,
                     @RequestParam(required = false) approved: Boolean?) =
      logic.searchProperty(page, pageSize, propertyName, propertyNameLike, templateName, templateNameLike,
          className, classNameLike, predicateName, predicateNameLike, allNull, oneNull, approved)

  @RequestMapping("predicateProposal", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun predicateProposal(@RequestParam keyword: String) = logic.predicateProposal(keyword)

}