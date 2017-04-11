package ir.ac.iust.dml.kg.web.rest;

import io.swagger.annotations.Api;
import ir.ac.iust.dml.kg.access.dao.WikipediaPropertyTranslationDao;
import ir.ac.iust.dml.kg.access.entities.WikipediaPropertyTranslation;
import ir.ac.iust.dml.kg.templateequalities.logic.Loader;
import ir.ac.iust.dml.kg.templateequalities.logic.export.ExportData;
import ir.ac.iust.dml.kg.templateequalities.logic.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/template")
@Api(tags = "template", description = "سرویس‌های خصیصه‌های معادل انگلیسی و فارسی")
public class TemplateRestServices {
  @Autowired
  private Loader loader;
  @Autowired
  private Exporter exporter;
  @Autowired
  private WikipediaPropertyTranslationDao dao;

  @RequestMapping("/load")
  public String load() throws Exception {
    loader.load();
    return "Loaded!";
  }

  @RequestMapping("/export")
  @ResponseBody
  public ExportData exportXml() throws Exception {
    return exporter.export();
  }

  @RequestMapping(value = "/rest/v1/mapping/{lang}/{title}", method = RequestMethod.GET)
  @ResponseBody
  public List<WikipediaPropertyTranslation> readByLang(@PathVariable String lang,
                                                       @PathVariable String title) {
    if (lang.equals("fa")) return dao.readByFaTitle(null, title);
    return dao.readByEnTitle(null, title, true);
  }

  @RequestMapping(value = "/rest/v1/mapping/{lang}/{type}/{title}", method = RequestMethod.GET)
  @ResponseBody
  public List<WikipediaPropertyTranslation> readByLangAndType(@PathVariable String lang,
                                                              @PathVariable String type,
                                                              @PathVariable String title) {
    if (lang.equals("fa")) return dao.readByFaTitle(type, title);
    return dao.readByEnTitle(type, title, true);
  }

  @RequestMapping(value = "/rest/v1/mapping", method = RequestMethod.GET)
  @ResponseBody
  public List<WikipediaPropertyTranslation> read(@RequestParam String lang,
                                                 @RequestParam(required = false) String type,
                                                 @RequestParam String title) {
    if (lang.equals("fa")) return dao.readByFaTitle(type, title);
    return dao.readByEnTitle(type, title, true);
  }

}
