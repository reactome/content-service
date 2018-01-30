package org.reactome.server.service.controller.graph;

import io.swagger.annotations.*;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.OrthologyService;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@RestController
@Api(tags = "orthology", description = "Reactome Data: Orthology related queries")
@RequestMapping("/data")
public class OrthologyController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private OrthologyService orthologyService;

    @ApiOperation(value = "The orthology for a given event or entity", notes = "Reactome uses the set of manually curated human reactions to computationally infer reactions in twenty evolutionarily divergent eukaryotic species for which high-quality whole-genome sequence data are available, and hence a comprehensive and high-quality set of protein predictions exists. Thus, this method retrieves the orthology for any given event or entity in the specified species. <a href=\"//www.reactome.org/pages/documentation/electronically-inferred-events/\" target=\"_blank\">Here</a> you can find more information about the computationally inferred events.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Species does not match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/orthology/{id}/species/{speciesId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject getOrthology(@ApiParam(value = "The event for which the orthology is requested", defaultValue = "R-HSA-6799198", required = true)
                                       @PathVariable String id,
                                       @ApiParam(value = "The species for which the orthology is requested", defaultValue = "49633", required = true)
                                       @PathVariable Long speciesId) {
        Collection<DatabaseObject> orthology = orthologyService.getOrthology(id, speciesId);
        if (orthology == null) throw new NotFoundException("No orthology found for '" + id + "' in species '" + speciesId + "'");
        infoLogger.info("Request for orthology of Entry with id: {} and species: {}", id, speciesId);
        return orthology.iterator().next(); //here we only retrieve the first one
    }

    @ApiOperation(value = "The orthologies of a given set of events or entities", notes = "Reactome uses the set of manually curated human reactions to computationally infer reactions in twenty evolutionarily divergent eukaryotic species for which high-quality whole-genome sequence data are available, and hence a comprehensive and high-quality set of protein predictions exists. Thus, this method retrieves the orthologies for any given set of events or entities in the specified species. <a href=\"/documentation/inferred-events/\" target=\"_blank\">Here</a> you can find more information about the computationally inferred events.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Species does not match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/orthologies/ids/species/{speciesId}", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Map<Object, DatabaseObject> getOrthologies(@ApiParam(value = "The species for which the orthology is requested", defaultValue = "49633", required = true)
                                         @PathVariable Long speciesId,
                                         @RequestBody String post) {
        Collection<Object> ids = new ArrayList<>();
        for (String id : post.split(",|;|\\n|\\t")) {
            ids.add(id.trim());
        }
        if (ids.size() > 20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Map<Object, DatabaseObject> orthologies = new HashMap<>();
        final Map<Object, Collection<DatabaseObject>> aux = orthologyService.getOrthologies(ids, speciesId);
        aux.keySet().forEach(key -> {
            try {
                orthologies.put(key, aux.get(key).iterator().next()); //Only the first one is kept
            } catch (NullPointerException | NoSuchElementException ex){/* Nothing here */}
        });
        if (orthologies.isEmpty()) throw new NotFoundException("No orthologies found");
        infoLogger.info("Request for orthology of Entries with ids: {} and species: {}", ids, speciesId);
        return orthologies;
    }
}
