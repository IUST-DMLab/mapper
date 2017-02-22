package ir.ac.iust.dml.kg.ontologytranslator.web;

import ir.ac.iust.dml.kg.ontologytranslator.logic.Importer;
import ir.ac.iust.dml.kg.ontologytranslator.logic.Translator;
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.ExportData;
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.OntologyClassTranslationData;
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.OntologyTranslatorExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/translator")
public class OntologyTranslatorRestServices {
  @Autowired
  private Importer importer;
  @Autowired
  private Translator translator;
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

  @RequestMapping(value = "/rest/v1/root")
  @ResponseBody
  public OntologyClassTranslationData root() throws Exception {
    return translator.getRoot();
  }

  @RequestMapping(value = "/rest/v1/node/{name}")
  @ResponseBody
  public OntologyClassTranslationData getNode(@PathVariable String name) throws Exception {
    return translator.getNode(name);
  }

  @RequestMapping(value = "/rest/v1/parent/{name}")
  @ResponseBody
  public OntologyClassTranslationData getParent(@PathVariable String name) throws Exception {
    return translator.getParent(name);
  }

  @RequestMapping(value = "/rest/v1/children/{name}")
  @ResponseBody
  public List<OntologyClassTranslationData> getChildren(@PathVariable String name) throws Exception {
    return translator.getChildren(name);
  }

  @RequestMapping(value = "/rest/v1/translate", method = RequestMethod.GET)
  @ResponseBody
  public Boolean translateGet(@RequestParam String name,
                              @RequestParam String faLabel,
                              @RequestParam String faOtherLabels,
                              @RequestParam String note) throws Exception {
    return translator.translate(new OntologyClassTranslationData(name, null, null,
            faLabel, faOtherLabels, note));
  }

  @RequestMapping(value = "/rest/v1/translate", method = RequestMethod.POST)
  @ResponseBody
  public Boolean translatePost(@RequestBody OntologyClassTranslationData data) throws Exception {
    return translator.translate(data);
  }
}
