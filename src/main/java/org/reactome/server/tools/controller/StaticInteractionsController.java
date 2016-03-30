package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.tools.manager.DownloadManager;
import org.reactome.server.tools.manager.InteractionManager;
import org.reactome.server.tools.model.interactors.Interactors;
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

    private static final String STATIC_RESOURCE_NAME = "static";

    @Autowired
    private InteractionManager interactions;

    @Autowired
    public DownloadManager downloadManager;

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

//    @ApiOperation(value = "Downloads interactions for a given accession",
//            notes = "Download interactions from IntAct (static). The filename is the one to be suggested in the download window.")
//    @RequestMapping(value = "/molecule/download/{acc}/{filename}.csv", method = RequestMethod.GET, produces = "text/csv")
//    @ResponseBody
//    public FileSystemResource downloadStaticInteraction(@ApiParam(name = "acc", value = "the accession", required = true)
//                                                        @PathVariable String acc,
//                                                        @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
//                                                        @PathVariable String filename) throws IOException {
//
//
//        return downloadManager.downloadStaticMoleculeInteractions(acc, filename);
//    }
//
//    //@ApiIgnore // Shall we change the url just for security ?
//    @ApiOperation(value = "Downloads interactions for a given accessions",
//            notes = "Download interactions from IntAct (static). The filename is the one to be suggested in the download window.")
//    @RequestMapping(value = "/molecules/download/{filename}.csv", method = RequestMethod.POST, produces = "text/csv", headers = "Content-Type=application/x-www-form-urlencoded")
//    @ResponseBody
//    public FileSystemResource downloadStaticInteractions(@ApiParam(value = "Interactor accessions (or identifiers)", required = true)
//                                                         @RequestParam(required = true) String molecules,
//                                                         @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
//                                                         @PathVariable String filename) throws IOException {
//
//        /** Split param and put into a Set to avoid duplicates **/
//        Set<String> accs = new HashSet<>(Arrays.asList(molecules.split("\\s*,\\s*")));
//
//        return downloadManager.downloadStaticMoleculesInteractions(accs, filename);
//    }
}
