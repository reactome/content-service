package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.TopLevelPathway;
import org.reactome.server.graph.service.TopLevelPathwayService;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@RestController
@Api(tags = "topLevelPathways", description = "Reactome Data: TopLevelPathway queries" )
@RequestMapping("/data")
public class TopLevelPathwayController {

    @Autowired
    private TopLevelPathwayService topLevelPathwayService;

    @ApiOperation(value = "Retrieves all Reactome top level pathways",
            notes = "If species is specified result will be filtered.",
            produces = "application/json")
    @RequestMapping(value = "/topLevelPathway", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getTopLevelPathways(@ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606") @RequestParam(required = false) String species)  {
        if (species == null) {
            return topLevelPathwayService.getTopLevelPathways();
        } else {
            Collection<TopLevelPathway> topLevelPathways = topLevelPathwayService.getTopLevelPathways(species);
            if (topLevelPathways == null || topLevelPathways.isEmpty()) throw new NotFoundException("No TopLevelPathways were found for species: " + species);
            return topLevelPathways;
        }
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways",
            notes = "Retrieves a single property from the list of DatabaseObjects. If species is specified result will be filtered.",
            produces = "application/json")
    @RequestMapping(value = "/topLevelPathway/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Object> getTopLevelPathways(@ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606") @RequestParam(required = false) String species,
                                                  @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {

        Collection<TopLevelPathway> topLevelPathways;
        if (species == null) {
            topLevelPathways = topLevelPathwayService.getTopLevelPathways();
        } else {
            topLevelPathways = topLevelPathwayService.getTopLevelPathways(species);
            if (topLevelPathways == null || topLevelPathways.isEmpty()) throw new NotFoundException("No TopLevelPathways were found for species: " + species);
        }
        return ControllerUtils.getProperties(topLevelPathways, attributeName);
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways",
            notes = "If species is specified result will be filtered.",
            produces = "application/json")
    @RequestMapping(value = "/curatedTopLevelPathway", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getCuratedTopLevelPathways(@ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606") @RequestParam(required = false) String species)  {
        if (species == null) {
            return topLevelPathwayService.getCuratedTopLevelPathways();
        } else {
            Collection<TopLevelPathway> topLevelPathways = topLevelPathwayService.getCuratedTopLevelPathways(species);
            if (topLevelPathways == null || topLevelPathways.isEmpty()) throw new NotFoundException("No TopLevelPathways were found for species: " + species);
            return topLevelPathways;
        }
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways",
            notes = "Retrieves a single property from the list of DatabaseObjects. If species is specified result will be filtered.",
            produces = "application/json")
    @RequestMapping(value = "/curatedTopLevelPathway/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Object> getCuratedTopLevelPathways(@ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606") @RequestParam(required = false) String species,
                                                         @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<TopLevelPathway> topLevelPathways;
        if (species == null) {
            topLevelPathways = topLevelPathwayService.getCuratedTopLevelPathways();
        } else {
            topLevelPathways = topLevelPathwayService.getCuratedTopLevelPathways(species);
            if (topLevelPathways == null || topLevelPathways.isEmpty()) throw new NotFoundException("No TopLevelPathways were found for species: " + species);
        }
        return ControllerUtils.getProperties(topLevelPathways, attributeName);
    }
}
