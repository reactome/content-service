package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.service.HierarchyService;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;
import org.reactome.server.graph.service.util.PathwayBrowserLocationsUtils;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collection;
import java.util.Set;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
//@RestController
@ApiIgnore
@Api(tags = "hierarchy", description = "Reactome Data: Hierarchical queries." )
@RequestMapping("/data")
@Deprecated
public class HierarchyController {

    @Autowired
    private HierarchyService eventHierarchyService;

    @ApiOperation(value = "Retrieves a sub graph for given id",
            notes = "Sub graph will be created following all outgoing relationships of type: hasEvent, input, output, hasCandidate, hasMember, hasComponent, repeatedUnit. PathwayBrowserNode contains: stId, name, species, url, type, diagram.",
            response = PathwayBrowserNode.class,
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/getSubGraph", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public PathwayBrowserNode getSubGraph(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-109581", required = true) @PathVariable String id)  {
        PathwayBrowserNode pathwayBrowserNode = eventHierarchyService.getSubHierarchy(id);
        if (pathwayBrowserNode == null) throw new NotFoundException("No sub graph found for given id: " + id);
        return pathwayBrowserNode;
    }


    @ApiOperation(value = "Retrieves a full reverse graph for given id",
            notes = "Reverse sub graph will be created following all incoming relationships of type: hasEvent, input, output, hasCandidate, hasMember, hasComponent, repeatedUnit, regulatedBy, regulator, physicalEntity, requiredInputComponent, entityFunctionalStatus, activeUnit, catalystActivity. PathwayBrowserNode contains: stId, name, species, url, type, diagram.",
            response = PathwayBrowserNode.class,
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/getReverseGraph", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public PathwayBrowserNode getReverseSubGraph(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-199420",required = true) @PathVariable String id,
                                                 @ApiParam(value = "Direct Participants are proteins or molecules, direcly involved in Reactions.", defaultValue = "false") @RequestParam(required = false) Boolean directParticipants,
                                                 @ApiParam(value = "Items like Catalysts or Regulations can not be displayed in the PWB, omit to avoid them in tree.", defaultValue = "true") @RequestParam(required = false) Boolean omitNonDisplayableItems)  {
        PathwayBrowserNode pathwayBrowserNode = eventHierarchyService.getLocationsInPathwayBrowser(id, directParticipants, omitNonDisplayableItems);
        if (pathwayBrowserNode == null) throw new NotFoundException("Reverse sub graph found for id: " + id);
        return pathwayBrowserNode;
    }

    @ApiOperation(value = "Retrieves a locations in PWB graph for given id",
            notes = "This method retrieves multiple trees, where each root is a TopLevelPathway. Each leaf is a copy of the initially searched entry with a unique url pointing to a location in the PWB. Initial graph will be created following all incoming relationships of type: hasEvent, input, output, hasCandidate, hasMember, hasComponent, repeatedUnit, regulatedBy, regulator, physicalEntity, requiredInputComponent, entityFunctionalStatus, activeUnit, catalystActivity. PathwayBrowserNode contains: stId, name, species, url, type, diagram.",
            response = PathwayBrowserNode.class,
            responseContainer = "List",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/locationsInPWB", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PathwayBrowserNode> getPWBGraph(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-199420",required = true) @PathVariable String id,
                                                      @ApiParam(value = "Direct Participants are proteins or molecules, direcly involved in Reactions.", defaultValue = "false") @RequestParam(required = false) Boolean directParticipants,
                                                      @ApiParam(value = "Items like Catalysts or Regulations can not be displayed in the PWB, omit to avoid them in tree.", defaultValue = "true") @RequestParam(required = false) Boolean omitNonDisplayableItems)  {
        PathwayBrowserNode pathwayBrowserNode = eventHierarchyService.getLocationsInPathwayBrowser(id, directParticipants, omitNonDisplayableItems);
        if (pathwayBrowserNode == null) throw new NotFoundException("Reverse sub graph found for id: " + id);
        Set<PathwayBrowserNode> pathwayBrowserNodes = pathwayBrowserNode.getLeaves();
        pathwayBrowserNodes = PathwayBrowserLocationsUtils.removeOrphans(pathwayBrowserNodes);
        pathwayBrowserNodes = PathwayBrowserLocationsUtils.buildTreesFromLeaves(pathwayBrowserNodes);
        if (pathwayBrowserNodes == null || pathwayBrowserNodes.isEmpty()) throw new NotFoundException("No Locations in the PathwayBrowser could have been found for id: " + id);
        return pathwayBrowserNodes;
    }

}
