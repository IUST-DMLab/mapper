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
            "and writeDbpediaEnglishMapping data <a href='helper/writeDbpediaEnglishMapping'>here</a>" +
            "<br/>" +
            "To Generate Persian mapping, click <a href='helper/generate'>here</a>" +
            "<br/>" +
            "To generate statistical data to file <a href='helper/createStatsFile'>here</a>" +
            "<br/>" +
            "To write statistical data to mysql <a href='helper/writeStats'>here</a>" +
            "<br/>" +
            "To generate mapings to mysql <a href='helper/generateMapping'>here</a>" +
            "<br/>" +
            "To Generate Triples without saving (for checking console outputs), " +
            "click <a href='helper/triples?type=none'>here</a>" +
            "<br/>" +
            "To Generate Triples on json files, click <a href='helper/triples?type=file'>here</a>" +
            "<br/>" +
            "To Generate Triples on mysql, click <a href='helper/triples?type=mysql'>here</a>" +
            "<br/>" +
            "write classes of entities to database <a href='helper/loadTypes'>here</a>" +
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
