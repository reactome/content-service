package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.service.EventsService;
import org.reactome.server.graph.service.HierarchyService;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 */
@RestController
@Api(tags = "events", description = "Reactome Data: Queries related to events")
@RequestMapping("/data")
public class EventsController {

    @Autowired
    private HierarchyService eventHierarchyService;

    @Autowired
    private EventsService eventsService;

    @ApiOperation(value = "The ancestors for a given event", notes = "An event (pathway or reaction) can be in more than one location. This method retrieves a list of possible paths from the requested event to the top level pathway(s)")
    @RequestMapping(value = "/event/{id}/ancestors", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Collection<Pathway>> getEventAncestors(@ApiParam(value = "The event for which the ancestors are requested", defaultValue = "R-HSA-5673001", required = true)
                                                             @PathVariable String id) {
        Collection<Collection<Pathway>> ancestors = eventsService.getEventAncestors(id);
        if (ancestors == null || ancestors.isEmpty())
            throw new NotFoundException("No ancestors found for given event: " + id);
        return ancestors;
    }

    @ApiOperation(value = "A full event hierarchy for a given species", notes = "Event hierarchy will be created following hasEvent relationships. Each TopLevelPathway will form a tree. PathwayBrowserNode contains: stId, name, species, url, type, diagram")
    @RequestMapping(value = "/eventsHierarchy/{species}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PathwayBrowserNode> getEventHierarchy(@ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606",required = true) @PathVariable String species)  {
        Collection<PathwayBrowserNode> pathwayBrowserNodes = eventHierarchyService.getEventHierarchy(species);
        if (pathwayBrowserNodes == null || pathwayBrowserNodes.isEmpty()) throw new NotFoundException("No event hierarchy found for given species: " + species);
        return pathwayBrowserNodes;
    }
}
