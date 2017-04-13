package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.dbpediahelper.logic.EntityToClassLogic
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.FkgEntityClassesData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/entityType/rest/v1/")
@Api(tags = arrayOf("entityType"), description = "سرویس‌های نوع موجودیت")
class EntityToClassRestService {

   @Autowired lateinit var logic: EntityToClassLogic

//   @RequestMapping("exportTypes", method = arrayOf(RequestMethod.GET))
//   fun exportTypes(@RequestParam(required = false) after: Long?,
//                   response: HttpServletResponse)
//         = logic.exportTypes(after, response)

   @RequestMapping("exportTypes", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun exportTypes(@RequestParam(required = false) after: Long?) = logic.exportTypes(after)

   @RequestMapping("classParents", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun classParents() = logic.classParents()

   @RequestMapping("exportAll", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun exportAll(@RequestParam(required = false) after: Long?)
         = logic.search(page = 0, pageSize = 0, after = after).data

   @RequestMapping("search", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun search(@RequestParam(required = false, defaultValue = "0") page: Int?,
              @RequestParam(required = false, defaultValue = "20") pageSize: Int?,
              @RequestParam(required = false) entity: String?,
              @RequestParam(required = false) className: String?,
              @RequestParam(required = false, defaultValue = "false") like: Boolean?,
              @RequestParam(required = false) approved: Boolean?,
              @RequestParam(required = false) status: MappingStatus?,
              @RequestParam(required = false) after: Long? = null)
         = logic.search(page = page!!, pageSize = pageSize!!,
         entity = entity, className = className, like = like!!,
         approved = approved, after = after, status = status)

   @RequestMapping("data", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun getEditData(@RequestParam(required = false) id: Long?) = logic.getEditData(id)

   @RequestMapping("entity", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun getEditDataByEntity(@RequestParam entity: String) = logic.getEntity(entity)

   @RequestMapping("editByGet", method = arrayOf(RequestMethod.GET))
   @ResponseBody
   fun editData(@RequestParam(required = false) id: Long?,
                @RequestParam entity: String,
                @RequestParam(required = false) className: String?,
                @RequestParam approved: Boolean,
                @RequestParam(required = false) status: MappingStatus?) =
         logic.edit(FkgEntityClassesData(id = id, entity = entity, className = className,
               approved = approved, status = status))

   @RequestMapping("edit", method = arrayOf(RequestMethod.POST))
   @ResponseBody
   fun editData(@RequestBody data: FkgEntityClassesData) = logic.edit(data)
}