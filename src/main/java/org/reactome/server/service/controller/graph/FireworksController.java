package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.FireworksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@RestController
@Api(tags = "data", description = "Reactome Data ")
@RequestMapping("/data")
public class FireworksController {

    @Autowired
    private FireworksService fireworksService;

    @ApiOperation(value = "Retrieves the list of the lower level pathways where the passed PhysicalEntity or Event are present", response = SimpleDatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/pathwaysForInstance/{stId}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysFor(@ApiParam(defaultValue = "R-HSA-199420") @PathVariable String stId,
                                                           @ApiParam(defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId){
        return fireworksService.getPathwaysFor(stId, speciesId);
    }

    @ApiOperation(value = "Retrieves the list of the lower level pathways where all the forms of the passed PhysicalEntity are present", response = SimpleDatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/pathwaysForAllFormsOf/{stId}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysForAllFormsOf(@ApiParam(defaultValue = "R-HSA-199420") @PathVariable String stId,
                                                                     @ApiParam(defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId){
        return fireworksService.getPathwaysForAllFormsOf(stId, speciesId);
    }
}
