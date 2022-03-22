package org.reactome.server.service.controller.interactors;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.interactors.exception.CustomPsicquicInteractionClusterException;
import org.reactome.server.interactors.model.Interaction;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.manager.CustomInteractorManager;
import org.reactome.server.service.manager.InteractionManager;
import org.reactome.server.service.model.interactors.Interactors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */

@SuppressWarnings("unused")
@Tag(name = "interactors", description = "Molecule interactors")
@RequestMapping(value = "/interactors/token")
@RestController
public class TokenController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private static final String CUSTOM_RESOURCE_NAME = "custom";

    @Autowired
    public CustomInteractorManager customInteractionManager;

    @Autowired
    public InteractionManager interactionManager;

    @Operation(summary = "Retrieve custom interactions associated with a token")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Could not find the given token"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/{token}", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public Interactors getInteractors(@Parameter(description = "A token associated with a data submission", required = true)
                                      @PathVariable String token,
                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "<b>Interactors</b> Interactors accessions", required = true)
                                      @RequestBody String proteins) throws CustomPsicquicInteractionClusterException {
        infoLogger.info("Token {} query has been submitted", token);
        // Split param and put into a Set to avoid duplicates
        Set<String> accs = new HashSet<>();
        for (String id : proteins.split(",|;|\\n|\\t")) {
            accs.add(id.trim());
        }
        Map<String, List<Interaction>> interactionMap = customInteractionManager.getInteractionsByTokenAndProteins(token, accs);
        return interactionManager.getCustomInteractionResult(interactionMap, CUSTOM_RESOURCE_NAME, token);
    }
}
