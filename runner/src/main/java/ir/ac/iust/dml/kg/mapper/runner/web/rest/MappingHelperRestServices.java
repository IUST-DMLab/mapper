package ir.ac.iust.dml.kg.mapper.runner.web.rest;

import ir.ac.iust.dml.kg.dbpediahelper.logic.*;
import ir.ac.iust.dml.kg.dbpediahelper.logic.export.ExportData;
import ir.ac.iust.dml.kg.dbpediahelper.logic.export.TemplateToOntologyExporter;
import ir.ac.iust.dml.kg.dbpediahelper.logic.store.*;
import ir.ac.iust.dml.kg.raw.utils.PrefixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/helper")
public class MappingHelperRestServices {
  @Autowired
  private MappingLoader helperLoader;
  @Autowired
  private StatsLogic statsLogic;
  @Autowired
  private TripleImporter tripleImporter;
  @Autowired
  private TemplateToOntologyExporter templateToOntologyExporter;
  @Autowired
  private EntityToClassLogic entityToClassLogic;
  @Autowired
  private PropertyMappingLogic propertyMappingLogic;
  @Autowired
  private RedirectLogic redirectLogic;
  @Autowired
  private AmbiguityLogic ambiguityLogic;
  @Autowired
  private MigrationManager migrationManager;
  @Autowired
  private KSMappingHolder ksMappingHolder;
  @Autowired
  private KGTripleImporter kgTripleImporter;
  @Autowired
  private KGTableImporter KGTableImporter;
  @Autowired
  private PredicateImporter predicateImporter;

  @RequestMapping("/migrate")
  public String migrate() throws Exception {
    migrationManager.migrate();
    migrationManager.save();
    return "Migrated!";
  }

  @RequestMapping("/ksMapLoad")
  public String ksMapLoad() throws Exception {
    ksMappingHolder.loadFromKS();
    return "Loaded!";
  }

  @RequestMapping("/prefixes")
  public String prefixes() throws Exception {
    PrefixService.INSTANCE.reload();
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

  @RequestMapping("/createStatsFile")
  public String createStatsFile() throws Exception {
    statsLogic.createStatsFile();
    return "Stats created!";
  }

  @RequestMapping("/writeStats")
  public String writeStats() throws Exception {
    statsLogic.writeStats();
    return "Stats created!";
  }

  @RequestMapping("/generateMapping")
  public String generateMapping() {
    return String.valueOf(propertyMappingLogic.generateMapping());
  }

  @RequestMapping("/triples")
  public String triples(@RequestParam(defaultValue = "none") TripleImporter.StoreType type) throws Exception {
    tripleImporter.processTripleInputFiles(type);
    return "Imported!";
  }

  @RequestMapping("/kgTriples")
  public String kgTriples(@RequestParam(defaultValue = "none") TripleImporter.StoreType type) throws Exception {
    kgTripleImporter.writeTriples(type);
    return "Imported!";
  }

  @RequestMapping("/kgTables")
  public String kgTables(@RequestParam(defaultValue = "none") TripleImporter.StoreType type) throws Exception {
    KGTableImporter.writeTriples(type);
    return "Imported!";
  }

  @RequestMapping("/rewriteLabels")
  public String rewriteLabels(@RequestParam(defaultValue = "none") TripleImporter.StoreType type) throws Exception {
    kgTripleImporter.rewriteLabels(type);
    return "Imported!";
  }

  @RequestMapping("/allTriples")
  public String allTriples(@RequestParam(defaultValue = "none") TripleImporter.StoreType type) throws Exception {
    kgTripleImporter.writeTriples(type);
    kgTripleImporter.rewriteLabels(type);
    redirectLogic.write(type);
    ambiguityLogic.write(type);
    return "Imported!";
  }

  @RequestMapping("/redirects")
  public String redirects() throws Exception {
    redirectLogic.write(TripleImporter.StoreType.knowledgeStore);
    return "Imported!";
  }

  @RequestMapping("/ambiguities")
  public String ambiguities() throws Exception {
    ambiguityLogic.write(TripleImporter.StoreType.knowledgeStore);
    return "Imported!";
  }

  @RequestMapping("/entities")
  public String entities() throws Exception {
    entityToClassLogic.writeEntityTypesToKnowledgeStore();
    return "Imported!";
  }

  @RequestMapping("/relations")
  public String relations() throws Exception {
    propertyMappingLogic.writeResourcesToKnowledgeStore();
    return "Imported!";
  }

  @RequestMapping("/fixWikiTemplateMapping")
  public String fixWikiTemplateMapping() throws Exception {
    tripleImporter.fixWikiTemplateMapping();
    return "Fixed!";
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

  @RequestMapping("/predicates")
  @ResponseBody
  public Boolean predicates(@RequestParam(defaultValue = "none") TripleImporter.StoreType type) {
    predicateImporter.writePredicates(type);
    return true;
  }

  @RequestMapping("/completeDumpUpdate")
  public void completeDumpUpdate(@RequestParam(defaultValue = "none") TripleImporter.StoreType type) throws Exception {
    migrationManager.migrate();
    migrationManager.save();
    KGTableImporter.writeTriples(type);
    allTriples(type);
    kgTripleImporter.rewriteLabels(type);
    entityToClassLogic.writeEntityTypesToKnowledgeStore();
    predicateImporter.writePredicates(type);
  }
}
