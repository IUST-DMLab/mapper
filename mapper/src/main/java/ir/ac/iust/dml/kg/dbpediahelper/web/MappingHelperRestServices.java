package ir.ac.iust.dml.kg.dbpediahelper.web;

import ir.ac.iust.dml.kg.dbpediahelper.logic.EntityToClassLogic;
import ir.ac.iust.dml.kg.dbpediahelper.logic.MappingLoader;
import ir.ac.iust.dml.kg.dbpediahelper.logic.PrefixService;
import ir.ac.iust.dml.kg.dbpediahelper.logic.export.ExportData;
import ir.ac.iust.dml.kg.dbpediahelper.logic.export.TemplateToOntologyExporter;
import ir.ac.iust.dml.kg.dbpediahelper.logic.triple.TripleImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/helper")
public class MappingHelperRestServices {
  @Autowired
  private PrefixService prefixService;
  @Autowired
  private MappingLoader helperLoader;
  @Autowired
  private TripleImporter tripleImporter;
  @Autowired
  private TemplateToOntologyExporter templateToOntologyExporter;
  @Autowired
  private EntityToClassLogic entityToClassLogic;

  @RequestMapping("/prefixes")
  public String prefixes() throws Exception {
    prefixService.reload();
    return "Reloaded!";
  }

  @RequestMapping("/load")
  public String load() throws Exception {
    helperLoader.writeDbpediaEnglishMapping();
    return "Loaded!";
  }


  @RequestMapping("/loadTypes")
  public String loadTypes() throws Exception {
    entityToClassLogic.load();
    return "Loaded!";
  }

  @RequestMapping("/triples")
  public String triples(@RequestParam(defaultValue = "none") TripleImporter.StoreType type) throws Exception {
    tripleImporter.processTripleInputFiles(type);
    return "Imported!";
  }

  @RequestMapping("/fixWikiTemplateMapping")
  public String fixWikiTemplateMapping() throws Exception {
    tripleImporter.fixWikiTemplateMapping();
    return "Fixed!";
  }

  @RequestMapping("/writeStats")
  public String triples() throws Exception {
    tripleImporter.writeStats();
    return "Stats created!";
  }

  @RequestMapping("/generate")
  public String generate() throws Exception {
    helperLoader.generatePersian();
    return "Generated!";
  }

  @RequestMapping("/generateByCount")
  public String generateByCount() throws Exception {
    helperLoader.generateByCount();
    return "Generated!";
  }

  @RequestMapping("/export")
  @ResponseBody
  public ExportData exportXml(@RequestParam(required = false) String language) throws Exception {
    return templateToOntologyExporter.export(language);
  }
}
