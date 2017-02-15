package ir.ac.iust.dml.kg.templateequalities.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/template")
public class HelloController {

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot! " +
                "<br/>" +
                "Put the dump file in ~/pkg/wikipedia_dump.txt and load data <a href='load'>here</a>" +
                "<br/>" +
                "Search:" +
                "<form action='/rest/v1/mapping'>" +
                "lang:<input name='lang' value='fa'><br/>" +
                "type:<input name='type'><br/>" +
                "title:<input name='title'><br/>" +
                "<button>Search!</button>" +
                "</form>" +
                "<br/>" +
                "for exact search, call <br/>" +
                "/rest/v1/mapping/{lang}/{title} <br/>" +
                "/rest/v1/mapping/{lang}/{infobox_type}/{title} <br/>" +
                "for example:<br/>" +
                "http://localhost/rest/v1/mapping/fa/birth_place" +
                "http://localhost/rest/v1/mapping/fa/person/birth_place";
    }
}
