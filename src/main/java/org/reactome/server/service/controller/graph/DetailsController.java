package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.service.DetailsService;
import org.reactome.server.graph.service.helper.ContentDetails;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 19.05.16.
 */
@RestController
@Api(tags = "detail", description = "Reactome Data: Detailed queries")
@RequestMapping("/data")
public class DetailsController {

    @Autowired
    private DetailsService detailsService;

    @ApiOperation(value = "Retrieves a wrapper containing extended information about a DatabaseObject",
            notes = "ContentDetails contains: DatabaseObject, componentsOf, other forms of the entry, locationsTree.",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/extended", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ContentDetails getContentDetail(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id,
                                           @ApiParam(value = "Direct Participants are proteins or molecules, direcly involved in Reactions.", defaultValue = "false") @RequestParam(required = false) Boolean directParticipants)  {
        ContentDetails contentDetails = detailsService.getContentDetails(id, directParticipants);
        if (contentDetails == null || contentDetails.getDatabaseObject() == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return contentDetails;
    }
}
