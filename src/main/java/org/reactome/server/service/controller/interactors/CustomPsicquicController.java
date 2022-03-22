package org.reactome.server.service.controller.interactors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.interactors.model.CustomPsicquicResource;
import org.reactome.server.interactors.tuple.exception.ParserException;
import org.reactome.server.service.manager.CustomInteractorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("unused")
@Tag(name = "interactors")
@RequestMapping(value = "/interactors/upload/psicquic")
@RestController
public class CustomPsicquicController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    public CustomInteractorManager customInteractionManager;

    @Operation(summary = "Registry custom PSICQUIC resource")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/url", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public CustomPsicquicResource registryPsicquicURL(@Parameter(name = "name", required = true, description = "Name which identifies the custom psicquic")
                                                      @RequestParam String name,
                                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                              required = true,
                                                              description = "<b>url</b> A URL pointing to the Custom PSICQUIC Resource",
                                                              content = @Content(examples = {@ExampleObject("https://reactome.org/download/current/interactors/reactome.homo_sapiens.interactions.psi-mitab.txt")})
                                                      )
                                                      @RequestBody String url) throws ParserException {
        infoLogger.info("Custom Psicquic resource {} has been submitted", name);
        return customInteractionManager.registryCustomPsicquic(name, url);

    }
}
