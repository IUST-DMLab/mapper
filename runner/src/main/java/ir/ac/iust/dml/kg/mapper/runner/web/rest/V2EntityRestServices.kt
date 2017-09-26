/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.mapper.logic.V2EntityViewer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/entity/rest/v2/")
@Api(tags = arrayOf("entity/v2"), description = "سرویس‌های مربوط به موجودیت، نسخه دوم")
class V2EntityRestServices {
  @Autowired lateinit var logic: V2EntityViewer

  data class EntityURLs(var entities: MutableList<String> = mutableListOf())

  @RequestMapping("getEntityData", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun getEntityData(@RequestParam url: String,
                    @RequestParam(required = false, defaultValue = "true") properties: Boolean)
      = logic.getEntityData(url, properties)

  @RequestMapping("getEntities", method = arrayOf(RequestMethod.POST))
  @ResponseBody
  fun getEntityData(@RequestBody urls: EntityURLs) = logic.getEntities(urls.entities)
}