package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.tools.interactors.model.PsicquicResource;
import org.reactome.server.tools.manager.DownloadManager;
import org.reactome.server.tools.manager.InteractionManager;
import org.reactome.server.tools.model.interactors.Interactors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@RestController
@Api(tags ="interactors", description = "Molecule interactors")
@RequestMapping("/interactors/psicquic")
public class PsicquicInteractionsController {

    @Autowired
    private InteractionManager interactions;

    @Autowired
    public DownloadManager downloadManager;

    @ApiOperation(value = "Retrieve a list of all Psicquic Registries services", response = PsicquicResource.class, produces = "application/json")
    @RequestMapping(value = "/resources", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<PsicquicResource> getResources()  {
        return interactions.getPsicquicResources();
    }

    @ApiOperation(value = "Retrieve a detailed clustered interaction, sorted by score, of a given accession by resource.", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/{resource}/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinDetailsByResource(@ApiParam(value="PSICQUIC Resource",required = true) @PathVariable String resource,
                                                   @ApiParam(value="Single Accession",required = true) @PathVariable String acc)  {

        return interactions.getPsicquicProteinsDetails(Collections.singletonList(acc), resource);
    }

    @ApiOperation(value = "Retrieve a detailed clustered interaction, sorted by score, of a given accession(s) by resource.", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecules/{resource}/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsDetailsByResource(@ApiParam(value="PSICQUIC Resource",required = true) @PathVariable String resource,
                                                    @ApiParam(value="Accessions",required = true) @RequestBody String proteins) {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));

        return interactions.getPsicquicProteinsDetails(accs, resource);
    }

    @ApiOperation(value = "Retrieve a summary of a given accession by resource", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecule/{resource}/{acc}/summary", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinSummaryByResource(@ApiParam(value="PSICQUIC Resource",required = true) @PathVariable String resource,
                                                   @ApiParam(value="Single Accession",required = true) @PathVariable String acc)  {

        return interactions.getPsicquicProteinsSummary(Collections.singletonList(acc), resource);
    }

    @ApiOperation(value = "Retrieve a summary of a given accession list by resource.", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecules/{resource}/summary", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsSummaryByResource(@ApiParam(value="PSICQUIC Resource",required = true) @PathVariable String resource,
                                                    @ApiParam(value="Accessions",required = true) @RequestBody String proteins) {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));

        return interactions.getPsicquicProteinsSummary(accs, resource);

    }
//
//    @ApiOperation(value = "Downloads PSICQUIC interactions for a given accession",
//            notes = "The results are filtered by the selected resource. The filename is the one to be suggested in the download window.")
//    @RequestMapping(value = "/molecule/{resource}/{acc}/download/{filename}.csv", method = RequestMethod.GET, produces = "text/csv")
//    @ResponseBody
//    public FileSystemResource downloadPsicquicMoleculeInteractions(@ApiParam(name = "acc", value = "the accession", required = true)
//                                                                   @PathVariable String acc,
//                                                                   @ApiParam(name = "resource", value = "the psicquic resource", required = true)
//                                                                   @PathVariable String resource,
//                                                                   @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
//                                                                   @PathVariable String filename) throws IOException {
//
//        return downloadManager.downloadPsicquicMoleculeInteractions(acc, resource, filename);
//    }
//
//    //@ApiIgnore // Shall we change the url just for security ?
//    @ApiOperation(value = "Downloads PSICQUIC interactions for a given accessions",
//            notes = "The results are filtered by the selected resource. The filename is the one to be suggested in the download window.")
//    @RequestMapping(value = "/molecules/{resource}/download/{filename}.csv", method = RequestMethod.POST, produces = "text/csv", headers = "Content-Type=application/x-www-form-urlencoded")
//    @ResponseBody
//    public FileSystemResource downloadPsicquicMoleculesInteractions(@ApiParam(value = "Interactor accessions (or identifiers)", required = true)
//                                                                    @RequestParam String molecules,
//                                                                    @ApiParam(name = "resource", value = "the psicquic resource", required = true)
//                                                                    @PathVariable String resource,
//                                                                    @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
//                                                                    @PathVariable String filename) throws IOException {
//
//        /** Split param and put into a Set to avoid duplicates **/
//        Set<String> accs = new HashSet<>(Arrays.asList(molecules.split("\\s*,\\s*")));
//
//        return downloadManager.downloadPsicquicMoleculesInteractions(accs, resource, filename);
//    }
}
