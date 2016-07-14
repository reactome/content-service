package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.domain.result.Participant;
import org.reactome.server.graph.service.EventsService;
import org.reactome.server.graph.service.HierarchyService;
import org.reactome.server.graph.service.ParticipantService;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private HierarchyService eventHierarchyService;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private EventsService eventsService;

    @ApiOperation(value = "The ancestors of a given event", notes = "The Reactome definition of events includes pathways and reactions. Although events are organised in a hierarchical structure, a single event can be in more than one location, i.e. a reaction can take part in different pathways while, in the same way, a sub-pathway can take part in many pathways. Therefore, this method retrieves a list of all possible paths from the requested event to the top level pathway(s).")
    @RequestMapping(value = "/event/{id}/ancestors", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Collection<Pathway>> getEventAncestors(@ApiParam(value = "The event for which the ancestors are requested", defaultValue = "R-HSA-5673001", required = true)
                                                             @PathVariable String id) {
        Collection<Collection<Pathway>> ancestors = eventsService.getEventAncestors(id);
        if (ancestors == null || ancestors.isEmpty()) throw new NotFoundException("No ancestors found for given event: " + id);
        infoLogger.info("Request for all Ancestors of Event with id: {}", id);
        return ancestors;
    }

    @ApiOperation(value = "The full event hierarchy for a given species", notes = "Events (pathways and reactions) in Reactome are organised in a hierarchical structure for every species. By following all 'hasEvent' relationships, this method retrieves the full event hierarchy for any given species. The result is a list of tree structures, one for each TopLevelPathway. Every event in these trees is represented by a PathwayBrowserNode. The latter contains the stable identifier, the name, the species, the url, the type, and the diagram of the particular event.")
    @RequestMapping(value = "/eventsHierarchy/{species}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PathwayBrowserNode> getEventHierarchy(@ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606",required = true) @PathVariable String species)  {
        Collection<PathwayBrowserNode> pathwayBrowserNodes = eventHierarchyService.getEventHierarchy(species);
        if (pathwayBrowserNodes == null || pathwayBrowserNodes.isEmpty()) throw new NotFoundException("No event hierarchy found for given species: " + species);
        infoLogger.info("Request for full event hierarchy");
        return pathwayBrowserNodes;
    }

    @ApiOperation(value = "A list of participants for a given event",
            notes = "Participants contains a PhysicalEntity (dbId, displayName) and a collection of ReferenceEntities (dbId, name, identifier, url)",
            response = Participant.class, responseContainer = "List")
    @RequestMapping(value = "/event/{id}/participants", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Participant> getParticipants(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "5205685",required = true) @PathVariable String id)  {
        Collection<Participant> participants = participantService.getParticipants(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

    @ApiOperation(value = "A list of participating PhysicalEntities for a given event", notes = "This method retrieves all the PhysicalEntities that take part in a given event. It is worth mentioning that because a pathway can contain smaller pathways (subpathways), the method also recursively retrieves the PhysicalEntities from every constituent pathway.")
    @RequestMapping(value = "/event/{id}/participatingPhysicalEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getParticipatingPhysicalEntities(@ApiParam(value = "The event for which the participating PhysicalEntities are requested", defaultValue = "R-HSA-5205685",required = true) @PathVariable String id)  {
        Collection<PhysicalEntity> participants = participantService.getParticipatingPhysicalEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

    @ApiOperation(value = "A list of participating ReferenceEntities for a given event", notes = "PhysicalEntity instances that represent, e.g., the same chemical in different compartments, or different post-translationally modified forms of a single protein, share numerous invariant features such as names, molecular structure and links to external databases like UniProt or ChEBI.<br>To enable storage of this shared information in a single place, and to create an explicit link among all the variant forms of what can also be seen as a single chemical entity, Reactome creates instances of the separate ReferenceEntity class. A ReferenceEntity instance captures the invariant features of a molecule.<br>This method retrieves the ReferenceEntities of all PhysicalEntities that take part in a given event. It is worth mentioning that because a pathway can contain smaller pathways (subpathways), this method also recursively retrieves the ReferenceEntities for all PhysicalEntities in every constituent pathway.")
    @RequestMapping(value = "/event/{id}/participatingReferenceEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getParticipatingReferenceEntities(@ApiParam(value = "The event for which the participating ReferenceEntities are requested", defaultValue = "5205685",required = true) @PathVariable String id)  {
        Collection<ReferenceEntity> participants = participantService.getParticipatingReferenceEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

}
