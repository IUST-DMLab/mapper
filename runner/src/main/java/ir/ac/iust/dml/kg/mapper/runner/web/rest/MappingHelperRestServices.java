package ir.ac.iust.dml.kg.mapper.runner.web.rest;

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao;
import ir.ac.iust.dml.kg.access.entities.FkgTriple;
import ir.ac.iust.dml.kg.mapper.logic.Fixers;
import ir.ac.iust.dml.kg.mapper.logic.ProgressInformer;
import ir.ac.iust.dml.kg.mapper.logic.RawTripleImporter;
import ir.ac.iust.dml.kg.mapper.logic.TableTripleImporter;
import ir.ac.iust.dml.kg.mapper.logic.data.StoreType;
import ir.ac.iust.dml.kg.mapper.logic.mapping.KSMappingHolder;
import ir.ac.iust.dml.kg.mapper.logic.ontology.NotMappedPropertyHandler;
import ir.ac.iust.dml.kg.mapper.logic.ontology.OntologyLogic;
import ir.ac.iust.dml.kg.mapper.logic.ontology.PredicateImporter;
import ir.ac.iust.dml.kg.mapper.logic.utils.StoreProvider;
import ir.ac.iust.dml.kg.mapper.logic.wiki.AmbiguityLogic;
import ir.ac.iust.dml.kg.mapper.logic.wiki.RedirectLogic;
import ir.ac.iust.dml.kg.mapper.logic.wiki.SameAsLogic;
import ir.ac.iust.dml.kg.mapper.logic.wiki.WikiTripleImporter;
import ir.ac.iust.dml.kg.raw.utils.ConfigReader;
import ir.ac.iust.dml.kg.raw.utils.Module;
import ir.ac.iust.dml.kg.raw.utils.PagedData;
import ir.ac.iust.dml.kg.raw.utils.URIs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/helper")
public class MappingHelperRestServices {
  @Autowired
  private RedirectLogic redirectLogic;
  @Autowired
  private AmbiguityLogic ambiguityLogic;
  @Autowired
  private KSMappingHolder ksMappingHolder;
  @Autowired
  private SameAsLogic sameAsLogic;
  @Autowired
  private WikiTripleImporter wikiTripleImporter;
  @Autowired
  private TableTripleImporter tableTripleImporter;
  @Autowired
  private PredicateImporter predicateImporter;
  @Autowired
  private NotMappedPropertyHandler notMappedPropertyHandler;
  @Autowired
  private RawTripleImporter rawTripleImporter;
  @Autowired
  private OntologyLogic ontologyLogic;
  @Autowired
  private StoreProvider storeProvider;
  @Autowired
  private Fixers fixers;

  @RequestMapping("/ksMapLoad")
  public String ksMapLoad() throws Exception {
    ksMappingHolder.loadFromKS();
    return "Loaded!";
  }

  @RequestMapping("/withoutInfoBox")
  public String withoutInfoBox(@RequestParam int version,
                               @RequestParam(defaultValue = "none") StoreType type) throws Exception {
    wikiTripleImporter.writeEntitiesWithoutInfoBox(version, type);
    return "Imported!";
  }

  @RequestMapping("/withInfoBox")
  public String withInfoBox(@RequestParam int version,
                            @RequestParam(defaultValue = "none") StoreType type) throws Exception {
    wikiTripleImporter.writeEntitiesWithInfoBox(version, type);
    return "Imported!";
  }

  @RequestMapping("/abstracts")
  public String abstracts(@RequestParam int version,
                          @RequestParam(defaultValue = "none") StoreType type) throws Exception {
    wikiTripleImporter.writeAbstracts(version, type);
    return "Imported!";
  }

  @RequestMapping("/triples")
  public String triples(@RequestParam int version,
                        @RequestParam(defaultValue = "none") StoreType type) throws Exception {
    wikiTripleImporter.writeTriples(version, type, true);
    notMappedPropertyHandler.writeNotMappedProperties(Module.wiki.name(), version, true);
    return "Imported!";
  }

  @RequestMapping("/categoryTriples")
  public String categoryTriples(@RequestParam int version,
                                @RequestParam(defaultValue = "none") StoreType type) throws Exception {
    wikiTripleImporter.writeCategoryTriples(version, type, true, null);
    return "Imported!";
  }

  @RequestMapping("/tables")
  public String tables(@RequestParam(defaultValue = "none") StoreType type) throws Exception {
    tableTripleImporter.writeTriples(type);
    return "Imported!";
  }

  @RequestMapping("/redirects")
  public String redirects(@RequestParam int version) throws Exception {
    redirectLogic.write(version, StoreType.knowledgeStore);
    return "Imported!";
  }

  @RequestMapping("/ambiguities")
  public String ambiguities(@RequestParam int version) throws Exception {
    ambiguityLogic.write(version, StoreType.knowledgeStore);
    return "Imported!";
  }

  @RequestMapping("/predicates")
  @ResponseBody
  public Boolean predicates(@RequestParam(defaultValue = "true") boolean resolveAmbiguity) {
    predicateImporter.writePredicates(resolveAmbiguity);
    return true;
  }

  @RequestMapping("/dbpediaPredicates")
  @ResponseBody
  public Boolean dbpediaPredicates() {
    ontologyLogic.importFromDBpedia();
    return true;
  }

