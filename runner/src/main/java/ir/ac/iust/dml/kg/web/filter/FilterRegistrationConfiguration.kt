package ir.ac.iust.dml.kg.web.filter

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
open class FilterRegistrationConfiguration {

  @Bean
  open fun someFilterRegistration(): FilterRegistrationBean {
    val registration = FilterRegistrationBean()
    registration.filter = SimpleCORSFilter()
    registration.addUrlPatterns("/*")
    registration.setName("corsFilter")
    registration.order = 1
    return registration
  }
}