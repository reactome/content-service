package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.domain.result.Participant;
import org.reactome.server.graph.service.ParticipantService;
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
@Api(tags = "participants", description = "Reactome Data: Participant queries" )
@RequestMapping("/data")
public class ParticipantController {

    @Autowired
    private ParticipantService participantService;

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",
            notes = "Participants contains a PhysicalEntity (dbId, displayName) and a collection of ReferenceEntities (dbId, name, identifier, url)",
            response = Participant.class,
            responseContainer = "List",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participants", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Participant> getParticipants(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "5205685",required = true) @PathVariable String id)  {
        Collection<Participant> participants = participantService.getParticipants(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        return participants;
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",
            notes = "Participants contains a PhysicalEntity (dbId, displayName) and a collection of ReferenceEntities (dbId, name, identifier, url). Retrieves a single property from the list of DatabaseObjects.",
            response = Participant.class,
            responseContainer = "List",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participants/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Object> getParticipants(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "5205685",required = true) @PathVariable String id,
                                              @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<Participant> participants = participantService.getParticipants(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        return ControllerUtils.getProperties(participants, attributeName);
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",
            notes = "Retrieves a collection of PhysicalEntities",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participatingPhysicalEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getParticipatingPhysicalEntities(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-5205685",required = true) @PathVariable String id)  {
        Collection<PhysicalEntity> participants = participantService.getParticipatingPhysicalEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        return participants;
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",
            notes = "Retrieves a collection of PhysicalEntities. Retrieves a single property from the list of DatabaseObjects.",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participatingPhysicalEntities/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Object> getParticipatingPhysicalEntities(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-5205685",required = true) @PathVariable String id,
                                                               @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<PhysicalEntity> participants = participantService.getParticipatingPhysicalEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        return ControllerUtils.getProperties(participants, attributeName);
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",
            notes = "Retrieves a collection of ReferenceEntitites.",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participatingReferenceEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getParticipationgReferenceEntities(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "5205685",required = true) @PathVariable String id)  {
        Collection<ReferenceEntity> participants = participantService.getParticipatingReferenceEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        return participants;
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",
            notes = "Retrieves a collection of ReferenceEntitites. Retrieves a single property from the list of DatabaseObjects.",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participatingReferenceEntities/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Object> getParticipationgReferenceEntities(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "5205685",required = true) @PathVariable String id,
                                                                 @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<ReferenceEntity> participants = participantService.getParticipatingReferenceEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        return ControllerUtils.getProperties(participants, attributeName);
    }
}