  @RequestMapping("/properties")
  @ResponseBody
  public Boolean properties(@RequestParam int version,
                            @RequestParam(defaultValue = "none") StoreType type,
                            @RequestParam(defaultValue = "true") boolean resolveAmbiguity) {
    wikiTripleImporter.writeTriples(version, type, false);
    notMappedPropertyHandler.writeNotMappedProperties(Module.wiki.name(), version, resolveAmbiguity);
    return true;
  }

  @RequestMapping("/completeDumpUpdate")
  public void completeDumpUpdate(@RequestParam(defaultValue = "none") StoreType type,
                                 @RequestParam(defaultValue = "false") boolean entitiesWithoutInfoBox)
      throws Exception {
    final FkgTripleDao store = storeProvider.getStore(type);
    int version = store.newVersion(Module.wiki.name());
    ProgressInformer informer = new ProgressInformer(9);
    if (entitiesWithoutInfoBox) wikiTripleImporter.writeEntitiesWithoutInfoBox(version, type);
    informer.stepDone(1);
    wikiTripleImporter.writeEntitiesWithInfoBox(version, type);
    informer.stepDone(2);
    // 2 percent for triple exporting because of its complication
    wikiTripleImporter.writeTriples(version, type, true);
    informer.stepDone(3);
    wikiTripleImporter.writeCategoryTriples(version, type, true, null);
    informer.stepDone(4);
    notMappedPropertyHandler.writeNotMappedProperties(Module.wiki.name(), version, true);
    informer.stepDone(5);
    wikiTripleImporter.writeAbstracts(version, type);
    informer.stepDone(6);
    redirectLogic.write(version, type);
    informer.stepDone(7);
    ambiguityLogic.write(version, type);
    informer.stepDone(8);
    predicateImporter.writePredicates(true);
    store.activateVersion(Module.wiki.name(), version);
    informer.done();
  }

  public boolean raw(@NotNull StoreType type, Boolean newSubject) {
    rawTripleImporter.writeTriples(type, (newSubject != null) && newSubject);
    notMappedPropertyHandler.writeNotMappedProperties("raw", 1, true);
    return true;
  }

  public void fix(String[] arguments) {
    assert arguments.length > 0;
    switch (arguments[0]) {
      case "ontologyLabel":
        fixers.findOntologyMoreThanOneLabels();
      case "wrongResources":
        fixers.findWrongResources();
      case "migrateUrls":
        fixers.migrateUrls(arguments[1], arguments[2]);
    }
  }

  public void createTestSet(@Nullable String filteredSubjectFile) throws IOException {
    assert filteredSubjectFile != null;
    final List<String> list = Files.readAllLines(Paths.get(filteredSubjectFile), Charset.forName("UTF-8"));
    wikiTripleImporter.createTestTriples(list.toArray(new String[list.size()]));
  }

  public void fileToStore(Integer overrideVersion, String overrideModule) {
    final FkgTripleDao knowledgeStore = storeProvider.getStore(StoreType.knowledgeStore);
    knowledgeStore.setValidate(false);
    final FkgTripleDao fileStore = storeProvider.getStore(StoreType.file);
    int version = (overrideVersion != null) ? overrideVersion : knowledgeStore.newVersion(Module.wiki.name());
    int pageSize = ConfigReader.INSTANCE.getInt("file.to.store.page.size", "10000");
    int page = 0;
    PagedData<FkgTriple> list = fileStore.list(pageSize, page);
    ProgressInformer informer = new ProgressInformer((int) list.getPageCount());
    String categoryResource = null;
    long numberOfTriples = 0;
    final String sameAs = URIs.INSTANCE.getSameAs();
    while (true) {
      for (FkgTriple triple : list.getData()) {
        if (triple.getSubject() == null || triple.getObjekt() == null || triple.getPredicate() == null) continue;
        if (triple.getPredicate().equals(sameAs)) {
          if (triple.getSubject().contains("الگو:") || triple.getSubject().contains("Template:")) continue;
          if (triple.getObjekt().toLowerCase().contains("category")) {
            categoryResource = triple.getSubject();
          }
          if (Objects.equals(triple.getSubject(), categoryResource)) {
            triple.setSubject(triple.getSubject().replace("/resource/", "/category/"));
            triple.setSource(triple.getSubject());
          }
        }
        triple.setVersion(version);
        if (overrideModule != null) triple.setModule(overrideModule);
        numberOfTriples++;
        knowledgeStore.save(triple);
        if (numberOfTriples % 10000 == 0)
          System.out.println(numberOfTriples + " triples has been written.");
      }
      knowledgeStore.flush();
      if (list.getData().isEmpty()) break;
      list = fileStore.list(pageSize, ++page);
      informer.stepDone(page);
    }
    System.out.println(numberOfTriples + " triples has been written.");
    if (overrideModule == null && overrideVersion == null)
      knowledgeStore.activateVersion(Module.wiki.name(), version);
    informer.done();
  }

  public void fastWikiUpdate() throws Exception {
    completeDumpUpdate(StoreType.file, true);
    fileToStore(null, null);
  }

  public void sameAs(int version, @NotNull StoreType storeType) {
    sameAsLogic.writeSameAs(version, storeType);
  }
}
