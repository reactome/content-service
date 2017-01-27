package org.reactome.server.service.controller.graph;

import io.swagger.annotations.*;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.TopLevelPathway;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.PathwaysService;
import org.reactome.server.graph.service.TopLevelPathwayService;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.exception.NotFoundTextPlainException;
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
@SuppressWarnings("unused")
@RestController
@Api(tags = "pathways", description = "Reactome Data: Pathway related queries")
@RequestMapping("/data")
public class PathwaysController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private PathwaysService pathwaysService;

    @Autowired
    private TopLevelPathwayService topLevelPathwayService;

    @ApiOperation(value = "All the events contained in the given event", notes = "Events are the building blocks used in Reactome to represent all biological processes, and they include pathways and reactions. Typically, an event can contain other events. For example, a pathway can contain smaller pathways and reactions. This method recursively retrieves all the events contained in any given event.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "No contained events found in the given event", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/pathway/{id}/containedEvents", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Event> getContainedEvents(@ApiParam(value = "The event for which the contained events are requested", defaultValue = "R-HSA-5673001", required = true) @PathVariable String id) {
        Collection<Event> containedEvents = pathwaysService.getContainedEvents(id);
        if (containedEvents == null || containedEvents.isEmpty()) throw new NotFoundException("No contained events found in the given event: " + id);
        infoLogger.info("Request for contained events of event with id: {}", id);
        return containedEvents;
    }

    @ApiOperation(value = "A single property for each event contained in the given event", notes = "Events are the building blocks used in Reactome to represent all biological processes, and they include pathways and reactions. Typically, an event can contain other events. For example, a pathway can contain smaller pathways (subpathways) and reactions.<br> This method recursively retrieves a single attribute for each of the events contained in the given event.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "No contained events found in the given event or invalid attribute name", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/pathway/{id}/containedEvents/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getContainedEvents(@ApiParam(value = "The event for which the contained events are requested", defaultValue = "R-HSA-5673001", required = true) @PathVariable String id,
                                                 @ApiParam(value = "Attribute to be filtered", defaultValue = "stId", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<Event> containedEvents = pathwaysService.getContainedEvents(id);
        if (containedEvents == null || containedEvents.isEmpty()) throw new NotFoundTextPlainException("No contained events found in the given event: " + id);
        infoLogger.info("Request for contained events of event with id: {}", id);
        return ControllerUtils.getProperties(containedEvents, attributeName).toString();
    }

    @ApiOperation(value = "All Reactome top level pathways", notes = "This method retrieves the list of top level pathways for the given species") //, response = Pathway.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "No TopLevelPathways were found for species", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/pathways/top/{species}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<? extends Pathway> getTopLevelPathways(@ApiParam(value = "Specifies the species by SpeciesName (eg: Homo sapiens) or SpeciesTaxId (eg: 9606)", defaultValue = "9606") @PathVariable String species) {
        Collection<TopLevelPathway> rtn = topLevelPathwayService.getTopLevelPathways(species);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No TopLevelPathways were found for species: " + species);
        infoLogger.info("Request for toplevelpathways with species: {}", species);
        return rtn;
    }

    @ApiOperation(value = "A list of lower level pathways containing a given entity or event", notes = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that contain the given PhysicalEntity or Event.") //, response = SimpleDatabaseObject.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier is not present in any pathways", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/pathways/low/entity/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysFor(@ApiParam(value = "The entity that has to be present in the pathways", defaultValue = "R-HSA-199420") @PathVariable String id,
                                                           @ApiParam(value = "The species for which the pathways are requested (SpeciesName or SpeciesTaxId)", defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId) {
        Collection<SimpleDatabaseObject> rtn = pathwaysService.getPathwaysFor(id, speciesId);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + speciesId);
        infoLogger.info("Request for all lower level pathways of entry with id: {}", id);
        return rtn;
    }

    @ApiOperation(value = "A list of lower level pathways containing any form of a given entity", notes = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that contain the given PhysicalEntity in any of its variant forms. These variant forms include for example different post-translationally modified versions of a single protein, or the same chemical in different compartments.") //, response = SimpleDatabaseObject.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier (in any of its forms) is not present in any pathways", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/pathways/low/entity/{id}/allForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysForAllFormsOf(@ApiParam(value = "The entity (in any of its forms) that has to be present in the pathways", defaultValue = "R-HSA-199420") @PathVariable String id,
                                                                     @ApiParam(value = "The species for which the pathways are requested (SpeciesName or SpeciesTaxId)", defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId) {
        Collection<SimpleDatabaseObject> rtn = pathwaysService.getPathwaysForAllFormsOf(id, speciesId);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + speciesId);
        infoLogger.info("Request for all lower level pathways of entry with id: {}", id);
        return rtn;
    }

    @ApiOperation(value = "A list of lower level pathways with diagram containing a given entity or event", notes = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that have a diagram and contain the given PhysicalEntity or Event.") //, response = SimpleDatabaseObject.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier is not present in any pathways with diagram", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/pathways/low/diagram/entity/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysWithDiagramFor(@ApiParam(value = "The entity that has to be present in the pathways", defaultValue = "R-HSA-199420") @PathVariable String id,
                                                                      @ApiParam(value = "The species for which the pathways are requested (SpeciesName or SpeciesTaxId)", defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId) {
        Collection<SimpleDatabaseObject> rtn = pathwaysService.getPathwaysWithDiagramFor(id, speciesId);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + speciesId);
        infoLogger.info("Request for all lower level pathways (containing diagrams) of entry with id: {}", id);
        return rtn;
    }

    @ApiOperation(value = "A list of lower level pathways with diagram containing any form of a given entity", notes = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that have a diagram and contain the given PhysicalEntity in any of its variant forms. These variant forms include for example different post-translationally modified versions of a single protein, or the same chemical in different compartments.") //, response = SimpleDatabaseObject.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier (in any of its forms) is not present in any pathways with diagram", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/pathways/low/diagram/entity/{id}/allForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysWithDiagramForAllFormsOf(@ApiParam(value = "The entity (in any of its forms) that has to be present in the pathways", defaultValue = "R-HSA-199420") @PathVariable String id,
                                                                                @ApiParam(value = "The species for which the pathways are requested (SpeciesName or SpeciesTaxId)", defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId) {
        Collection<SimpleDatabaseObject> rtn = pathwaysService.getPathwaysWithDiagramForAllFormsOf(id, speciesId);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + speciesId);
        infoLogger.info("Request for all lower level pathways (containing diagrams) of entry with id: {}", id);
        return rtn;
    }

    @ApiOperation(value = "A list of lower level pathways with diagram containing any form of a given identifier", notes = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that have a diagram and contain the given PhysicalEntity in any of its variant forms. These variant forms include for example different post-translationally modified versions of a single protein, or the same chemical in different compartments.") //, response = SimpleDatabaseObject.class, responseContainer = "List")
    @RequestMapping(value = "/pathways/low/diagram/identifier/{identifier}/allForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getLowerLevelPathwaysForIdentifier(@ApiParam(value = "The entity (in any of its forms) that has to be present in the pathways", defaultValue = "PTEN") @PathVariable String identifier,
                                                                                @ApiParam(value = "The species for which the pathways are requested (SpeciesName or SpeciesTaxId)", defaultValue = "48887") @RequestParam(required = false, defaultValue = "48887") Long speciesId) {
        Collection<SimpleDatabaseObject> rtn = pathwaysService.getLowerLevelPathwaysForIdentifier(identifier, speciesId);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + identifier + " in " + speciesId);
        infoLogger.info("Request for all lower level pathways (containing diagrams) of entry with identifier: {}", identifier);
        return rtn;
    }

    //##################### API Ignored but still available for internal purposes #####################//


    @ApiIgnore
    @ApiOperation(value = "All Reactome top level pathways", notes = "This method retrieves a list containing only curated top level pathways for the given species")
    @RequestMapping(value = "/pathways/top/{species}/curated", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<? extends Pathway> getCuratedTopLevelPathways(@ApiParam(value = "Specifies the species by SpeciesName (eg: Homo sapiens) or SpeciesTaxId (eg: 9606)", defaultValue = "9606") @PathVariable String species) {
        Collection<TopLevelPathway> topLevelPathways = topLevelPathwayService.getCuratedTopLevelPathways(species);
        if (topLevelPathways == null || topLevelPathways.isEmpty()) throw new NotFoundException("No TopLevelPathways were found for species: " + species);
        infoLogger.info("Request for curated toplevelpathways with species: {}", species);
        return topLevelPathways;
    }
}
