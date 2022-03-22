package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.domain.result.Participant;
import org.reactome.server.graph.service.ParticipantService;
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
@Tag(name = "participants", description = "Reactome Data: Queries related to participants")
@RequestMapping("/data")
public class ParticipantsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private final ParticipantService participantService;

    @Autowired
    public ParticipantsController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @Operation(
            summary = "A list of participants for a given event",
            description = "Participants contains a PhysicalEntity (dbId, displayName) and a collection of ReferenceEntities (dbId, name, identifier, url)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/participants/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Participant> getParticipants(@Parameter(description = "DbId or StId of an Event", example = "5205685", required = true) @PathVariable String id) {
        Collection<Participant> participants = participantService.getParticipants(id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

    @Operation(
            summary = "A list of participating PhysicalEntities for a given event",
            description = "This method retrieves all the PhysicalEntities that take part in a given event. It is worth mentioning that because a pathway can contain smaller pathways (subpathways), the method also recursively retrieves the PhysicalEntities from every constituent pathway."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/participants/{id}/participatingPhysicalEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getParticipatingPhysicalEntities(@Parameter(description = "The event for which the participating PhysicalEntities are requested", example = "R-HSA-5205685", required = true) @PathVariable String id) {
        Collection<PhysicalEntity> participants = participantService.getParticipatingPhysicalEntities(id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }

    @Operation(
            summary = "A list of participating ReferenceEntities for a given event",
            description = "PhysicalEntity instances that represent, e.g., the same chemical in different compartments, or different post-translationally modified forms of a single protein, share numerous invariant features such as names, molecular structure and links to external databases like UniProt or ChEBI.<br>To enable storage of this shared information in a single place, and to create an explicit link among all the variant forms of what can also be seen as a single chemical entity, Reactome creates instances of the separate ReferenceEntity class. A ReferenceEntity instance captures the invariant features of a molecule.<br>This method retrieves the ReferenceEntities of all PhysicalEntities that take part in a given event. It is worth mentioning that because a pathway can contain smaller pathways (subpathways), this method also recursively retrieves the ReferenceEntities for all PhysicalEntities in every constituent pathway."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/participants/{id}/referenceEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getParticipatingReferenceEntities(@Parameter(description = "The event for which the participating ReferenceEntities are requested", example = "5205685", required = true) @PathVariable String id) {
        Collection<ReferenceEntity> participants = participantService.getParticipatingReferenceEntities(id);
        infoLogger.info("Request for participants of event with id: {}", id);
        return participants;
    }
}
