package ir.ac.iust.dml.kg.ontologytranslator.web;

import ir.ac.iust.dml.kg.ontologytranslator.logic.Importer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translator")
public class OntologyTranslatorRestServices {
  @Autowired
  private Importer importer;

  @RequestMapping("/import")
  public String load() throws Exception {
    importer.importFromDb();
    return "Imported!";
  }
}
