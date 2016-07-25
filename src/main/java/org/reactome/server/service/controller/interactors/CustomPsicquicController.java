package org.reactome.server.service.controller.interactors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.interactors.model.CustomPsicquicResource;
import org.reactome.server.interactors.tuple.exception.ParserException;
import org.reactome.server.interactors.tuple.model.TupleResult;
import org.reactome.server.service.manager.CustomInteractorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */


@Api(tags = "interactors", description = "Molecule interactors")
@RequestMapping(value = "/interactors/upload/psicquic")
@RestController
public class CustomPsicquicController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    public CustomInteractorManager customInteractionManager;

    @ApiOperation(value = "Registry custom PSICQUIC resource", response = TupleResult.class, produces = "application/json")
    @RequestMapping(value = "/url", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public CustomPsicquicResource registryPsicquicURL(@ApiParam(name = "name", required = true, value = "Name which identifies the custom psicquic")
                                           @RequestParam String name,
                                                      @ApiParam(name = "url", required = true, value = "A URL pointing to the Custom PSICQUIC Resource")
                                           @RequestBody String url) throws ParserException {
        infoLogger.info("Custom Psicquic resource {} has been submitted", name);
        return customInteractionManager.registryCustomPsicquic(name, url);

    }
}
