package org.reactome.server.service.controller.interactors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.manager.InteractionManager;
import org.reactome.server.service.model.interactors.Interactors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;


/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("unused")
@Tag(name = "interactors", description = "Molecule interactors")
@RequestMapping("/interactors/static")
@RestController
public class StaticInteractionsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private static final String STATIC_RESOURCE_NAME = "static";

    @Autowired
    private InteractionManager interactions;

    @Operation(summary = "Retrieve a summary of a given accession")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Could not find the Interactor Resource"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/molecule/{acc}/summary", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinSummaryByAcc(@Parameter(description = "Accession", required = true, example = "Q13501") @PathVariable String acc) {
        infoLogger.info("Static interaction summary query for accession {}", acc);
        Interactors interactors = interactions.getStaticProteinsSummary(Collections.singletonList(acc), STATIC_RESOURCE_NAME);
        if (interactors == null || interactors.getEntities().isEmpty()) {
            throw new NotFoundException("No interactors found for accession: " + acc);
        }
        return interactors;
    }

    @Operation(summary = "Retrieve a detailed interaction information of a given accession")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Could not find the Interactor Resource"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/molecule/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    //todo: Error mapping GraphModel to instance of Interaction and wait for a new library in graph core, the org.neo4j.ogm is going to deprecated
    public Interactors getProteinDetailsByAcc(@Parameter(description = "Interactor accession (or identifier)", required = true, example = "Q13501") @PathVariable String acc,
                                              @Parameter(description = "For paginating the results") @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                              @Parameter(description = "Number of results to be retrieved") @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize) {
        infoLogger.info("Static interaction details query for accession {}", acc);
        Interactors interactors = interactions.getStaticProteinDetails(Collections.singletonList(acc), STATIC_RESOURCE_NAME, page, pageSize);
        if (interactors == null || interactors.getEntities().isEmpty() || interactors.hasNoInteractorsInEntities()) {
            throw new NotFoundException("No interactors found for accession: " + acc);
        }
        return interactors;
    }

    @Operation(summary = "Retrieve a summary of a given accession list")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Could not find the Interactor Resource"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/molecules/summary", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsSummaryByAccs(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "<b>Interactors</b> Interactor accessions (or identifiers)",
                    required = true,
                    content = @Content(examples = @ExampleObject("O95631, P60484")))
            @RequestBody String proteins
    ) {
        infoLogger.info("Static interaction summary query for accessions by POST");
        // Split param and put into a Set to avoid duplicates
        Collection<String> accs = new HashSet<>();
        for (String id : proteins.split(",|;|\\n|\\t")) {
            accs.add(id.trim());
        }
        Interactors interactors = interactions.getStaticProteinsSummary(accs, STATIC_RESOURCE_NAME);
        if (interactors == null || interactors.getEntities().isEmpty()) {
            throw new NotFoundException("No interactors found for accession: " + proteins);
        }
        return interactors;
    }

    @Operation(summary = "Retrieve a detailed interaction information of a given accession")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Could not find the Interactor Resource"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/molecules/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    //todo: Error mapping GraphModel to instance of Interaction and wait for a new library in graph core, the org.neo4j.ogm is going to deprecated
    public Interactors getProteinsDetailsByAccs(@Parameter(description = "For paginating the results") @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                                @Parameter(description = "Number of results to be retrieved") @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize,
                                                @Parameter(description = "Interactor accessions (or identifiers)", required = true, example = "O95631")
                                                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                        description = "<b>Interactors</b> Interactor accessions (or identifiers)",
                                                        required = true,
                                                        content = @Content(examples = @ExampleObject("O95631, P60484")))
                                                @RequestBody String proteins) {
        infoLogger.info("Static interaction details query for accessions by POST");
        // Split param and put into a Set to avoid duplicates
        Collection<String> accs = new HashSet<>();
        for (String id : proteins.split(",|;|\\n|\\t")) {
            accs.add(id.trim());
        }
        Interactors interactors = interactions.getStaticProteinDetails(accs, STATIC_RESOURCE_NAME, page, pageSize);
        if (interactors == null || interactors.getEntities().isEmpty() || interactors.hasNoInteractorsInEntities()) {
            throw new NotFoundException("No interactors found for accession: " + proteins);
        }
        return interactors;
    }

    @Operation(summary = "Retrieve a list of lower level pathways where the interacting molecules can be found")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Could not find the Interactor Resource"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/molecule/{acc}/pathways", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getLowerLevelPathways(@Parameter(description = "Accession", required = true, example = "Q9BXM7-1")
                                                     @PathVariable String acc,
                                                     @Parameter(description = "The species name for which the pathways are requested  (e.g. 'Homo sapiens')", example = "Homo sapiens")
                                                     @RequestParam(required = false) String species,
                                                     @Parameter(description = "Specifies whether the pathways has to have an associated diagram or not", example = "false")
                                                     @RequestParam(required = false, defaultValue = "false") Boolean onlyDiagrammed) {
        Collection<Pathway> lowerLevelPathways = interactions.getLowerLevelPathways(acc, species, onlyDiagrammed);
        if (lowerLevelPathways.isEmpty()) {
            throw new NotFoundException("No pathways found for molecule: " + acc);
        }
        return lowerLevelPathways;
    }
}
