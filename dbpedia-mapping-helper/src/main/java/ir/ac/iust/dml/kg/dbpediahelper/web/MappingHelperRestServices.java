package ir.ac.iust.dml.kg.dbpediahelper.web;

import ir.ac.iust.dml.kg.dbpediahelper.logic.DbpediaHelperLoader;
import ir.ac.iust.dml.kg.dbpediahelper.logic.PrefixService;
import ir.ac.iust.dml.kg.dbpediahelper.logic.export.DbpediaHelperExporter;
import ir.ac.iust.dml.kg.dbpediahelper.logic.export.ExportData;
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
    private DbpediaHelperLoader helperLoader;
    @Autowired
    private TripleImporter tripleImporter;
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

    @RequestMapping("/triples")
    public String triples() throws Exception {
        tripleImporter.traverse();
        return "Imported!";
    }

    @RequestMapping("/generate")
    public String generate() throws Exception {
        helperLoader.generatePersian();
        return "Generated!";
    }

    @RequestMapping("/export")
    @ResponseBody
    public ExportData exportXml(@RequestParam(required = false) String language) throws Exception {
        return helperExporter.export(language);
    }
}
