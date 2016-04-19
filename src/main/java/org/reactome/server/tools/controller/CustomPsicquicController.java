package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.tools.interactors.model.CustomPsicquicResource;
import org.reactome.server.tools.interactors.tuple.model.TupleResult;
import org.reactome.server.tools.manager.CustomInteractorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */


@Api(tags = "interactors", description = "Molecule interactors")
@RequestMapping(value = "/interactors/upload/psicquic")
@RestController
public class CustomPsicquicController {

    @Autowired
    public CustomInteractorManager customInteractionManager;

    @ApiOperation(value = "Registry custom PSICQUIC resource", response = TupleResult.class, produces = "application/json")
    @RequestMapping(value = "/url", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public CustomPsicquicResource registryPsicquicURL(@ApiParam(name = "name", required = true, value = "Name which identifies the custom psicquic")
                                           @RequestParam(required = true) String name,
                                                      @ApiParam(name = "url", required = true, value = "A URL pointing to the Custom PSICQUIC Resource")
                                           @RequestBody String url) {

        return customInteractionManager.registryCustomPsicquic(name, url);

    }
}
