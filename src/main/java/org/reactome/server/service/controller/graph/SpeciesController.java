package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.service.SpeciesService;
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
@Tag(name = "species", description = "Reactome Data: Species related queries")
@RequestMapping("/data")
public class SpeciesController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    SpeciesService speciesService;

    @Operation(summary = "The list of main species in Reactome", description = "This method retrieves the list of main species in Reactome knowledgebase, sorted by name, but having 'Homo sapiens' as the first one. It should be mentioned that for Reactome, main species are considered those have either manually curated or computationally inferred pathways.")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/species/main", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Species> getSpecies() {
        infoLogger.info("Request for all main species performed");
        return speciesService.getSpecies();
    }

    @Operation(summary = "The list of all species in Reactome", description = "This method retrieves the list of all species in Reactome knowledgebase, sorted by name.")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/species/all", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Species> getAllSpecies() {
        infoLogger.info("Request for all species performed");
        return speciesService.getAllSpecies();
    }
}
