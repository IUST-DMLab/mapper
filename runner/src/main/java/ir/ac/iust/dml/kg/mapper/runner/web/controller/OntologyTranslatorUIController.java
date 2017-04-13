package ir.ac.iust.dml.kg.mapper.runner.web.controller;

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
            "You can export data in <a href='translator/export.json'>json</a> or " +
            "<a href='translator/export.xml'>xml</a>.<br/>" +
            "<br/><hr/>" +
            "Search:" +
            "<form action='translator/rest/v1/search?like=true'>" +
            "title:<input name='name'><br/>" +
            "<input type='hidden' name='like' value='true'>" +
            "<button>Search!</button>" +
            "</form>" +
            "<br/><hr/>" +
            "for exact search, call <br/>" +
            "translator/rest/v1/root <br/>" +
            "translator/rest/v1/node/{title} <br/>" +
            "translator/rest/v1/children/{title} <br/>" +
            "translator/rest/v1/parent/{title} <br/>" +
            "for example:<br/>" +
            "http://localhost:8090/translator/rest/v1/root<br/>" +
            "http://localhost:8090/translator/rest/v1/node/Thing<br/>" +
            "http://localhost:8090/translator/rest/v1/children/Thing<br/>" +
            "http://localhost:8090/translator/rest/v1/parent/Athlete</br>" +
            "<hr/>" +
            "You can get data in pages. Every parameters are optional:<br/>" +
            "translator/rest/v1/search?page={page}&pageSize={pageSize}name={name}" +
            "&parent={parent}&like={searches more deeper}&approved={approved}&hasFarsi={hasFarsi}<br/>" +
            "like:<br/>" +
            "http://localhost:8090/translator/rest/v1/search?page=0&pageSize=20&name=Ath" +
            "&parent=Person&like=true&approved=&hasFarsi=</br>" +
            "<hr/>" +
            "for change translation call GET:<br/>" +
            "translator/rest/v1/translate?name={ontology title}&faLabel={persian label}&approved={approved}" +
            "&faOtherLabels={other persian labels}&amp;note={any notes}<br/>" +
            "Like this:<br/>" +
            "http://localhost:8090/translator/rest/v1/translate?name=Athlete&approved=true&" +
            "&faLabel=ورزشکار&faOtherLabels=بازیکن&amp;note=هنوز%20مطمئن%20نیستم<br/>" +
            "<hr/>" +
            "Check it out to ensure changes have effected<br/>" +
            "http://localhost:8090/translator/rest/v1/node/Athlete<br/>" +
            "<hr/>" +
            "or POST: translator/rest/v1/translate with this request body:<br/>" +
            "{<br/>" +
            "  \"name\" : \"ontology title\"<br/>" +
            "  \"faLabel\" : \"persian label\"<br/>" +
            "  \"faOtherLabels\" : \"other persian labels\"<br/>" +
            "  \"note\" : \"any notes\"<br/>" +
            "  \"approved\" : \"approved\"<br/>" +
            "}";
  }
}
