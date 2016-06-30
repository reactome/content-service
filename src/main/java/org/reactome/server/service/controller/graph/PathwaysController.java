package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.result.Participant;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.ParticipantService;
import org.reactome.server.graph.service.PathwaysService;
import org.reactome.server.graph.service.TopLevelPathwayService;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RestController
@Api(tags = "pathways", description = "Reactome Data: Pathway related queries")
@RequestMapping("/data")
public class PathwaysController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private PathwaysService pathwaysService;

    @Autowired
    private TopLevelPathwayService topLevelPathwayService;

    @ApiOperation(value = "All the contained events for a given event", notes = "An event (pathway) can contain other pathways. This method recursively retrieves all the events contained in the requested event")
    @RequestMapping(value = "/pathway/{id}/containedEvents", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Event> getContainedEvents(@ApiParam(value = "The event for which the ancestors are requested", defaultValue = "R-HSA-5673001", required = true)
                                                @PathVariable String id) {
        Collection<Event> containedEvents = pathwaysService.getContainedEvents(id);
        if (containedEvents == null || containedEvents.isEmpty()) throw new NotFoundException("No ancestors found for given event: " + id);
        infoLogger.info("Request for contained events of event with id: {}", id);
        return containedEvents;
    }


    @ApiOperation(value = "A single property for each event contained in the given event", notes = "An event (pathway) can contain other pathways. This method recursively retrieves all the events contained in the requested event")
    @RequestMapping(value = "/pathway/{id}/containedEvents/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<String> getContainedEvents(@ApiParam(value = "The event for which the ancestors are requested", defaultValue = "R-HSA-5673001", required = true)
                                                 @PathVariable String id,
                                                 @ApiParam(value = "Attribute to be filtered", defaultValue = "stId", required = true)
                                                 @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<Event> containedEvents = pathwaysService.getContainedEvents(id);
        if (containedEvents == null || containedEvents.isEmpty()) throw new NotFoundException("No ancestors found for given event: " + id);
        infoLogger.info("Request for contained events of event with id: {}", id);
        return ControllerUtils.getProperties(containedEvents, attributeName);
    }

    @ApiOperation(value = "A list of participants for a given pathway",
            notes = "Participants contains a PhysicalEntity (dbId, displayName) and a collection of ReferenceEntities (dbId, name, identifier, url)",
            response = Participant.class, responseContainer = "List")
    @RequestMapping(value = "/pathway/{id}/participants", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Participant> getParticipants(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "5205685",required = true) @PathVariable String id)  {
        Collection<Participant> participants = participantService.getParticipants(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

    @ApiOperation(value = "A list of participants for a given pathway", notes = "Retrieves a collection of PhysicalEntities")
    @RequestMapping(value = "/pathway/{id}/participatingPhysicalEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getParticipatingPhysicalEntities(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-5205685",required = true) @PathVariable String id)  {
        Collection<PhysicalEntity> participants = participantService.getParticipatingPhysicalEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

    @ApiOperation(value = "A list of participants for a given pathway", notes = "Retrieves a collection of ReferenceEntitites")
    @RequestMapping(value = "/pathway/{id}/participatingReferenceEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getParticipationgReferenceEntities(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "5205685",required = true) @PathVariable String id)  {
        Collection<ReferenceEntity> participants = participantService.getParticipatingReferenceEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

    @ApiOperation(value = "All Reactome top level pathways", notes = "This method retrieves the list of top level pathways for the given species") //, response = Pathway.class, responseContainer = "List")
    @RequestMapping(value = "/pathways/top/{species}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<? extends Pathway> getTopLevelPathways(@ApiParam(value = "Specifies the species by SpeciesName (eg: Homo sapiens) or SpeciesTaxId (eg: 9606)", defaultValue = "9606")
                                                             @PathVariable String species) {
        Collection<TopLevelPathway> rtn = topLevelPathwayService.getTopLevelPathways(species);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No TopLevelPathways were found for species: " + species);
        infoLogger.info("Request for toplevelpathways with species: {}", species);
        return rtn;
    }

    @ApiOperation(value = "A list of lower level pathways for a given entity or event", notes = "Retrieves the list of the lower level pathways where the passed PhysicalEntity or Event are present") //, response = SimpleDatabaseObject.class, responseContainer = "List")
    @RequestMapping(value = "/pathways/low/entity/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysFor(@ApiParam(defaultValue = "R-HSA-199420") @PathVariable String id,
                                                           @ApiParam(defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId) {
        Collection<SimpleDatabaseObject> rtn = pathwaysService.getPathwaysFor(id, speciesId);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + speciesId);
        infoLogger.info("Request for all lower level pathways of entry with id: {}", id);
        return rtn;
    }

    @ApiOperation(value = "A list of lower level pathways for all forms of given entity", notes = "Retrieves the list of the lower level pathways where all the forms of the passed PhysicalEntity are present") //, response = SimpleDatabaseObject.class, responseContainer = "List")
    @RequestMapping(value = "/pathways/low/entity/{id}/allForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysForAllFormsOf(@ApiParam(defaultValue = "R-HSA-199420") @PathVariable String id,
                                                                     @ApiParam(defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId) {
        Collection<SimpleDatabaseObject> rtn = pathwaysService.getPathwaysForAllFormsOf(id, speciesId);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + speciesId);
        infoLogger.info("Request for all lower level pathways of entry with id: {}", id);
        return rtn;
    }

    @ApiOperation(value = "A list of lower level pathways with diagram for a given entity or event", notes = "Retrieves the list of the lower level pathways which has diagram where the passed PhysicalEntity or Event are present") //, response = SimpleDatabaseObject.class, responseContainer = "List")
    @RequestMapping(value = "/pathways/low/diagram/entity/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysWithDiagramFor(@ApiParam(defaultValue = "R-HSA-199420") @PathVariable String id,
                                                                      @ApiParam(defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId) {
        Collection<SimpleDatabaseObject> rtn = pathwaysService.getPathwaysWithDiagramFor(id, speciesId);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + speciesId);
        infoLogger.info("Request for all lower level pathways (containing diagrams) of entry with id: {}", id);
        return rtn;
    }

    @ApiOperation(value = "A list of lower level pathways with diagram for all forms of given entity", notes = "Retrieves the list of the lower level pathways which has diagram where all the forms of the passed PhysicalEntity are present") //, response = SimpleDatabaseObject.class, responseContainer = "List")
    @RequestMapping(value = "/pathways/low/diagram/entity/{id}/allForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysWithDiagramForAllFormsOf(@ApiParam(defaultValue = "R-HSA-199420") @PathVariable String id,
                                                                                @ApiParam(defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId) {
        Collection<SimpleDatabaseObject> rtn = pathwaysService.getPathwaysWithDiagramForAllFormsOf(id, speciesId);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + speciesId);
        infoLogger.info("Request for all lower level pathways (containing diagrams) of entry with id: {}", id);
        return rtn;
    }

    //##################### API Ignored but still available for internal purposes #####################//


    @ApiIgnore
    @ApiOperation(value = "All Reactome top level pathways", notes = "This method retrieves a list containing only curated top level pathways for the given species")
    @RequestMapping(value = "/pathways/top/{species}/curated", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<? extends Pathway> getCuratedTopLevelPathways(@ApiParam(value = "Specifies the species by SpeciesName (eg: Homo sapiens) or SpeciesTaxId (eg: 9606)", defaultValue = "9606")
                                                                    @PathVariable String species) {
        Collection<TopLevelPathway> topLevelPathways = topLevelPathwayService.getCuratedTopLevelPathways(species);
        if (topLevelPathways == null || topLevelPathways.isEmpty()) throw new NotFoundException("No TopLevelPathways were found for species: " + species);
        infoLogger.info("Request for curated toplevelpathways with species: {}", species);
        return topLevelPathways;
    }
}
