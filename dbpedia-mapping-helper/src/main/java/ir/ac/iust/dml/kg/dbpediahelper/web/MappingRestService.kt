package ir.ac.iust.dml.kg.dbpediahelper.web

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.dbpediahelper.logic.TemplateToClassLogic
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.TemplateToClassData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mapping/rest/v1/")
@Api(tags = arrayOf("mapping"), description = "سرویس‌های نگاشت")
class MappingRestService {

   @Autowired lateinit var templateToClassLogic: TemplateToClassLogic

   @RequestMapping("exportAll", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun exportAll(@RequestParam(required = false) after: Long?)
         = templateToClassLogic.exportAll(after)

   @RequestMapping("search", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun search(@RequestParam page: Int,
              @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
              @RequestParam(required = false) templateName: String?,
              @RequestParam(required = false) className: String?,
              @RequestParam(required = false, defaultValue = "false") like: Boolean?,
              @RequestParam(required = false) language: String,
              @RequestParam(required = false) approved: Boolean?,
              @RequestParam(required = false) after: Long? = null)
         = templateToClassLogic.search(page = page, pageSize = pageSize!!,
         templateName = templateName, className = className, like = like!!,
         language = language, approved = approved, after = after)

   @RequestMapping("data", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun getEditData(@RequestParam(required = false) id: Long?) = templateToClassLogic.getEditData(id)

   @RequestMapping("editByGet", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun editData(@RequestParam(required = false) id: Long?,
                @RequestParam templateName: String,
                @RequestParam(required = false) className: String?,
                @RequestParam language: String,
                @RequestParam approved: Boolean) =
         templateToClassLogic.edit(TemplateToClassData(
               id = id, language = language, templateName = templateName,
               className = className, approved = approved))

   @RequestMapping("edit", method = arrayOf(RequestMethod.POST))
   @ResponseBody
   fun editData(@RequestBody data: TemplateToClassData) = templateToClassLogic.edit(data)
}