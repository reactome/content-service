package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.SpeciesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@RestController
@Api(tags = "species", description = "Species related queries.")
@RequestMapping("/data")
public class SpeciesController {

    @Autowired
    SpeciesService speciesService;

    @ApiOperation(value = "Retrieves the list of main species sorted by name (but having 'Homo sapiens' the first one)", response = SimpleDatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/species", method = RequestMethod.GET)
    @ResponseBody
    public List<Species> getSpecies() {
        return speciesService.getSpecies();
    }

    @ApiOperation(value = "Retrieves the list of all species sorted by name", response = SimpleDatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/species/all", method = RequestMethod.GET)
    @ResponseBody
    public List<Species> getAllSpecies() {
        return speciesService.getAllSpecies();
    }
}
