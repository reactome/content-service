package org.reactome.server.service.controller.graph;

import io.swagger.annotations.*;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.domain.result.Participant;
import org.reactome.server.graph.service.ParticipantService;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@RestController
@Api(tags = "participants", description = "Reactome Data: Queries related to participants")
@RequestMapping("/data")
public class ParticipantsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private final ParticipantService participantService;

    @Autowired
    public ParticipantsController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @ApiOperation(value = "A list of participants for a given event",
            notes = "Participants contains a PhysicalEntity (dbId, displayName) and a collection of ReferenceEntities (dbId, name, identifier, url)",
            response = Participant.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier does not match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/participants/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Participant> getParticipants(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "5205685",required = true) @PathVariable String id)  {
        Collection<Participant> participants = participantService.getParticipants(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

    @ApiOperation(value = "A list of participating PhysicalEntities for a given event", notes = "This method retrieves all the PhysicalEntities that take part in a given event. It is worth mentioning that because a pathway can contain smaller pathways (subpathways), the method also recursively retrieves the PhysicalEntities from every constituent pathway.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier does not match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/participants/{id}/participatingPhysicalEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getParticipatingPhysicalEntities(@ApiParam(value = "The event for which the participating PhysicalEntities are requested", defaultValue = "R-HSA-5205685",required = true) @PathVariable String id)  {
        Collection<PhysicalEntity> participants = participantService.getParticipatingPhysicalEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

    @ApiOperation(value = "A list of participating ReferenceEntities for a given event", notes = "PhysicalEntity instances that represent, e.g., the same chemical in different compartments, or different post-translationally modified forms of a single protein, share numerous invariant features such as names, molecular structure and links to external databases like UniProt or ChEBI.<br>To enable storage of this shared information in a single place, and to create an explicit link among all the variant forms of what can also be seen as a single chemical entity, Reactome creates instances of the separate ReferenceEntity class. A ReferenceEntity instance captures the invariant features of a molecule.<br>This method retrieves the ReferenceEntities of all PhysicalEntities that take part in a given event. It is worth mentioning that because a pathway can contain smaller pathways (subpathways), this method also recursively retrieves the ReferenceEntities for all PhysicalEntities in every constituent pathway.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier does not match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/participants/{id}/referenceEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getParticipatingReferenceEntities(@ApiParam(value = "The event for which the participating ReferenceEntities are requested", defaultValue = "5205685",required = true) @PathVariable String id)  {
        Collection<ReferenceEntity> participants = participantService.getParticipatingReferenceEntities(id);
        if (participants == null || participants.isEmpty())  throw new NotFoundException("No participants found for id: " + id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }
}
