package ir.ac.iust.dml.kg.mapper.runner.web.rest;

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao;
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
import ir.ac.iust.dml.kg.mapper.logic.wiki.WikiTripleImporter;
import ir.ac.iust.dml.kg.raw.utils.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
    wikiTripleImporter.writeTriples(version, type, true, null);
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
    wikiTripleImporter.writeTriples(version, type, false, null);
    notMappedPropertyHandler.writeNotMappedProperties(Module.wiki.name(), version, resolveAmbiguity);
    return true;
  }

  @RequestMapping("/completeDumpUpdate")
  public void completeDumpUpdate(@RequestParam(defaultValue = "none") StoreType type,
                                 @RequestParam(defaultValue = "false") boolean entitiesWithoutInfoBox)
      throws Exception {
    final FkgTripleDao store = storeProvider.getStore(type, null);
    int version = store.newVersion(Module.wiki.name());
    ProgressInformer informer = new ProgressInformer(9);
    if (entitiesWithoutInfoBox) wikiTripleImporter.writeEntitiesWithoutInfoBox(version, type);
    informer.stepDone(1);
    wikiTripleImporter.writeEntitiesWithInfoBox(version, type);
    informer.stepDone(2);
    // 2 percent for triple exporting because of its complication
    wikiTripleImporter.writeTriples(version, type, true, null);
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

  public boolean raw(@NotNull StoreType type) {
    rawTripleImporter.writeTriples(type);
    notMappedPropertyHandler.writeNotMappedProperties("raw", 1, true);
    return true;
  }

  public void fix(@Nullable String type) {
    assert type != null;
    switch (type) {
      case "ontologyLabel":
        fixers.findOntologyMoreThanOneLables();
    }
  }
}
