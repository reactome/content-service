package org.reactome.server.service.controller.interactors;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings("unused")
//@Api(value = "/download", description = "Download Molecule interactors")
@RequestMapping("/download")
@RestController
public class DownloadController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @ApiIgnore
    @ApiOperation(value = "Wrap interactor content into a CSV file")
    @RequestMapping(value = "/{filename}", method = RequestMethod.POST, consumes = "text/plain", produces = "text/csv", headers = "Content-Type=application/x-www-form-urlencoded")
    @ResponseBody
    public FileSystemResource downloadFile(@ApiParam(value = "Interactor accessions (or identifiers)", required = true)
                                                         @RequestParam String content,
                                                         @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result.csv")
                                                         @PathVariable String filename) throws IOException {
        infoLogger.info("Download interactor file {} has submitted", filename);
        String ext = FilenameUtils.getExtension(filename);
        File file = File.createTempFile(filename, ext);
        FileWriter fw = new FileWriter(file);

        for (String line : content.split("#NL#")) {
            fw.write(line + "\n");
        }

        fw.flush();
        fw.close();

        return new FileSystemResource(file);
    }
}
