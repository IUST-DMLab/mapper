package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.access.dao.virtuoso.ExportFormat
import ir.ac.iust.dml.kg.mapper.logic.virtuoso.FinalDataLogic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.OutputStream

@RestController
@RequestMapping("/virtuoso/rest/v1/")
@Api(tags = arrayOf("virtuoso/v1"), description = "سرویس‌های مربوط به ویرتوسو")
class FinalDataRestServices {
  @Autowired lateinit var logic: FinalDataLogic

  @RequestMapping("getTriplesOfSubject", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun getTriplesOfSubject(@RequestParam subjectUrl: String) = logic.getTriplesOfSubject(subjectUrl)

  @RequestMapping("export", method = arrayOf(RequestMethod.GET))
  fun export(@RequestParam subjectUrl: String,
             @RequestParam format: ExportFormat,
             out: OutputStream) = logic.export(subjectUrl, format, out)
}