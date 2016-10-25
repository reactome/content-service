package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@RestController
@Api(tags = "references", description = "Reactome xRefs: ReferenceEntity queries")
@RequestMapping("/references")
public class ReferenceEntityController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private ReferenceEntityService referenceEntityService;

    @ApiOperation(value = "All ReferenceEntities for a given identifier",
            notes = "Retrieves a list containing all the reference entities for a given identifier.",
            produces = "application/json")
    @RequestMapping(value = "/mapping/{identifier}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getReferenceEntitiesFor(@ApiParam(value = "Identifier for a given entity", defaultValue = "15377", required = true) @PathVariable String identifier) {
        Collection<ReferenceEntity> referenceEntities = referenceEntityService.getReferenceEntitiesFor(identifier);
        if (referenceEntities == null || referenceEntities.isEmpty())
            throw new NotFoundException("Identifier: " + identifier + " has not been found as a cross reference in any of the annotated entities");
        infoLogger.info("Request all ReferenceEntities for the identifier: {}", identifier);
        return referenceEntities;
    }

}
