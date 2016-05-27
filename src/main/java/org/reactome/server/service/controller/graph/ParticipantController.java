package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.domain.result.Participant;
import org.reactome.server.graph.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@RestController
@Api(tags = "participants", description = "Reactome Data " )
@RequestMapping("/data")
public class ParticipantController {

    @Autowired
    private ParticipantService participantService;

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participatingPhysicalEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getParticipatingPhysicalEntities(@ApiParam(defaultValue = "R-HSA-5205685",required = true) @PathVariable String id)  {
        return participantService.getParticipatingPhysicalEntities(id);
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",response = Participant.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participants", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Participant> getParticipatingPhysicalEntities2(@ApiParam(defaultValue = "5205685",required = true) @PathVariable String id)  {
        return participantService.getParticipants(id);
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participatingReferenceEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getParticipationgReferenceEntities(@ApiParam(defaultValue = "5205685",required = true) @PathVariable String id)  {
        return participantService.getParticipatingReferenceEntities(id);
    }
}
