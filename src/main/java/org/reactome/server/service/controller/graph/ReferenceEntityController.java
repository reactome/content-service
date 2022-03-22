package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.service.ReferenceEntityService;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "references", description = "Reactome xRefs: ReferenceEntity queries")
@RequestMapping("/references")
public class ReferenceEntityController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private ReferenceEntityService referenceEntityService;

    @Operation(
            summary = "All ReferenceEntities for a given identifier",
            description = "Retrieves a list containing all the reference entities for a given identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any reference entity"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/mapping/{identifier}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getReferenceEntitiesFor(@Parameter(description = "Identifier for a given entity", example = "15377", required = true) @PathVariable String identifier) {
        Collection<ReferenceEntity> referenceEntities = referenceEntityService.getReferenceEntitiesFor(identifier);
        if (referenceEntities == null || referenceEntities.isEmpty())
            throw new NotFoundException("Identifier: " + identifier + " has not been found as a cross reference in any of the annotated entities");
        infoLogger.info("Request all ReferenceEntities for the identifier: {}", identifier);
        return referenceEntities;
    }

}
