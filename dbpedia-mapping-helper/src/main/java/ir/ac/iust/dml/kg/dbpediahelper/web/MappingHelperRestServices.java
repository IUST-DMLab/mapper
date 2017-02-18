package ir.ac.iust.dml.kg.dbpediahelper.web;

import ir.ac.iust.dml.kg.dbpediahelper.logic.DbpediaHelperLoader;
import ir.ac.iust.dml.kg.dbpediahelper.logic.PrefixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/helper")
public class MappingHelperRestServices {
    @Autowired
    private PrefixService prefixService;
    @Autowired
    private DbpediaHelperLoader helperLoader;

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
}
