package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.service.HierarchyService;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;
import org.reactome.server.graph.service.util.PathwayBrowserLocationsUtils;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Set;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
//@RestController
@Hidden
@Tag(name = "hierarchy")
@RequestMapping("/data")
@Deprecated
public class HierarchyController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private HierarchyService eventHierarchyService;

    @Operation(summary = "Retrieves a sub graph for given id",
            description = "Sub graph will be created following all outgoing relationships of type: hasEvent, input, output, hasCandidate, hasMember, hasComponent, repeatedUnit. PathwayBrowserNode contains: stId, name, species, url, type, diagram.")
    @RequestMapping(value = "/detail/{id}/getSubGraph", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public PathwayBrowserNode getSubGraph(@Parameter(description = "DbId or StId of a PhysicalEntity", example = "R-HSA-109581", required = true) @PathVariable String id) {
        PathwayBrowserNode pathwayBrowserNode = eventHierarchyService.getSubHierarchy(id);
        if (pathwayBrowserNode == null) throw new NotFoundException("No sub graph found for given id: " + id);
        infoLogger.info("Request for subgraph of Entry id: {}", id);
        return pathwayBrowserNode;
    }


    @Operation(summary = "Retrieves a full reverse graph for given id",
            description = "Reverse sub graph will be created following all incoming relationships of type: hasEvent, input, output, hasCandidate, hasMember, hasComponent, repeatedUnit, regulatedBy, regulator, physicalEntity, requiredInputComponent, entityFunctionalStatus, activeUnit, catalystActivity. PathwayBrowserNode contains: stId, name, species, url, type, diagram.")
    @RequestMapping(value = "/detail/{id}/getReverseGraph", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public PathwayBrowserNode getReverseSubGraph(@Parameter(description = "DbId or StId of a PhysicalEntity", example = "R-HSA-199420", required = true) @PathVariable String id,
                                                 @Parameter(description = "Direct Participants are proteins or molecules, direcly involved in Reactions.", example = "false") @RequestParam(required = false) Boolean directParticipants,
                                                 @Parameter(description = "Items like Catalysts or Regulations can not be displayed in the PWB, omit to avoid them in tree.", example = "true") @RequestParam(required = false) Boolean omitNonDisplayableItems) {
        PathwayBrowserNode pathwayBrowserNode = eventHierarchyService.getLocationsInPathwayBrowser(id, directParticipants, omitNonDisplayableItems);
        if (pathwayBrowserNode == null) throw new NotFoundException("Reverse sub graph found for id: " + id);
        infoLogger.info("Request for reverse subgraph of Entry with id: {}", id);
        return pathwayBrowserNode;
    }

    @Operation(
            summary = "Retrieves a locations in PWB graph for given id",
            description = "This method retrieves multiple trees, where each root is a TopLevelPathway. Each leaf is a copy of the initially searched entry with a unique url pointing to a location in the PWB. Initial graph will be created following all incoming relationships of type: hasEvent, input, output, hasCandidate, hasMember, hasComponent, repeatedUnit, regulatedBy, regulator, physicalEntity, requiredInputComponent, entityFunctionalStatus, activeUnit, catalystActivity. PathwayBrowserNode contains: stId, name, species, url, type, diagram."
    )
    @RequestMapping(value = "/detail/{id}/locationsInPWB", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PathwayBrowserNode> getPWBGraph(@Parameter(description = "DbId or StId of a PhysicalEntity", example = "R-HSA-199420", required = true) @PathVariable String id,
                                                      @Parameter(description = "Direct Participants are proteins or molecules, direcly involved in Reactions.", example = "false") @RequestParam(required = false) Boolean directParticipants,
                                                      @Parameter(description = "Items like Catalysts or Regulations can not be displayed in the PWB, omit to avoid them in tree.", example = "true") @RequestParam(required = false) Boolean omitNonDisplayableItems) {
        PathwayBrowserNode pathwayBrowserNode = eventHierarchyService.getLocationsInPathwayBrowser(id, directParticipants, omitNonDisplayableItems);
        if (pathwayBrowserNode == null) throw new NotFoundException("Reverse sub graph found for id: " + id);
        Set<PathwayBrowserNode> pathwayBrowserNodes = pathwayBrowserNode.getLeaves();
        pathwayBrowserNodes = PathwayBrowserLocationsUtils.removeOrphans(pathwayBrowserNodes);
        pathwayBrowserNodes = PathwayBrowserLocationsUtils.buildTreesFromLeaves(pathwayBrowserNodes);
        if (pathwayBrowserNodes == null || pathwayBrowserNodes.isEmpty())
            throw new NotFoundException("No Locations in the PathwayBrowser could have been found for id: " + id);
        infoLogger.info("Request for locationsgraph of Entry with id: {}", id);
        return pathwayBrowserNodes;
    }

}
