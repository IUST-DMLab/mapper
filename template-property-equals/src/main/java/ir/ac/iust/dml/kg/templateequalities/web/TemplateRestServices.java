package ir.ac.iust.dml.kg.templateequalities.web;

import ir.ac.iust.dml.kg.templateequalities.access.dao.TemplatePropertyMappingDao;
import ir.ac.iust.dml.kg.templateequalities.access.entities.TemplatePropertyMapping;
import ir.ac.iust.dml.kg.templateequalities.logic.Loader;
import ir.ac.iust.dml.kg.templateequalities.logic.export.ExportData;
import ir.ac.iust.dml.kg.templateequalities.logic.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/template")
public class TemplateRestServices {
    @Autowired
    private Loader loader;
    @Autowired
    private Exporter exporter;
    @Autowired
    private TemplatePropertyMappingDao dao;

    @RequestMapping("/load")
    public String load() throws Exception {
        loader.load();
        return "Loaded!";
    }

    @RequestMapping("/export/xml")
    public void exportXml(HttpServletResponse response) throws Exception {
        exporter.exportXml(response);
    }

    @RequestMapping(value = "/export/json", produces = "application/json")
    @ResponseBody
    public ExportData exportJson(HttpServletResponse response) throws Exception {
        return exporter.exportJson(response);
    }

    @RequestMapping("/rest/v1/mapping/{lang}/{title}")
    @ResponseBody
    public List<TemplatePropertyMapping> readByLang(@PathVariable String lang,
                                                    @PathVariable String title) {
        if (lang.equals("fa")) return dao.readByFaTitle(null, title);
        return dao.readByEnTitle(null, title);
    }

    @RequestMapping("/rest/v1/mapping/{lang}/{type}/{title}")
    @ResponseBody
    public List<TemplatePropertyMapping> readByLangAndType(@PathVariable String lang,
                                                           @PathVariable String type,
                                                           @PathVariable String title) {
        if (lang.equals("fa")) return dao.readByFaTitle(type, title);
        return dao.readByEnTitle(type, title);
    }

    @RequestMapping("/rest/v1/mapping")
    @ResponseBody
    public List<TemplatePropertyMapping> read(@RequestParam String lang,
                                              @RequestParam(required = false) String type,
                                              @RequestParam String title) {
        if (lang.equals("fa")) return dao.readByFaTitle(type, title);
        return dao.readByEnTitle(type, title);
    }

}
