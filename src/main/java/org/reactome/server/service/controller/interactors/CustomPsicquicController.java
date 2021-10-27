package org.reactome.server.service.controller.interactors;

import io.swagger.annotations.*;
import org.reactome.server.interactors.model.CustomPsicquicResource;
import org.reactome.server.interactors.tuple.exception.ParserException;
import org.reactome.server.interactors.tuple.model.TupleResult;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.manager.CustomInteractorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("unused")
@Api(tags = {"interactors"})
@RequestMapping(value = "/interactors/upload/psicquic")
@RestController
public class CustomPsicquicController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    public CustomInteractorManager customInteractionManager;

    @ApiOperation(value = "Registry custom PSICQUIC resource", response = TupleResult.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Bad request", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
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
