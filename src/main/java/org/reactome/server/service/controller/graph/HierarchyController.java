package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.repository.DetailsRepository;
import org.reactome.server.graph.service.DetailsService;
import org.reactome.server.graph.service.HierarchyService;
import org.reactome.server.graph.service.ParticipantService;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;
import org.reactome.server.graph.service.util.PathwayBrowserLocationsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Set;

/**
 * Created by flo on 24/05/16.
 */
@RestController
@Api(tags = "hierarchy", description = "Reactome Data " )
@RequestMapping("/data")
public class HierarchyController {

    @Autowired
    private HierarchyService eventHierarchyService;

    @Autowired
    private DetailsService detailsService;

    @ApiOperation(value = "Retrieves a full Event hierarchy",response = PathwayBrowserNode.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/getEventHierarchy/{speciesName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PathwayBrowserNode> getEventHierarchy(@ApiParam(defaultValue = "Homo sapiens",required = true) @PathVariable String speciesName)  {
        return eventHierarchyService.getEventHierarchy(speciesName);
    }

    @ApiOperation(value = "Retrieves a full Sub graph for given id",response = PathwayBrowserNode.class, produces = "application/json")
    @RequestMapping(value = "/detail/getSubGraph/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public PathwayBrowserNode getSubGraph(@ApiParam(defaultValue = "R-HSA-109581",required = true) @PathVariable String id)  {
        return eventHierarchyService.getSubHierarchy(id);
    }

    @ApiOperation(value = "Retrieves a full reverse graph for given id",response = PathwayBrowserNode.class, produces = "application/json")
    @RequestMapping(value = "/detail/getReverseGraph/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public PathwayBrowserNode getReverseGraph(@ApiParam(defaultValue = "R-HSA-199420",required = true) @PathVariable String id,
                                              @RequestParam(required = false, defaultValue = "false") Boolean directParticipants)  {
        return detailsService.getLocationsInThePathwayBrowser(id,directParticipants);
    }

    @ApiOperation(value = "Retrieves a locations in PWB graph for given id",response = PathwayBrowserNode.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/getPWBGraph/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PathwayBrowserNode> getPWBGraph(@ApiParam(defaultValue = "R-HSA-199420",required = true) @PathVariable String id,
                                              @RequestParam(required = false, defaultValue = "false") Boolean directParticipants)  {
        Set<PathwayBrowserNode> nodes = detailsService.getLocationsInThePathwayBrowserHierarchy(id, directParticipants);
        nodes = PathwayBrowserLocationsUtils.removeOrphans(nodes);
        return PathwayBrowserLocationsUtils.buildTreesFromLeaves(nodes);
//        PathwayBrowserLocationsUtils.convertParentsToChildren(nodes);
//        return nodes;
    }




}
