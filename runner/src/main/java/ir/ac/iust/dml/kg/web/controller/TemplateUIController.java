package ir.ac.iust.dml.kg.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/template")
public class TemplateUIController {

    @RequestMapping({"/", ""})
    public String index() {
        return "Greetings from Spring Boot! " +
                "<br/>" +
                "Put the dump file in ~/.pkg/wikipedia_dump.txt and load data <a href='template/load'>here</a>" +
                "<br/>" +
                "You can export data in <a href='template/export.json'>json</a> or " +
                "<a href='template/export.xml'>xml</a>." +
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
                "template/rest/v1/mapping/{lang}/{title} <br/>" +
                "template/rest/v1/mapping/{lang}/{infobox_type}/{title} <br/>" +
                "for example:<br/>" +
                "http://localhost:8090/template/rest/v1/mapping/fa/birth_place<br/>" +
                "http://localhost:8090/template/rest/v1/mapping/fa/rail%20line/نام دیگر<br/>" +
                "http://localhost:8090/template/rest/v1/mapping/en/rail%20line/other_name";
    }
}
