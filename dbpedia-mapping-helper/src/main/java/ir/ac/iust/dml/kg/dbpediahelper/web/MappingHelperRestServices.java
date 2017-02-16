package ir.ac.iust.dml.kg.dbpediahelper.web;

import ir.ac.iust.dml.kg.dbpediahelper.logic.PrefixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/template")
public class MappingHelperRestServices {
    @Autowired
    private PrefixService prefixService;

    @RequestMapping("/prefixes")
    public String prefixes() throws Exception {
        prefixService.reload();
        return "Reloaded!";
    }
}
