package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@RestController
@Api(value = "/psicquic", description = "PSICQUIC content")
@RequestMapping("/psicquic")
public class PsicquicInteractionsController {

    @RequestMapping(value = "/resources", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<String> resources()  {
        List<String> rtn = new ArrayList<>();
        rtn.add("{msg:resource...to be created}");
        return rtn;
    }

}
