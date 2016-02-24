package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.tools.manager.DownloadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@RestController
@Api(value = "/interactors", description = "Download Molecule interactors")
@RequestMapping("/interactors/psicquic")
public class DownloadPsicquicInteractionsController {

    @Autowired
    public DownloadManager downloadManager;

    @ApiOperation(value = "Downloads PSICQUIC interactions for a given accession",
            notes = "The results are filtered by the selected resource. The filename is the one to be suggested in the download window.")
    @RequestMapping(value = "/{resource}/{acc}/download/{filename}.csv", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody
    public FileSystemResource downloadPsicquicMoleculeInteractions(@ApiParam(name = "acc", value = "the accession", required = true)
                                                                   @PathVariable String acc,
                                                                   @ApiParam(name = "resource", value = "the psicquic resource", required = true)
                                                                   @PathVariable String resource,
                                                                   @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                                   @PathVariable String filename) throws IOException {

        return downloadManager.downloadPsicquicMoleculeInteractions(acc, resource, filename);
    }

    //@ApiIgnore // Shall we change the url just for security ?
    @ApiOperation(value = "Downloads PSICQUIC interactions for a given accessions",
            notes = "The results are filtered by the selected resource. The filename is the one to be suggested in the download window.")
    @RequestMapping(value = "/{resource}/download/{filename}.csv", method = RequestMethod.POST, produces = "text/csv", headers = "Content-Type=application/x-www-form-urlencoded")
    @ResponseBody
    public FileSystemResource downloadPsicquicMoleculesInteractions(@ApiParam(value = "Interactor accessions (or identifiers)", required = true)
                                                                    @RequestParam String molecules,
                                                                    @ApiParam(name = "resource", value = "the psicquic resource", required = true)
                                                                    @PathVariable String resource,
                                                                    @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                                    @PathVariable String filename) throws IOException {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(molecules.split("\\s*,\\s*")));

        return downloadManager.downloadPsicquicMoleculesInteractions(accs, resource, filename);
    }

}
