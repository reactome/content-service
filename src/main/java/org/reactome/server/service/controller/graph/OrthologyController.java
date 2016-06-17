package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.OrthologyService;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@RestController
@Api(tags = "orthology", description = "Reactome Data: Orthology related queries")
@RequestMapping("/data")
public class OrthologyController {

    @Autowired
    private OrthologyService orthologyService;

    @ApiOperation(value = "The orthology for a given event or entity", notes = "ToDo")
    @RequestMapping(value = "/orthology/{id}/species/{speciesId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject getOrthology(@ApiParam(value = "The event for which the orthology is requested", defaultValue = "R-HSA-6799198", required = true)
                                       @PathVariable String id,
                                       @ApiParam(value = "The species for which the orthology is requested", defaultValue = "49633", required = true)
                                       @PathVariable Long speciesId) {
        DatabaseObject orthology = orthologyService.getOrthology(id, speciesId);
        if (orthology == null)
            throw new NotFoundException("No orthology found for '" + id + "' in species '" + speciesId + "'");
        return orthology;
    }

    @ApiOperation(value = "The orthologies of a given set of events or entities", notes = "ToDo")
    @RequestMapping(value = "/ortholgies/ids/species/{speciesId}", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Map<Object, DatabaseObject> getOrthologies(@ApiParam(value = "The species for which the orthology is requested", defaultValue = "49633", required = true)
                                         @PathVariable Long speciesId,
                                         @RequestBody String post) {
        Collection<Object> ids = new ArrayList<>();
        for (String id : post.split(",|;|\\n|\\t")) {
            ids.add(id.trim());
        }
        if (ids.size() > 20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Map<Object, DatabaseObject> orthologies = orthologyService.getOrthologies(ids, speciesId);
        if (orthologies.isEmpty())
            throw new NotFoundException("No orthologies found");
        return orthologies;
    }
}
