/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.runner.web.rest;

import io.swagger.annotations.Api;
import ir.ac.iust.dml.kg.mapper.logic.data.FkgClassData;
import ir.ac.iust.dml.kg.mapper.logic.ontology.OntologyLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/translator")
@Api(tags = "translator", description = "سرویس‌های ترجمه")
public class OntologyTranslatorRestServices {

  @Autowired
  OntologyLogic ontologyLogic;

  @RequestMapping(value = "/rest/v1/node/{name}", method = RequestMethod.GET)
  @ResponseBody
  public FkgClassData getNode(@PathVariable String name) throws Exception {
    return ontologyLogic.getNode(name);
  }
}
