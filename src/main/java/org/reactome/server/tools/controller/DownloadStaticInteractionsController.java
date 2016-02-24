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
@RequestMapping("/interactors/static")
public class DownloadStaticInteractionsController {

    @Autowired
    public DownloadManager downloadManager;

    @ApiOperation(value = "Downloads interactions for a given accession",
            notes = "Download interactions from IntAct (static). The filename is the one to be suggested in the download window.")
    @RequestMapping(value = "/molecule/{acc}/download/{filename}.csv", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody
    public FileSystemResource downloadStaticInteraction(@ApiParam(name = "acc", value = "the accession", required = true)
                                                        @PathVariable String acc,
                                                        @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                        @PathVariable String filename) throws IOException {


        return downloadManager.downloadStaticMoleculeInteractions(acc, filename);
    }

    //@ApiIgnore // Shall we change the url just for security ?
    @ApiOperation(value = "Downloads interactions for a given accessions",
            notes = "Download interactions from IntAct (static). The filename is the one to be suggested in the download window.")
    @RequestMapping(value = "/molecules/download/{filename}.csv", method = RequestMethod.POST, produces = "text/csv", headers = "Content-Type=application/x-www-form-urlencoded")
    @ResponseBody
    public FileSystemResource downloadStaticInteractions(@ApiParam(value = "Interactor accessions (or identifiers)", required = true)
                                                         @RequestParam String molecules,
                                                         @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                         @PathVariable String filename) throws IOException {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(molecules.split("\\s*,\\s*")));

        return downloadManager.downloadStaticMoleculesInteractions(accs, filename);
    }
}
