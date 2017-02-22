package ir.ac.iust.dml.kg.ontologytranslator.web;

import ir.ac.iust.dml.kg.ontologytranslator.logic.Importer;
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.ExportData;
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.OntologyTranslatorExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translator")
public class OntologyTranslatorRestServices {
  @Autowired
  private Importer importer;
  @Autowired
  private OntologyTranslatorExporter translatorExporter;

  @RequestMapping("/import")
  public String load() throws Exception {
    importer.importFromDb();
    return "Imported!";
  }

  @RequestMapping(value = "/export")
  @ResponseBody
  public ExportData export() throws Exception {
    return translatorExporter.export();
  }
}
