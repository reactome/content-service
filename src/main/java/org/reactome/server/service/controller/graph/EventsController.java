package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.analysis.core.model.PathwayNodeData;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.PathwayNodeSummary;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.graph.domain.result.EventProjection;
import org.reactome.server.graph.domain.result.EventProjectionWrapper;
import org.reactome.server.graph.service.EventsService;
import org.reactome.server.graph.service.HierarchyService;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.model.graph.AnalysedPathwayBrowserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "events", description = "Reactome Data: Queries related to events")
@RequestMapping("/data")
public class EventsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private final HierarchyService eventHierarchyService;
    private final EventsService eventsService;
    private final TokenUtils tokenUtils;

    @Autowired
    public EventsController(HierarchyService eventHierarchyService, EventsService eventsService, TokenUtils tokenUtils) {
        this.eventHierarchyService = eventHierarchyService;
        this.eventsService = eventsService;
        this.tokenUtils = tokenUtils;
    }

    @Operation(
            summary = "The ancestors of a given event",
            description = "The Reactome definition of events includes pathways and reactions. Although events are organised in a hierarchical structure, a single event can be in more than one location, i.e. a reaction can take part in different pathways while, in the same way, a sub-pathway can take part in many pathways. Therefore, this method retrieves a list of all possible paths from the requested event to the top level pathway(s)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/event/{id}/ancestors", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Collection<EventProjection>> getEventAncestors(@Parameter(description = "The event for which the ancestors are requested", example = "R-HSA-5673001", required = true)
                                                                     @PathVariable String id) {
        Collection<EventProjectionWrapper> ancestors = eventsService.getEventAncestors(id);
        if (ancestors == null || ancestors.isEmpty())
            throw new NotFoundException("No ancestors found for given event: " + id);
        infoLogger.info("Request for all Ancestors of Event with id: {}", id);
        return ancestors.stream().map(EventProjectionWrapper::getEvents).collect(Collectors.toList());
    }

    @Operation(summary = "The full event hierarchy for a given species", description = "Events (pathways and reactions) in Reactome are organised in a hierarchical structure for every species. By following all 'hasEvent' relationships, this method retrieves the full event hierarchy for any given species. The result is a list of tree structures, one for each TopLevelPathway. Every event in these trees is represented by a PathwayBrowserNode. The latter contains the stable identifier, the name, the species, the url, the type, and the diagram of the particular event.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Species does not match with any in current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/eventsHierarchy/{species}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PathwayBrowserNode> getEventHierarchy(@Parameter(description = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", example = "9606", required = true)
                                                            @PathVariable String species,

                                                            @Parameter(description = "Only get pathways", example = "false")
                                                            @RequestParam(required = false, defaultValue = "false") Boolean pathwaysOnly,

                                                            @Parameter(description = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> token with the results to be overlaid on top of the given pathways overview")
                                                            @RequestParam(value = "token", required = false) String token,

                                                            @Parameter(description = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> resource for which the results will be overlaid on top of the given pathways overview")
                                                            @RequestParam(value = "resource", required = false, defaultValue = "TOTAL") String resource,

                                                            @Parameter(name = "interactors", description = "Include interactors", example = "false")
                                                            @RequestParam(required = false, defaultValue = "false") Boolean interactors,

                                                            HttpServletResponse response) {
        Collection<PathwayBrowserNode> pathwayBrowserNodes = eventHierarchyService.getEventHierarchy(species, pathwaysOnly);
        if (pathwayBrowserNodes == null || pathwayBrowserNodes.isEmpty())
            throw new NotFoundException("No event hierarchy found for given species: " + species);
        response.setHeader("Content-Disposition", "inline; swaggerDownload=\"attachment\"; filename=\"" + species + ".json\"");
        infoLogger.info("Request for full event hierarchy");

        if (token != null) {
            AnalysisStoredResult result = tokenUtils.getFromToken(token);
            Map<String, PathwayNodeData> stIdToNodeData = result.getPathways().stream().collect(Collectors.toMap(PathwayNodeSummary::getStId, PathwayNodeSummary::getData));
            pathwayBrowserNodes = pathwayBrowserNodes.stream()
                    .map(AnalysedPathwayBrowserNode::new)
                    .peek(node -> node.initAnalysis(stIdToNodeData, resource, interactors))
                    .collect(Collectors.toList());
        }
        return pathwayBrowserNodes;
    }
}
