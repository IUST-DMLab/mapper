package ir.ac.iust.dml.kg.dbpediahelper.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/helper")
public class MappingHelperUIController {

  @RequestMapping({"/", ""})
  public String index() {
    return "Greetings from Spring Boot! " +
            "<br/>" +
            "Put the dbpedia english mapping dump file in ~/.pkg/dbpedia_mapping.owl " +
            "and load data <a href='helper/load'>here</a>" +
            "<br/>" +
            "To Generate Persian mapping, click <a href='helper/generate'>here</a>" +
            "<br/>" +
            "To Generate Triples without saving (for checking console outputs), " +
            "click <a href='helper/triples?type=none'>here</a>" +
            "<br/>" +
            "To Generate Triples on json files, click <a href='helper/triples?type=file'>here</a>" +
            "<br/>" +
            "To Generate Triples on mysql, click <a href='helper/triples?type=mysql'>here</a>" +
            "<br/>" +
//                "Reload prefixes by clicking <a href='helper/prefixes'>here</a>" +
//                "<br/>" +
            "You can export data in <a href='helper/export.json'>json</a> or " +
            "<a href='helper/export.xml'>xml</a>.<br/>" +
            "You can export data in a specific language by sending language parameters to above services:<br/>" +
            "<a href='helper/export.json?language=fa'>json</a> or " +
            "<a href='helper/export.xml?language=fa'>xml</a>.";
  }
}
