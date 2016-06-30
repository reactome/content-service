package org.reactome.server.service.controller.interactors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.service.manager.InteractionManager;
import org.reactome.server.service.model.interactors.Interactors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@RestController
@Api(tags = "interactors", description = "Molecule interactors")
@RequestMapping("/interactors/static")
public class StaticInteractionsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private static final String STATIC_RESOURCE_NAME = "static";

    @Autowired
    private InteractionManager interactions;


    @ApiOperation(value = "Retrieve a summary of a given accession", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecule/{acc}/summary", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinSummaryByAcc(@ApiParam(value = "Accession", required = true) @PathVariable String acc) {
        return interactions.getStaticProteinsSummary(Collections.singletonList(acc), STATIC_RESOURCE_NAME);
    }

    @ApiOperation(value = "Retrieve a detailed interaction information of a given accession", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecule/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinDetailsByAcc(@ApiParam(value = "Interactor accession (or identifier)", required = true) @PathVariable String acc,
                                              @ApiParam(value = "For paginating the results") @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                              @ApiParam(value = "Number of results to be retrieved") @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize) {
        return interactions.getStaticProteinDetails(Collections.singletonList(acc), STATIC_RESOURCE_NAME, page, pageSize);
    }

    @ApiOperation(value = "Retrieve a summary of a given accession list", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecules/summary", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsSummaryByAccs(@ApiParam(value = "Interactor accessions (or identifiers)", required = true) @RequestBody String proteins) {
        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));
        return interactions.getStaticProteinsSummary(accs, STATIC_RESOURCE_NAME);

    }

    @ApiOperation(value = "Retrieve a detailed interaction information of a given accession", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecules/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsDetailsByAccs(@ApiParam(value = "For paginating the results") @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                                @ApiParam(value = "Number of results to be retrieved") @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize,
                                                @ApiParam(value = "Interactor accessions (or identifiers)", required = true) @RequestBody String proteins) {
        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));
        return interactions.getStaticProteinDetails(accs, STATIC_RESOURCE_NAME, page, pageSize);
    }
}
