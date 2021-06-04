package org.reactome.server.service.controller.interactors;

import io.swagger.annotations.*;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.service.exception.ErrorInfo;
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
@Api(tags = {"interactors"})
@RequestMapping("/interactors/static")
@RestController
public class StaticInteractionsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private static final String STATIC_RESOURCE_NAME = "static";

    @Autowired
    private InteractionManager interactions;

    @ApiOperation(value = "Retrieve a summary of a given accession", response = Interactors.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Could not find the Interactor Resource", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/molecule/{acc}/summary", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinSummaryByAcc(@ApiParam(value = "Accession", required = true, defaultValue = "Q13501") @PathVariable String acc) {
        infoLogger.info("Static interaction summary query for accession {}", acc);
        Interactors interactors = interactions.getStaticProteinsSummary(Collections.singletonList(acc), STATIC_RESOURCE_NAME);
        if (interactors == null || interactors.getEntities().isEmpty()) {
            throw new NotFoundException("No interactors found for accession: " + acc);
        }
        return interactors;
    }

    @ApiOperation(value = "Retrieve a detailed interaction information of a given accession", response = Interactors.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Could not find the Interactor Resource", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/molecule/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    //todo: Error mapping GraphModel to instance of Interaction and wait for a new library in graph core, the org.neo4j.ogm is going to deprecated
    public Interactors getProteinDetailsByAcc(@ApiParam(value = "Interactor accession (or identifier)", required = true, defaultValue = "Q13501") @PathVariable String acc,
                                              @ApiParam(value = "For paginating the results") @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                              @ApiParam(value = "Number of results to be retrieved") @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize) {
        infoLogger.info("Static interaction details query for accession {}", acc);
        Interactors interactors = interactions.getStaticProteinDetails(Collections.singletonList(acc), STATIC_RESOURCE_NAME, page, pageSize);
        if (interactors == null || interactors.getEntities().isEmpty() || interactors.hasInteractorsInEntities() ) {
            throw new NotFoundException("No interactors found for accession: " + acc);
        }
        return interactors;
    }

    @ApiOperation(value = "Retrieve a summary of a given accession list", response = Interactors.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Could not find the Interactor Resource", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/molecules/summary", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsSummaryByAccs(@ApiParam(value = "Interactor accessions (or identifiers)", required = true, defaultValue = "O95631") @RequestBody String proteins) {
        infoLogger.info("Static interaction summary query for accessions by POST");
        // Split param and put into a Set to avoid duplicates
        Collection<String> accs = new HashSet<>();
        for (String id : proteins.split(",|;|\\n|\\t")) {
            accs.add(id.trim());
        }
        Interactors interactors =  interactions.getStaticProteinsSummary(accs, STATIC_RESOURCE_NAME);
        if (interactors == null || interactors.getEntities().isEmpty()) {
            throw new NotFoundException("No interactors found for accession: " + proteins);
        }
        return interactors;
    }

    @ApiOperation(value = "Retrieve a detailed interaction information of a given accession", response = Interactors.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Could not find the Interactor Resource", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/molecules/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    //todo: Error mapping GraphModel to instance of Interaction and wait for a new library in graph core, the org.neo4j.ogm is going to deprecated
    public Interactors getProteinsDetailsByAccs(@ApiParam(value = "For paginating the results") @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                                @ApiParam(value = "Number of results to be retrieved") @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize,
                                                @ApiParam(value = "Interactor accessions (or identifiers)", required = true, defaultValue = "O95631") @RequestBody String proteins) {
        infoLogger.info("Static interaction details query for accessions by POST");
        // Split param and put into a Set to avoid duplicates
        Collection<String> accs = new HashSet<>();
        for (String id : proteins.split(",|;|\\n|\\t")) {
            accs.add(id.trim());
        }
        Interactors interactors = interactions.getStaticProteinDetails(accs, STATIC_RESOURCE_NAME, page, pageSize);
        if (interactors == null || interactors.getEntities().isEmpty() || interactors.hasInteractorsInEntities()) {
            throw new NotFoundException("No interactors found for accession: " + proteins);
        }
        return interactors;
    }

    @ApiOperation(value = "Retrieve a list of lower level pathways where the interacting molecules can be found", response = Interactors.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Could not find the Interactor Resource", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/molecule/{acc}/pathways", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getLowerLevelPathways( @ApiParam(value = "Accession", required = true, defaultValue = "Q9BXM7-1")
                                                     @PathVariable String acc,
                                                      @ApiParam(value = "The species name for which the pathways are requested  (e.g. 'Homo sapiens')", defaultValue = "Homo sapiens")
                                                     @RequestParam(required = false) String species,
                                                      @ApiParam(value = "Specifies whether the pathways has to have an associated diagram or not", defaultValue = "false")
                                                     @RequestParam(required = false, defaultValue = "false") Boolean onlyDiagrammed){
        Collection<Pathway> lowerLevelPathways = interactions.getLowerLevelPathways(acc, species, onlyDiagrammed);
        if (lowerLevelPathways.isEmpty()) {
            throw new NotFoundException("No pathways found for molecule: " + acc);
        }
        return lowerLevelPathways;
    }
}
