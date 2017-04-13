package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.dbpediahelper.logic.TemplateMappingLogic
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.FkgTemplateMappingData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/templateMapping/rest/v1/")
@Api(tags = arrayOf("templateMapping"), description = "سرویس‌های نگاشت کلاس")
class TemplateMappingRestService {

   @Autowired lateinit var templateMappingLogic: TemplateMappingLogic

   @RequestMapping("exportAll", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun exportAll(@RequestParam(required = false) after: Long?)
         = templateMappingLogic.exportAll(after)

   @RequestMapping("updateCounts", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun exportAll() = templateMappingLogic.updateCounts()

   @RequestMapping("search", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun search(@RequestParam(required = false, defaultValue = "0") page: Int?,
              @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
              @RequestParam(required = false) templateName: String?,
              @RequestParam(required = false) className: String?,
              @RequestParam(required = false, defaultValue = "false") like: Boolean?,
              @RequestParam(required = false) language: String?,
              @RequestParam(required = false) approved: Boolean?,
              @RequestParam(required = false) after: Long? = null)
         = templateMappingLogic.search(page = page!!, pageSize = pageSize!!,
         templateName = templateName, className = className, like = like!!,
         language = language, approved = approved, after = after)

   @RequestMapping("data", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun getEditData(@RequestParam(required = false) id: Long?) = templateMappingLogic.getEditData(id)

   @RequestMapping("editByGet", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun editData(@RequestParam(required = false) id: Long?,
                @RequestParam templateName: String,
                @RequestParam(required = false) ontologyClass: String?,
                @RequestParam language: String,
                @RequestParam approved: Boolean) =
         templateMappingLogic.edit(FkgTemplateMappingData(
               id = id, language = language, templateName = templateName,
               ontologyClass = ontologyClass, approved = approved))

   @RequestMapping("edit", method = arrayOf(RequestMethod.POST))
   @ResponseBody
   fun editData(@RequestBody data: FkgTemplateMappingData) = templateMappingLogic.edit(data)

   @RequestMapping("searchTemplateName", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun searchTemplateName(@RequestParam(required = false, defaultValue = "0") page: Int?,
                          @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
                          @RequestParam keyword: String?) =
         templateMappingLogic.searchTemplateName(page!!, pageSize!!, keyword)

   @RequestMapping("searchOntologyClass", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun searchOntologyClass(@RequestParam(required = false, defaultValue = "0") page: Int?,
                           @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
                           @RequestParam keyword: String?) =
         templateMappingLogic.searchOntologyClass(page!!, pageSize!!, keyword)
}