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
//                "Reload prefixes by clicking <a href='helper/prefixes'>here</a>" +
//                "<br/>" +
                "You can export data in <a href='helper/export.json'>json</a> or " +
                "<a href='helper/export.xml'>xml</a>.<br/>" +
                "You can export data in a specific language by sending language parameters to above services:<br/>" +
                "<a href='helper/export.json?language=fa'>json</a> or " +
                "<a href='helper/export.xml?language=fa'>xml</a>." +
                "<br/>" +
                "Search:" +
                "<form action='template/rest/v1/mapping'>" +
                "lang:<input name='lang' value='fa'><br/>" +
                "type:<input name='type'><br/>" +
                "title:<input name='title'><br/>" +
                "<button>Search!</button>" +
                "</form>" +
                "<br/>" +
                "for exact search, call <br/>" +
                "helper/rest/v1/mapping/{lang}/{title} <br/>" +
                "helper/rest/v1/mapping/{lang}/{infobox_type}/{title} <br/>" +
                "for example:<br/>" +
                "http://localhost:8090/helper/rest/v1/mapping/fa/birth_place<br/>" +
                "http://localhost:8090/helper/rest/v1/mapping/fa/rail%20line/نام دیگر<br/>" +
                "http://localhost:8090/helper/rest/v1/mapping/en/rail%20line/other_name";
    }
}
