package ir.ac.iust.dml.kg.mapper.runner.web.filter

import javax.servlet.*
import javax.servlet.http.HttpServletResponse


class SimpleCORSFilter : Filter {

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val httpServletResponse = response as HttpServletResponse
    httpServletResponse.setHeader("Access-Control-Allow-Origin", "*")
    httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true")
    httpServletResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE")
    httpServletResponse.setHeader("Access-Control-Max-Age", "3600")
    httpServletResponse.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
    chain.doFilter(request, response)
  }

  override fun destroy() {
  }

  override fun init(config: FilterConfig?) {
  }
}