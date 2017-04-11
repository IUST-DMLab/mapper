package ir.ac.iust.dml.kg.web.controller

import io.swagger.annotations.Api
import ir.ac.iust.dml.kg.dbpediahelper.logic.StatsLogic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/stats/rest/v1/")
@Api(tags = arrayOf("stats"), description = "جمع‌کننده آمار")
class StatsRestService {

   @Autowired lateinit var logic: StatsLogic

}