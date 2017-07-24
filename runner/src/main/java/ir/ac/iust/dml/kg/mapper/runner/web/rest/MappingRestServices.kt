package ir.ac.iust.dml.kg.mapper.runner.web.rest

import com.google.gson.GsonBuilder
import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.mapper.logic.store.KSMappingLogic
import ir.ac.iust.dml.kg.services.client.swagger.model.TemplateData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/mapping/rest/v2/")
@Api(tags = arrayOf("mapping"), description = "سرویس‌های ویرایش نگاشت")
class MappingRestServices {
  @Autowired lateinit var logic: KSMappingLogic
  val gson = GsonBuilder().setPrettyPrinting().create()

  @RequestMapping("insert", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun insert(@RequestBody data: TemplateData) = logic.insert(data)

  @RequestMapping("search", method = arrayOf(RequestMethod.GET),
      produces = arrayOf("application/json; charset=utf-8"))
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
             @RequestParam(required = false) approved: Boolean?): String {
    val result = logic.search(page, pageSize, templateName, templateNameLike, className, classNameLike,
        propertyName, propertyNameLike, predicateName, predicateNameLike, approved)
    return gson.toJson(result)
  }

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

}