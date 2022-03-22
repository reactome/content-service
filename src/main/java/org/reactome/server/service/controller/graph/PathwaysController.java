package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.TopLevelPathway;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.PathwaysService;
import org.reactome.server.graph.service.TopLevelPathwayService;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.exception.NotFoundTextPlainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "pathways", description = "Reactome Data: Pathway related queries")
@RequestMapping("/data")
public class PathwaysController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private PathwaysService pathwaysService;

    @Autowired
    private TopLevelPathwayService topLevelPathwayService;

    @Operation(
            summary = "All the events contained in the given event",
            description = "Events are the building blocks used in Reactome to represent all biological processes, and they include pathways and reactions. Typically, an event can contain other events. For example, a pathway can contain smaller pathways and reactions. This method recursively retrieves all the events contained in any given event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No contained events found in the given event"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/pathway/{id}/containedEvents", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Event> getContainedEvents(@Parameter(description = "The event for which the contained events are requested", example = "R-HSA-5673001", required = true) @PathVariable String id) {
        Collection<Event> containedEvents = pathwaysService.getContainedEvents(id);
        if (containedEvents == null || containedEvents.isEmpty())
            throw new NotFoundException("No contained events found in the given event: " + id);
        infoLogger.info("Request for contained events of event with id: {}", id);
        return containedEvents;
    }

    @Operation(summary = "A single property for each event contained in the given event", description = "Events are the building blocks used in Reactome to represent all biological processes, and they include pathways and reactions. Typically, an event can contain other events. For example, a pathway can contain smaller pathways (subpathways) and reactions.<br> This method recursively retrieves a single attribute for each of the events contained in the given event.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No contained events found in the given event or invalid attribute name"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/pathway/{id}/containedEvents/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getContainedEvents(@Parameter(description = "The event for which the contained events are requested", example = "R-HSA-5673001", required = true) @PathVariable String id,
                                     @Parameter(description = "Attribute to be filtered", example = "stId", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<Event> containedEvents = pathwaysService.getContainedEvents(id);
        if (containedEvents == null || containedEvents.isEmpty())
            throw new NotFoundTextPlainException("No contained events found in the given event: " + id);
        infoLogger.info("Request for contained events of event with id: {}", id);
        return ControllerUtils.getProperties(containedEvents, attributeName).toString();
    }

    @Operation(
            summary = "All Reactome top level pathways",
            description = "This method retrieves the list of top level pathways for the given species"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No TopLevelPathways were found for species"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/pathways/top/{species:.+}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<? extends Pathway> getTopLevelPathways(@Parameter(description = "Specifies the species by the taxonomy identifier (eg: 9606) or species name (eg: 'Homo+sapiens')", example = "9606", required = true)
                                                             @PathVariable String species) throws UnsupportedEncodingException {
        Collection<TopLevelPathway> rtn = topLevelPathwayService.getTopLevelPathways(URLDecoder.decode(species, "UTF-8"));
        if (rtn == null || rtn.isEmpty())
            throw new NotFoundException("No TopLevelPathways were found for species: " + species);
        infoLogger.info("Request for toplevelpathways with species: {}", species);
        return rtn;
    }

    @Operation(
            summary = "A list of lower level pathways containing a given entity or event",
            description = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that contain the given PhysicalEntity or Event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier is not present in any pathways"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/pathways/low/entity/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getPathwaysFor(@Parameter(description = "The entity that has to be present in the pathways", example = "R-HSA-199420", required = true) @PathVariable String id,
                                              @Parameter(description = "The species for which the pathways are requested. Taxonomy identifier (eg: 9606) or species name (eg: 'Homo sapiens')", example = "9606") @RequestParam(required = false) String species) {
        Collection<Pathway> rtn = pathwaysService.getPathwaysFor(id, species);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + species);
        infoLogger.info("Request for all lower level pathways of entry with id: {}", id);
        return rtn;
    }

    @Operation(
            summary = "A list of lower level pathways containing any form of a given entity",
            description = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that contain the given PhysicalEntity in any of its variant forms. These variant forms include for example different post-translationally modified versions of a single protein, or the same chemical in different compartments."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier (in any of its forms) is not present in any pathways"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/pathways/low/entity/{id}/allForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getPathwaysForAllFormsOf(@Parameter(description = "The entity (in any of its forms) that has to be present in the pathways", example = "R-HSA-199420", required = true) @PathVariable String id,
                                                        @Parameter(description = "The species for which the pathways are requested. Taxonomy identifier (eg: 9606) or species name (eg: 'Homo sapiens')", example = "9606") @RequestParam(required = false) String species) {
        Collection<Pathway> rtn = pathwaysService.getPathwaysForAllFormsOf(id, species);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + species);
        infoLogger.info("Request for all lower level pathways of entry with id: {}", id);
        return rtn;
    }

    @Operation(
            summary = "A list of lower level pathways with diagram containing a given entity or event",
            description = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that have a diagram and contain the given PhysicalEntity or Event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier is not present in any pathways with diagram"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/pathways/low/diagram/entity/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getPathwaysWithDiagramFor(@Parameter(description = "The entity that has to be present in the pathways", example = "R-HSA-199420", required = true) @PathVariable String id,
                                                         @Parameter(description = "The species for which the pathways are requested. Taxonomy identifier (eg: 9606) or species name (eg: 'Homo sapiens')", example = "9606") @RequestParam(required = false) String species) {
        Collection<Pathway> rtn = pathwaysService.getPathwaysWithDiagramFor(id, species);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + species);
        infoLogger.info("Request for all lower level pathways (containing diagrams) of entry with id: {}", id);
        return rtn;
    }

    @Operation(
            summary = "A list of lower level pathways with diagram containing any form of a given entity",
            description = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that have a diagram and contain the given PhysicalEntity in any of its variant forms. These variant forms include for example different post-translationally modified versions of a single protein, or the same chemical in different compartments."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier (in any of its forms) is not present in any pathways with diagram"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/pathways/low/diagram/entity/{id}/allForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getPathwaysWithDiagramForAllFormsOf(@Parameter(description = "The entity (in any of its forms) that has to be present in the pathways", example = "R-HSA-199420", required = true) @PathVariable String id,
                                                                   @Parameter(description = "The species for which the pathways are requested. Taxonomy identifier (eg: 9606) or species name (eg: 'Homo sapiens')", example = "9606") @RequestParam(required = false) String species) {
        Collection<Pathway> rtn = pathwaysService.getPathwaysWithDiagramForAllFormsOf(id, species);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + id + " in " + species);
        infoLogger.info("Request for all lower level pathways (containing diagrams) of entry with id: {}", id);
        return rtn;
    }

    //##################### API Ignored but still available for internal purposes #####################//

    @Hidden
    @Operation(
            summary = "A list of lower level pathways with diagram containing any form of a given identifier",
            description = "This method traverses the event hierarchy and retrieves the list of all lower level pathways that have a diagram and contain the given PhysicalEntity in any of its variant forms. These variant forms include for example different post-translationally modified versions of a single protein, or the same chemical in different compartments."
    )
    @RequestMapping(value = "/pathways/low/diagram/identifier/{identifier}/allForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getLowerLevelPathwaysForIdentifier(@Parameter(description = "The entity (in any of its forms) that has to be present in the pathways", example = "PTEN", required = true) @PathVariable String identifier,
                                                                  @Parameter(description = "The species for which the pathways are requested. Taxonomy identifier (eg: 9606) or species name (eg: 'Homo sapiens'", example = "9606") @RequestParam(required = false) String species) {
        Collection<Pathway> rtn = pathwaysService.getLowerLevelPathwaysForIdentifier(identifier.toUpperCase(), species);
        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No result for " + identifier + " in " + species);
        infoLogger.info("Request for all lower level pathways (containing diagrams) of entry with identifier: {}", identifier);
        return rtn;
    }

    @Hidden
    @Operation(summary = "A list of diagram entities plus pathways from the provided list containing the specified identifier", description = "This method traverses the content and checks not only for the main identifier but also for all the cross-references to find the flag targets")
    @RequestMapping(value = "/diagram/{pathwayId}/entities/{identifier}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getEntitiesInDiagramForIdentifier(@Parameter(description = "The pathway to find items to flag", example = "R-HSA-446203")
                                                                              @PathVariable String pathwayId,
                                                                              @Parameter(description = "The identifier for the elements to be flagged", example = "CTSA")
                                                                              @PathVariable String identifier,
                                                                              @Parameter(description = "Encapsulated pathways to be checked (comma separated list - 20 max)", example = "R-HSA-199977,R-HSA-4085001")
                                                                              @RequestParam(required = false) Collection<String> pathways) {

        if (pathways != null && pathways.size() > 20)
            pathways = pathways.stream().skip(0).limit(20).collect(Collectors.toSet());

        Collection<SimpleDatabaseObject> rtn = new HashSet<>();
        Collection<SimpleDatabaseObject> aux = pathwaysService.getDiagramEntitiesForIdentifier(pathwayId, identifier.toUpperCase());
        if (aux != null && !aux.isEmpty()) rtn.addAll(aux);
        if (pathways != null) {
            aux = pathwaysService.getPathwaysForIdentifier(identifier.toUpperCase(), pathways);
            if (aux != null && !aux.isEmpty()) rtn.addAll(aux);
        }
        if (rtn.isEmpty())
            throw new NotFoundException("No entities with identifier '" + identifier + "' found for " + pathwayId + (pathways != null ? " nor for pathways " + pathways : ""));
        infoLogger.info("Request for all entities in diagram with identifier: {}", identifier);
        return rtn;
    }

    @Hidden
    @Operation(summary = "All Reactome top level pathways", description = "This method retrieves a list containing only curated top level pathways for the given species")
    @RequestMapping(value = "/pathways/top/{species}/curated", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<? extends Pathway> getCuratedTopLevelPathways(@Parameter(description = "Specifies the species by SpeciesName (eg: Homo sapiens) or SpeciesTaxId (eg: 9606)", example = "9606") @PathVariable String species) {
        Collection<TopLevelPathway> topLevelPathways = topLevelPathwayService.getCuratedTopLevelPathways(species);
        if (topLevelPathways == null || topLevelPathways.isEmpty())
            throw new NotFoundException("No TopLevelPathways were found for species: " + species);
        infoLogger.info("Request for curated toplevelpathways with species: {}", species);
        return topLevelPathways;
    }
}
