package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.SpeciesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@RestController
@Api(tags = "species", description = "Reactome Data: Species related queries")
@RequestMapping("/data")
public class SpeciesController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    SpeciesService speciesService;

    @ApiOperation(value = "The list of main species sorted by name (but having 'Homo sapiens' the first one)", response = SimpleDatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/species/main", method = RequestMethod.GET)
    @ResponseBody
    public List<Species> getSpecies() {
        infoLogger.info("Request for all main species performed");
        return speciesService.getSpecies();
    }

    @ApiOperation(value = "The list of all species sorted by name", response = SimpleDatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/species/all", method = RequestMethod.GET)
    @ResponseBody
    public List<Species> getAllSpecies() {
        infoLogger.info("Request for all species performed");
        return speciesService.getAllSpecies();
    }

    @ApiIgnore
    @RequestMapping(value = "/error", method = RequestMethod.GET)
    @ResponseBody
    public void error() throws Exception {
        throw new Exception("bla");
    }

}
