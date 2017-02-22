package ir.ac.iust.dml.kg.ontologytranslator.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translator")
public class OntologyTranslatorUIController {

  @RequestMapping({"/", ""})
  public String index() {
    return "Greetings from Spring Boot! " +
            "<br/>" +
            "reload data from dbpedia_ontology_classes table <a href='translator/import'>here</a>" +
            "<br/>" +
            "You can export data in <a href='translator/export/json'>json</a> or " +
            "<a href='translator/export/xml'>xml</a>.<br/>" +
            "<br/>" +
            "Search:" +
            "<form action='template/rest/v1/mapping'>" +
            "title:<input name='title'><br/>" +
            "<button>Search!</button>" +
            "</form>" +
            "<br/>" +
            "for exact search, call <br/>" +
            "translator/rest/v1/root <br/>" +
            "translator/rest/v1/node/{title} <br/>" +
            "translator/rest/v1/children/{title} <br/>" +
            "translator/rest/v1/parent/{title} <br/>" +
            "for example:<br/>" +
            "http://localhost:8080/translator/rest/v1/root<br/>" +
            "http://localhost:8080/translator/rest/v1/node/Thing<br/>" +
            "http://localhost:8080/translator/rest/v1/children/Thing<br/>" +
            "http://localhost:8080/translator/rest/v1/parent/Athlete";
  }
}
