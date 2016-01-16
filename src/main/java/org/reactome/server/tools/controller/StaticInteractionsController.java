package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.reactome.server.tools.manager.InteractionManager;
import org.reactome.server.tools.model.interactions.InteractionResult;
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
@Api(value = "/interactors", description = "Molecule interactors")
@RequestMapping("/interactors/static")
public class StaticInteractionsController {

    private static final String STATIC_RESOURCE_NAME = "IntAct";

    @Autowired
    private InteractionManager interactions;

    @ApiOperation(value = "Retrieve a summary of a given accession by resource", response = InteractionResult.class)
    @RequestMapping(value = "/protein/{acc}/summary", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InteractionResult getProteinSummaryByResourceAndAcc(@PathVariable String acc) {
        return interactions.getProteinsSummary(Collections.singletonList(acc), STATIC_RESOURCE_NAME);
    }

    @ApiOperation(value = "Retrieve a detailed interaction information of a given accession by resource", response = InteractionResult.class)
    @RequestMapping(value = "/protein/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InteractionResult getProteinDetailsByResourceAndAcc(@PathVariable String acc,
                                                               @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                                               @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize) {
        return interactions.getProteinDetails(Collections.singletonList(acc), STATIC_RESOURCE_NAME, page, pageSize);
    }

    @ApiOperation(value = "Retrieve a summary of a given accession list by resource", response = InteractionResult.class)
    @RequestMapping(value = "/proteins/summary", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public InteractionResult getProteinsSummaryByResource(@RequestBody String proteins) {
        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));
        return interactions.getProteinsSummary(accs, STATIC_RESOURCE_NAME);

    }

    @ApiOperation(value = "Retrieve a detailed interaction information of a given accession by resource", response = InteractionResult.class)
    @RequestMapping(value = "/proteins/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public InteractionResult getProteinsDetailsByResource(@RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                                          @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize,
                                                          @RequestBody String proteins) {
        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));
        return interactions.getProteinDetails(accs, STATIC_RESOURCE_NAME, page, pageSize);
    }
}
