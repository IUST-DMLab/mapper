package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.mapper.logic.ManualInsertLogic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/manual/rest/v1/")
@Api(tags = arrayOf("manual"), description = "سرویس‌های افزودن دستی سه‌تایی")
class ManualInsertRestService {
  @Autowired lateinit var logic: ManualInsertLogic

  @RequestMapping("any", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun any(@RequestParam subjectUrl: String,
          @RequestParam predicateUrl: String,
          @RequestParam objectUrl: String,
          @RequestParam(defaultValue = "true") permanent: Boolean) =
      logic.saveTriple(subjectUrl, predicateUrl, objectUrl, permanent)

  @RequestMapping("predicate", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun predicate(@RequestParam predicate: String,
                @RequestParam(required = false) label: String?,
                @RequestParam(required = false) variantLabel: String?,
                @RequestParam(defaultValue = "true") permanent: Boolean) =
      logic.savePredicate(predicate, label, variantLabel, permanent)

  @RequestMapping("resource", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun resource(@RequestParam resource: String,
               @RequestParam(required = false) ontologyClassName: String?,
               @RequestParam(required = false) label: String?,
               @RequestParam(required = false) variantLabel: String?,
               @RequestParam(defaultValue = "true") permanent: Boolean) =
      logic.saveResource(resource, ontologyClassName, label, variantLabel, permanent)

}