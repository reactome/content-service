package org.reactome.server.service.controller.exporter;

import io.swagger.annotations.*;
import org.reactome.server.service.manager.DiagramExportManager;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.exception.LicenseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@RestController
@Api(tags = "exporter", description = "Diagram Exporters")
@RequestMapping("/exporter")
public class DiagramExporterController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    public static final String PPT_FILE_EXTENSION = ".pptx";

    @Autowired
    private DiagramExportManager manager;

    @ApiIgnore
    @ApiOperation(value = "Export given diagram to PowerPoint")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Stable Identifier does not match with any of the available diagrams."),
            @ApiResponse(code = 500, message = "Could not deserialize diagram file."),
            @ApiResponse(code = 503, message = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/diagram/{stId}" + PPT_FILE_EXTENSION, method = RequestMethod.GET)
    @ResponseBody
    public FileSystemResource toPPTX(@ApiParam(value = "Stable Identifier", required = true, defaultValue = "R-HSA-177929")
                                     @PathVariable String stId,
                                     @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                                     @RequestParam(value = "profile", defaultValue = "Modern", required = false) String colorProfile,
                                     HttpServletResponse response) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramProfileException, LicenseException {
        // IMPORTANT: Downloading the file on Swagger does not work - https://github.com/swagger-api/swagger-ui/issues/2132
        infoLogger.info("Exporting the Diagram {} to PPTX for color profile {}", stId, colorProfile);

        return new FileSystemResource(manager.toPPTX(stId, colorProfile, response));
    }
}
