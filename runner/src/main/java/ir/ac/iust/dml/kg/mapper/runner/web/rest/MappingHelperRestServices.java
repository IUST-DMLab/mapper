package ir.ac.iust.dml.kg.mapper.runner.web.rest;

import ir.ac.iust.dml.kg.mapper.logic.*;
import ir.ac.iust.dml.kg.mapper.logic.export.ExportData;
import ir.ac.iust.dml.kg.mapper.logic.export.TemplateToOntologyExporter;
import ir.ac.iust.dml.kg.mapper.logic.store.*;
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType;
import ir.ac.iust.dml.kg.raw.utils.PrefixService;
import org.jetbrains.annotations.NotNull;
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
  private KGTableImporter kgTableImporter;
  @Autowired
  private PredicateImporter predicateImporter;
  @Autowired
  private NotMappedPropertyHandler notMappedPropertyHandler;
  @Autowired
  private RawTripleImporter rawTripleImporter;

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

  @RequestMapping("/withoutInfoBox")
  public String withoutInfoBox(@RequestParam(defaultValue = "none") StoreType type) throws Exception {
    kgTripleImporter.writeEntitiesWithoutInfoBox(type);
    return "Imported!";
  }

  @RequestMapping("/withInfoBox")
  public String withInfoBox(@RequestParam(defaultValue = "none") StoreType type) throws Exception {
    kgTripleImporter.writeEntitiesWithInfoBox(type);
    return "Imported!";
  }

  @RequestMapping("/abstracts")
  public String abstracts(@RequestParam(defaultValue = "none") StoreType type) throws Exception {
    kgTripleImporter.writeAbstracts(type);
    return "Imported!";
  }

  @RequestMapping("/triples")
  public String triples(@RequestParam(defaultValue = "none") StoreType type) throws Exception {
    kgTripleImporter.writeTriples(type);
    notMappedPropertyHandler.writeNotMappedProperties(type);
    return "Imported!";
  }

  @RequestMapping("/tables")
  public String tables(@RequestParam(defaultValue = "none") StoreType type) throws Exception {
    kgTableImporter.writeTriples(type);
    return "Imported!";
  }

  @RequestMapping("/redirects")
  public String redirects() throws Exception {
    redirectLogic.write(StoreType.knowledgeStore);
    return "Imported!";
  }

  @RequestMapping("/ambiguities")
  public String ambiguities() throws Exception {
    ambiguityLogic.write(StoreType.knowledgeStore);
    return "Imported!";
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
  public Boolean predicates(@RequestParam(defaultValue = "none") StoreType type) {
    predicateImporter.writePredicates(type);
    return true;
  }

  @RequestMapping("/completeDumpUpdate")
  public void completeDumpUpdate(@RequestParam(defaultValue = "none") StoreType type,
                                 @RequestParam(defaultValue = "false") boolean entitiesWithoutInfoBox) throws Exception {
    migrationManager.migrate();
    migrationManager.save();
    entityToClassLogic.writeTree(type);
    if (entitiesWithoutInfoBox) kgTripleImporter.writeEntitiesWithoutInfoBox(type);
    kgTripleImporter.writeEntitiesWithInfoBox(type);
    kgTripleImporter.writeTriples(type);
    kgTableImporter.writeTriples(type);
    rawTripleImporter.writeTriples(type);
    notMappedPropertyHandler.writeNotMappedProperties(type);
    kgTripleImporter.writeAbstracts(type);
    redirectLogic.write(type);
    ambiguityLogic.write(type);
    predicateImporter.writePredicates(type);
  }

  @RequestMapping("/writeTree")
  public void writeTree(@RequestParam(defaultValue = "none") StoreType type) {
    entityToClassLogic.writeTree(type);
  }

  public boolean raw(@NotNull StoreType type) {
    rawTripleImporter.writeTriples(type);
    notMappedPropertyHandler.writeNotMappedProperties(type);
    return true;
  }
}
