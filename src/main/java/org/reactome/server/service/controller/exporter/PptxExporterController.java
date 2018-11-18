package org.reactome.server.service.controller.exporter;

import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.service.exception.DiagramExporterException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.manager.ExportManager;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
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
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@Api(tags = "exporter", description = "Reactome Data: Format Exporter")
@RequestMapping("/exporter")
public class PptxExporterController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    public static final String PPT_FILE_EXTENSION = ".pptx";

    @Autowired
    private DatabaseObjectService databaseObjectService;

    @Autowired
    private ExportManager exportManager;

    @ApiIgnore
    @ApiOperation(value = "Exports a given pathway diagram to PowerPoint")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Stable Identifier does not match with any of the available diagrams."),
            @ApiResponse(code = 500, message = "Could not deserialize diagram file."),
            @ApiResponse(code = 503, message = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/diagram/{identifier}" + PPT_FILE_EXTENSION, method = RequestMethod.GET)
    public synchronized void diagramPPTX(@ApiParam(value = "Stable Identifier", required = true, defaultValue = "R-HSA-177929")
                                        @PathVariable String identifier,
                                         @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                                        @RequestParam(value = "profile", defaultValue = "Modern", required = false) String colorProfile,
                                         @ApiParam(value = "Flag element(s) in the diagram. CSV line.")
                                        @RequestParam(value = "flg", required = false) List<Long> flg,
                                         @ApiParam(value = "Highlight element(s) selection in the diagram. CSV line.")
                                        @RequestParam(value = "sel", required = false) List<Long> sel,
                                        HttpServletResponse response) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramProfileException, IOException {

        Event event = getEvent(identifier);
        // IMPORTANT: Downloading the file on Swagger does not work - https://github.com/swagger-api/swagger-ui/issues/2132
        // for this reason we are keeping this method as APIIgnore
        infoLogger.info("Exporting the Diagram {} to PPTX for color profile {}", event.getStId(), colorProfile);

        Decorator decorator = new Decorator(flg, sel);
        File pptx = exportManager.getPPTX(event.getStId(), colorProfile, decorator, response);

        // when returning a FileSystemResource using Spring, then the file won't be deleted because it still has the
        // reference to the file and then we cannot delete. Writing the file directly in the response allows us to
        // delete only the temporary file.
        OutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(pptx);
        IOUtils.copy(in, out);
        out.flush();
        out.close();
        in.close();

        // deleting the file in case it is decorated.
        if (decorator.isDecorated() && !pptx.delete()) {
            infoLogger.error("Could not delete the temporary file {}", pptx.getPath());
        }
    }

    @ApiIgnore
    @ApiOperation(value = "Exports a given reaction to PowerPoint")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier not found"),
            @ApiResponse(code = 422, message = "Identifier does not correspond to a pathway or reaction"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/reaction/{identifier}.pptx", method = RequestMethod.GET)
    public synchronized void reactionPPTX(@ApiParam(value = "DbId or StId of the requested pathway or reaction", required = true, defaultValue = "R-HSA-5205682")
                                         @PathVariable String identifier,
                                          @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                                         @RequestParam(value = "profile", defaultValue = "Modern", required = false) String colorProfile,
                                          @ApiParam(value = "Flag element(s) in the diagram. CSV line.")
                                         @RequestParam(value = "flg", required = false) List<Long> flg,
                                          @ApiParam(value = "Highlight element(s) selection in the diagram. CSV line.")
                                         @RequestParam(value = "sel", required = false) List<Long> sel,
                                         HttpServletResponse response) throws Exception {
        // IMPORTANT: Downloading the file on Swagger does not work - https://github.com/swagger-api/swagger-ui/issues/2132
        // for this reason we are keeping this method as APIIgnore
        infoLogger.info("Exporting the Diagram {} to PPTX for color profile {}", identifier, colorProfile);

        ReactionLikeEvent rle = getReactionLikeEvent(identifier);
        Decorator decorator = new Decorator(flg, sel);
        File pptx = exportManager.getPPTX(rle, colorProfile, decorator, response);

        // when returning a FileSystemResource using Spring, then the file won't be deleted because it still has the
        // reference to the file and then we cannot delete. Writing the file directly in the response allows us to
        // delete only the temporary file.
        OutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(pptx);
        IOUtils.copy(in, out);
        out.flush();
        out.close();
        in.close();

        // deleting the file in case it is decorated.
        if (decorator.isDecorated() && !pptx.delete()) {
            infoLogger.error("Could not delete the temporary file {}", pptx.getPath());
        }
    }

    private Event getEvent(String id){
        Event event;
        try {
            event = databaseObjectService.findById(id);
        } catch (ClassCastException ex) {
            throw new DiagramExporterException(String.format("The identifier '%s' does not correspond to a 'Event'", id));
        }
        if (event == null) throw new NotFoundException(String.format("Identifier '%s' not found", id));
        return event;
    }

    private ReactionLikeEvent getReactionLikeEvent(String id){
        ReactionLikeEvent rle;
        try {
            rle = databaseObjectService.findById(id);
        } catch (ClassCastException e){
            throw new DiagramExporterException(String.format("The identifier '%s' does not correspond to a 'ReactionLikeEvent'", id));
        }
        if (rle == null) throw new NotFoundException(String.format("Identifier '%s' not found", id));
        return rle;
    }

}
