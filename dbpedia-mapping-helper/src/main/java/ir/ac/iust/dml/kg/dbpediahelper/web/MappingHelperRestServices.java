package ir.ac.iust.dml.kg.dbpediahelper.web;

import ir.ac.iust.dml.kg.dbpediahelper.logic.DbpediaHelperLoader;
import ir.ac.iust.dml.kg.dbpediahelper.logic.PrefixService;
import ir.ac.iust.dml.kg.dbpediahelper.logic.export.DbpediaHelperExporter;
import ir.ac.iust.dml.kg.dbpediahelper.logic.export.ExportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/helper")
public class MappingHelperRestServices {
    @Autowired
    private PrefixService prefixService;
    @Autowired
    private DbpediaHelperLoader helperLoader;
    @Autowired
    private DbpediaHelperExporter helperExporter;

    @RequestMapping("/prefixes")
    public String prefixes() throws Exception {
        prefixService.reload();
        return "Reloaded!";
    }

    @RequestMapping("/load")
    public String load() throws Exception {
        helperLoader.load();
        return "Loaded!";
    }

    @RequestMapping("/generate")
    public String generate() throws Exception {
        helperLoader.generatePersian();
        return "Generated!";
    }

    @RequestMapping("/export/xml")
    public void exportXml(@RequestParam(required = false) String language,
                          HttpServletResponse response) throws Exception {
        helperExporter.exportXml(language, response);
    }

    @RequestMapping(value = "/export/json", produces = "application/json")
    @ResponseBody
    public ExportData exportJson(@RequestParam(required = false) String language,
                                 HttpServletResponse response) throws Exception {
        return helperExporter.exportJson(language, response);
    }
}
