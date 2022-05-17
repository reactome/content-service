package org.reactome.server.service.controller.graph;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.ReferenceEntityService;
import org.reactome.server.service.controller.graph.util.CrossReferenceResult;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gk.model.ReactomeJavaConstants.identifier;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "references", description = "Reactome xRefs: ReferenceEntity queries")
@RequestMapping("/references")
public class ReferenceEntityController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private ReferenceEntityService referenceEntityService;
    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    @Operation(
            summary = "All ReferenceEntities for a given identifier",
            description = "Retrieves a list containing all the reference entities for a given identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any reference entity"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/mapping/{identifier}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getReferenceEntitiesFor(@Parameter(description = "Identifier for a given entity", example = "15377", required = true) @PathVariable String identifier) {
        Collection<ReferenceEntity> referenceEntities = referenceEntityService.getReferenceEntitiesFor(identifier);
        if (referenceEntities == null || referenceEntities.isEmpty())
            throw new NotFoundException("Identifier: " + identifier + " has not been found as a cross reference in any of the annotated entities");
        infoLogger.info("Request all ReferenceEntities for the identifier: {}", identifier);
        return referenceEntities;
    }

    @Operation(
            summary = "All cross references and physical entities associated with a given identifier",
            description = "Retrieves a list containing cross references and physical entities associated with the matching reference entities identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any reference entity"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/mapping/{identifier}/xrefs", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<CrossReferenceResult> getCrossReferencesFor(
            @Parameter(description = "Identifier for a given entity", example = "P36897", required = true) @PathVariable String identifier,
            @Parameter(description = "Database filter to apply to results") @RequestParam(required = false) String dbFilter
    ) {
        Collection<CrossReferenceResult> crossReferenceResults = getCrossReferenceResults(identifier, dbFilter);
        if (crossReferenceResults == null || crossReferenceResults.isEmpty() || crossReferenceResults.stream().allMatch(CrossReferenceResult::isEmpty))
            throw new NotFoundException("Identifier: " + identifier + " has not been found as a cross reference in any of the annotated entities");

        return crossReferenceResults;
    }


    @Operation(
            summary = "All cross references and physical entities associated with a list of given identifiers",
            description = "Retrieves a paginated list containing cross references and physical entities associated with the matching reference entities identifiers"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any reference entity"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/mapping/xrefs", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    @JsonInclude
    public Page<Map<String, Collection<CrossReferenceResult>>> getCrossReferencesForList(
            @Parameter(description = "Database filter to apply to results") @RequestParam(required = false) String dbFilter,
            @Parameter(description = "Page to process, starting from 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size to process, an element being one of the submitted identifiers") @RequestParam(defaultValue = "100") int pageSize,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "A comma separated list of identifiers",
                    required = true,
                    content = @Content(examples = @ExampleObject("P36897, Q5S007"))
            )
            @RequestBody String post
    ) {
        String[] identifiers = post.split(",|;|\\n|\\t");
        PageImpl<Map<String, Collection<CrossReferenceResult>>> maps = new PageImpl<>(
                Arrays.stream(identifiers)
                        .skip((long) page * pageSize)
                        .limit(pageSize)
                        .parallel()
                        .map(String::trim)
                        .map(s -> Map.of(s, getCrossReferenceResults(s, dbFilter)))
                        .collect(Collectors.toList()),
                PageRequest.of(page, pageSize), identifiers.length);
        return maps;
    }

    private Collection<CrossReferenceResult> getCrossReferenceResults(String identifier, String dbFilter) {
        Collection<CrossReferenceResult> crossReferenceResults;
        try {
            crossReferenceResults = advancedDatabaseObjectService.getCustomQueryResults(
                    CrossReferenceResult.class,
                    "MATCH (n{identifier:\"" + identifier + "\"})<-[:crossReference]-(re:ReferenceEntity)-[:crossReference]->(xf) " +
                            (dbFilter == null ? "" : "WHERE xf.databaseName=\"" + dbFilter + "\" ") +
                            "OPTIONAL MATCH (re)<-[:referenceEntity]-(pe:PhysicalEntity) " +
                            "RETURN re.identifier as reference, [ x in collect(distinct xf) | {identifier:x.identifier, databaseName:x.databaseName, url:x.url}] as crossReferences, collect(distinct pe.stId) as physicalEntities"
            );
        } catch (CustomQueryException e) {
            infoLogger.error("Cross reference query failed for " + identifier + " with filter: " + dbFilter);
            return List.of();
        }
        return crossReferenceResults.isEmpty() ? List.of(CrossReferenceResult.EMPTY) : crossReferenceResults;
    }

}
