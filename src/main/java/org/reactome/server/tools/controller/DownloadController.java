package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FilenameUtils;
import org.reactome.server.tools.manager.DownloadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@RestController
@Api(value = "/download", description = "Download Molecule interactors")
@RequestMapping("/download")
public class DownloadController {

    @Autowired
    public DownloadManager downloadManager;

    //@ApiIgnore // Shall we change the url just for security ?
    @ApiOperation(value = "Wrap interactor content into a TSV file")
    @RequestMapping(value = "/{filename}", method = RequestMethod.POST, produces = "text/csv", headers = "Content-Type=application/x-www-form-urlencoded")
    @ResponseBody
    public FileSystemResource downloadFile(@ApiParam(value = "Interactor accessions (or identifiers)", required = true)
                                                         @RequestParam String content,
                                                         @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                         @PathVariable String filename) throws IOException {

        String ext = FilenameUtils.getExtension(filename);
        File file = File.createTempFile(filename, ext);
        FileWriter fw = new FileWriter(file);

        fw.write(content);

        fw.flush();
        fw.close();

        return new FileSystemResource(file);
    }
}
