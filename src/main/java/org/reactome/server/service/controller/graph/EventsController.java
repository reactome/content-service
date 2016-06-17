package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.service.EventsService;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@RestController
@Api(tags = "events", description = "Reactome Data: Queries related to events")
@RequestMapping("/data")
public class EventsController {

    @Autowired
    private EventsService eventsService;

    @ApiOperation(value = "The ancestors for a given event", notes = "An event (pathway or reaction) can be in more than one location. This method retrieves a list of possible paths from the requested event to the top level pathway(s)")
    @RequestMapping(value = "/event/{id}/ancestors", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Collection<Pathway>> getEventHierarchy(@ApiParam(value = "The event for which the ancestors are requested", defaultValue = "R-HSA-5673001", required = true)
                                                             @PathVariable String id) {
        Collection<Collection<Pathway>> ancestors = eventsService.getEventAncestors(id);
        if (ancestors == null || ancestors.isEmpty())
            throw new NotFoundException("No ancestors found for given event: " + id);
        return ancestors;
    }

}
