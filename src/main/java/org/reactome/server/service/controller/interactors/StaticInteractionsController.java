package org.reactome.server.service.controller.interactors;

import io.swagger.annotations.*;
import org.reactome.server.service.exception.ErrorInfo;
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
@Api(tags = "interactors", description = "Molecule interactors")
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
        return interactions.getStaticProteinsSummary(Collections.singletonList(acc), STATIC_RESOURCE_NAME);
    }

    @ApiOperation(value = "Retrieve a detailed interaction information of a given accession", response = Interactors.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Could not find the Interactor Resource", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/molecule/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinDetailsByAcc(@ApiParam(value = "Interactor accession (or identifier)", required = true, defaultValue = "Q13501") @PathVariable String acc,
                                              @ApiParam(value = "For paginating the results") @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                              @ApiParam(value = "Number of results to be retrieved") @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize) {
        infoLogger.info("Static interaction details query for accession {}", acc);
        return interactions.getStaticProteinDetails(Collections.singletonList(acc), STATIC_RESOURCE_NAME, page, pageSize);
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

        return interactions.getStaticProteinsSummary(accs, STATIC_RESOURCE_NAME);

    }

    @ApiOperation(value = "Retrieve a detailed interaction information of a given accession", response = Interactors.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Could not find the Interactor Resource", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/molecules/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsDetailsByAccs(@ApiParam(value = "For paginating the results") @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                                @ApiParam(value = "Number of results to be retrieved") @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize,
                                                @ApiParam(value = "Interactor accessions (or identifiers)", required = true, defaultValue = "O95631") @RequestBody String proteins) {
        infoLogger.info("Static interaction details query for accessions by POST");
        // Split param and put into a Set to avoid duplicates
        Collection<String> accs = new HashSet<>();
        for (String id : proteins.split(",|;|\\n|\\t")) {
            accs.add(id.trim());
        }

        return interactions.getStaticProteinDetails(accs, STATIC_RESOURCE_NAME, page, pageSize);
    }
}
