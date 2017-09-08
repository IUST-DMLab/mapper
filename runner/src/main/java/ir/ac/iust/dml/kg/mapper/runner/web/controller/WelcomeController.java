package ir.ac.iust.dml.kg.mapper.runner.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class WelcomeController {
  @RequestMapping({"/", ""})
  public String index() {
    return "Greetings from Spring Boot! " +
            "<br/>" +
            "Select one of these options:" +
            "<br/>" +
            "<a href='/swagger-ui.html'>All public services</a>";
  }
}
