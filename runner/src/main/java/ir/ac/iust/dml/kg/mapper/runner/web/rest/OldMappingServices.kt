/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.runner.web.rest

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mapping/rest/v1/")
@Api(tags = arrayOf("propertyMapping"), description = "سرویس‌های نگاشت خصیصه")
class OldMappingServices {

  private val prefixAddresses = mutableMapOf<String, String>()

  init {
    val fkgPrefixConvert = ConfigReader.getString("fkg.prefix.convert", "fkg")
    URIs.prefixAddresses.forEach {
      prefixAddresses[it.key] = it.value.replace("fkg", fkgPrefixConvert)
    }
  }

  @RequestMapping("prefixes", method = arrayOf(RequestMethod.GET))
  @ResponseBody
  fun prefixes() = prefixAddresses
}