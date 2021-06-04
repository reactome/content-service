package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.SpeciesService;
import org.reactome.server.service.exception.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@RestController
@Api(tags = {"species"})
@RequestMapping("/data")
public class SpeciesController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    SpeciesService speciesService;

    @ApiOperation(value = "The list of main species in Reactome", notes = "This method retrieves the list of main species in Reactome knowledgebase, sorted by name, but having 'Homo sapiens' as the first one. It should be mentioned that for Reactome, main species are considered those have either manually curated or computationally inferred pathways.", response = SimpleDatabaseObject.class, responseContainer = "List", produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/species/main", method = RequestMethod.GET)
    @ResponseBody
    public List<Species> getSpecies() {
        infoLogger.info("Request for all main species performed");
        return speciesService.getSpecies();
    }

    @ApiOperation(value = "The list of all species in Reactome", notes = "This method retrieves the list of all species in Reactome knowledgebase, sorted by name.", response = SimpleDatabaseObject.class, responseContainer = "List", produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/species/all", method = RequestMethod.GET)
    @ResponseBody
    public List<Species> getAllSpecies() {
        infoLogger.info("Request for all species performed");
        return speciesService.getAllSpecies();
    }
}
