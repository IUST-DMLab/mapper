package ir.ac.iust.dml.kg.ontologytranslator.web;

import io.swagger.annotations.Api;
import ir.ac.iust.dml.kg.ontologytranslator.logic.Importer;
import ir.ac.iust.dml.kg.ontologytranslator.logic.Translator;
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.ExportData;
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.FkgClassData;
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.FkgClassExporter;
import ir.ac.iust.dml.kg.raw.utils.PagedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/translator")
@Api(tags = "translator", description = "سرویس‌های ترجمه")
public class OntologyTranslatorRestServices {
  @Autowired
  private Importer importer;
  @Autowired
  private Translator translator;
  @Autowired
  private FkgClassExporter translatorExporter;

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

  @RequestMapping(value = "/rest/v1/root", method = RequestMethod.GET)
  @ResponseBody
  public FkgClassData root() throws Exception {
    return translator.getRoot();
  }

  @RequestMapping(value = "/rest/v1/node/{name}", method = RequestMethod.GET)
  @ResponseBody
  public FkgClassData getNode(@PathVariable String name) throws Exception {
    return translator.getNode(name);
  }

  @RequestMapping(value = "/rest/v1/parent/{name}", method = RequestMethod.GET)
  @ResponseBody
  public FkgClassData getParent(@PathVariable String name) throws Exception {
    return translator.getParent(name);
  }

  @RequestMapping(value = "/rest/v1/children/{name}", method = RequestMethod.GET)
  @ResponseBody
  public List<FkgClassData> getChildren(@PathVariable String name) throws Exception {
    return translator.getChildren(name);
  }

  @RequestMapping(value = "/rest/v1/search", method = RequestMethod.GET)
  @ResponseBody
  public PagedData<FkgClassData> search(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String parent,
                                        @RequestParam(required = false, defaultValue = "false") Boolean like,
                                        @RequestParam(required = false) Boolean approved,
                                        @RequestParam(required = false) Boolean hasFarsi,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int pageSize)
          throws Exception {
    return translator.search(name, parent, like, approved, hasFarsi, pageSize, page);
  }

  @RequestMapping(value = "/rest/v1/translate", method = RequestMethod.GET)
  @ResponseBody
  public Boolean translateGet(@RequestParam String name,
                              @RequestParam String faLabel,
                              @RequestParam String faOtherLabels,
                              @RequestParam String note,
                              @RequestParam boolean approved) throws Exception {
    return translator.translate(new FkgClassData(name, null, null, null,
            faLabel, faOtherLabels, note, approved));
  }

  @RequestMapping(value = "/rest/v1/translate", method = RequestMethod.POST)
  @ResponseBody
  public Boolean translatePost(@RequestBody FkgClassData data) throws Exception {
    return translator.translate(data);
  }
}
