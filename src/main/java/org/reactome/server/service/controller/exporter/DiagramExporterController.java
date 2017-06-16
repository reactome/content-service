package org.reactome.server.service.controller.exporter;

import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.reactome.server.service.manager.DiagramExportManager;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.model.Decorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings({"unused", "ConstantConditions"})
@RestController
@Api(tags = "exporter", description = "Reactome Data: Format Exporter")
@RequestMapping("/exporter")
public class DiagramExporterController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    public static final String PPT_FILE_EXTENSION = ".pptx";
    public static final String SBML_FILE_EXTENSION = ".xml";

    private DiagramExportManager manager;

    @ApiIgnore
    @ApiOperation(value = "Export given diagram to PowerPoint")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Stable Identifier does not match with any of the available diagrams."),
            @ApiResponse(code = 500, message = "Could not deserialize diagram file."),
            @ApiResponse(code = 503, message = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/diagram/{stId}" + PPT_FILE_EXTENSION, method = RequestMethod.GET)
    public void toPPTX(@ApiParam(value = "Stable Identifier", required = true, defaultValue = "R-HSA-177929")
                                     @PathVariable String stId,
                                     @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                                     @RequestParam(value = "profile", defaultValue = "Modern", required = false) String colorProfile,
                                     @ApiParam(value = "Flag element(s) in the diagram. CSV line.")
                                     @RequestParam(value = "flg", required = false) List<Long> flg,
                                     @ApiParam(value = "Highlight element(s) selection in the diagram. CSV line.")
                                     @RequestParam(value = "sel", required = false) List<Long> sel,
                                     HttpServletResponse response) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramProfileException, IOException {

        // IMPORTANT: Downloading the file on Swagger does not work - https://github.com/swagger-api/swagger-ui/issues/2132
        // for this reason we are keeping this method as APIIgnore
        infoLogger.info("Exporting the Diagram {} to PPTX for color profile {}", stId, colorProfile);

        Decorator decorator = new Decorator(flg, sel);
        File pptx =  manager.getPPTX(stId, colorProfile, decorator, response);

        // when returning a FileSystemResource using Spring, then the file won't be deleted because it still has the
        // reference to the file and then we cannot delete. Writing the file directly in the response allows us to
        // delete only the temporary file.
        OutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(pptx);
        IOUtils.copy(in,out);
        out.flush();
        out.close();
        in.close();

        // deleting the file in case it is decorated.
        if(decorator.isDecorated()) {
            if(!pptx.delete()) {
                infoLogger.error("Could not delete the temporary file {}", pptx.getPath());
            }
        }
    }

    @ApiOperation(value = "Export given Pathway to SBML")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier not found"),
            @ApiResponse(code = 422, message = "Identifier does not correspond to a pathway"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/sbml/{id}" + SBML_FILE_EXTENSION, method = RequestMethod.GET)
    public synchronized void toSBML(@ApiParam(value = "DbId or StId of the requested database object", required = true, defaultValue = "R-HSA-68616")
                                    @PathVariable String id,
                                    HttpServletResponse response) throws Exception {
        File sbml = manager.getSBML(id, response);

        // when returning a FileSystemResource using Spring, then the file won't be deleted because it still has the
        // reference to the file and then we cannot delete. Writing the file directly in the response allows us to
        // delete only the temporary file.
        OutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(sbml);
        IOUtils.copy(in,out);
        out.flush();
        out.close();
        in.close();
    }

    @Autowired
    public void setManager(DiagramExportManager manager) {
        this.manager = manager;
    }
}
