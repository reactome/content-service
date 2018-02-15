package org.reactome.server.service.controller.exporter;

import io.swagger.annotations.*;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.IOUtils;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.result.DiagramResult;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DiagramService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.exception.DiagramExporterException;
import org.reactome.server.service.exception.MissingSBMLException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.exception.UnprocessableEntityException;
import org.reactome.server.service.manager.DiagramPPTXExportManager;
import org.reactome.server.service.manager.DiagramRasterExportManager;
import org.reactome.server.tools.SBMLFactory;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Lorente-Arencibia, Pascual (plorente@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings({"unused", "ConstantConditions", "SpringAutowiredFieldsWarningInspection", "WeakerAccess"})
@RestController
@Api(tags = "exporter", description = "Reactome Data: Format Exporter")
@RequestMapping("/exporter")
public class DiagramExporterController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    public static final String PNG_FILE_EXTENSION = ".png";
    public static final String PPT_FILE_EXTENSION = ".pptx";
    public static final String SBML_FILE_EXTENSION = ".xml";

    private static final Object RASTER_SEMAPHORE = new Object();
    private static final int MAX_RASTER_SIZE = 120000000; //60000000;
    private static int CURRENT_RASTER_SIZE = 0;

    @Autowired
    private GeneralService generalService;

    @Autowired
    private DatabaseObjectService databaseObjectService;

    @Autowired
    private DiagramService diagramService;

    @Autowired
    private DiagramPPTXExportManager pptxManager;

    @Autowired
    private DiagramRasterExportManager rasterManager;

    @ApiOperation(
            value = "Export a given pathway diagram to raster file",
            notes = "This method accepts identifiers for <a href=\"/content/schema/Event\" target=\"_blank\">Event class</a> instances." +
                    "<br/>When a diagrammed pathway is provided, the diagram is exported to the specified format." +
                    "<br/>When a subpathway is provided, the diagram for the parent is exported and the events that are part of the subpathways are selected." +
                    "<br/>When a reaction is provided, the diagram containing the reaction is exported and the reaction is selected." +
                    "<br/><br/>Find out more about this method <a href=\"/dev/content-service/diagram-exporter\" target=\"_blank\">here</a>",
            produces = "image/png, image/jpg, image/jpeg, image/svg+xml, image/gif"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "Stable Identifier does not match with any of the available diagrams."),
            @ApiResponse(code = 500, message = "Could not deserialize diagram file."),
            @ApiResponse(code = 503, message = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/diagram/{identifier}.{ext:.*}", method = RequestMethod.GET)
    public void toRaster( @ApiParam(value = "Event identifier (it can be a pathway with diagram, a subpathway or a reaction)", required = true, defaultValue = "R-HSA-177929")
                         @PathVariable String identifier,
                          @ApiParam(value = "File extension (defines the image format)", required = true, defaultValue = "png", allowableValues = "png,jpg,jpeg,svg,gif")
                         @PathVariable String ext,

                          @ApiParam(value = "Result image quality between [1 - 10]. It defines the quality of the final image (Default 5)", defaultValue = "5")
                         @RequestParam(value = "quality", required = false, defaultValue = "5") Integer quality,
                          @ApiParam(value = "Flag element(s) in the diagram. CSV line.")
                         @RequestParam(value = "flg", required = false) List<String> flg,
                          @ApiParam(value = "Highlight element(s) selection in the diagram. CSV line.")
                         @RequestParam(value = "sel", required = false) List<String> sel,
                          @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                         @RequestParam(value = "diagramProfile", defaultValue = "Modern", required = false) String diagramProfile,

                          @ApiParam(value = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> token with the results to be overlaid on top of the given diagram")
                         @RequestParam(value = "token", required = false) String token,
                          @ApiParam(value = "Analysis  Color Profile", defaultValue = "Standard", allowableValues = "Standard, Strosobar, Copper%20Plus")
                         @RequestParam(value = "analysisProfile", defaultValue = "Standard", required = false) String analysisProfile,
                          @ApiParam(value = "Expression column. When the token is associated to an expression analysis, this parameter allows specifying the expression column for the overlay")
                         @RequestParam(value = "expColumn", required = false) Integer expColumn,

                         HttpServletResponse response) throws AnalysisException, EHLDException, TranscoderException, DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramExporterException {

        DiagramResult result = diagramService.getDiagramResult(identifier);
        if (result == null) throw new DiagramExporterException(String.format("'%s' is not an event", identifier));

        int size = result.getSize() * (int) Math.ceil(quality * 0.3);
        try {
            synchronized (RASTER_SEMAPHORE) {
                while ((CURRENT_RASTER_SIZE + size) >= MAX_RASTER_SIZE) RASTER_SEMAPHORE.wait();
                CURRENT_RASTER_SIZE += size;
            }

            List<String> toSelect = result.getEvents();
            if (sel != null) toSelect.addAll(sel);

            final RasterArgs args = new RasterArgs(result.getDiagramStId(), ext);
            args.setProfiles(new ColorProfiles(diagramProfile, analysisProfile, null));
            args.setFlags(flg);
            args.setSelected(toSelect);
            args.setToken(token);
            args.setQuality(quality);
            args.setColumn(expColumn);

            rasterManager.exportRaster(args, response);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage()); //This won't generate a 400, but a 500 instead (@see GlobalExceptionHandler.handleUnclassified)
        } finally {
            synchronized (RASTER_SEMAPHORE) {
                CURRENT_RASTER_SIZE -= size;
                RASTER_SEMAPHORE.notify();
            }
        }
    }

    @ApiIgnore
    @ApiOperation(value = "Export given diagram to PowerPoint")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Stable Identifier does not match with any of the available diagrams."),
            @ApiResponse(code = 500, message = "Could not deserialize diagram file."),
            @ApiResponse(code = 503, message = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/diagram/{stId}" + PPT_FILE_EXTENSION, method = RequestMethod.GET)
    public void toPPTX( @ApiParam(value = "Stable Identifier", required = true, defaultValue = "R-HSA-177929")
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
        File pptx = pptxManager.getPPTX(stId, colorProfile, decorator, response);

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
        if (decorator.isDecorated()) {
            if (!pptx.delete()) {
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
    public synchronized void toSBML( @ApiParam(value = "DbId or StId of the requested database object", required = true, defaultValue = "R-HSA-68616")
                                    @PathVariable String id,
                                    HttpServletResponse response) throws Exception {
        Pathway p;
        try {
            p = databaseObjectService.findById(id);
        } catch (ClassCastException ex) {
            throw new UnprocessableEntityException("The identifier '" + id + "' does not correspond to a 'Pathway'");
        }
        if (p == null) throw new NotFoundException("Identifier '" + id + "' not found");

        String sbmlFileName = p.getStId() + DiagramExporterController.SBML_FILE_EXTENSION;
        InputStream sbml;
        try {
            File file = pptxManager.getSBML(p, sbmlFileName);
            sbml = new FileInputStream(file);
            infoLogger.info("Exporting the pathway {} to SBML retrieved from previously generated file", p.getStId());
        } catch (MissingSBMLException | IOException e) {
            String content = SBMLFactory.getSBML(p, generalService.getDBVersion());
            pptxManager.saveSBML(content, sbmlFileName);
            sbml = IOUtils.toInputStream(content, Charset.defaultCharset());
            infoLogger.info("Exporting the pathway {} to SBML", p.getStId());
            generalService.clearCache();
        }

        response.setContentType("application/sbml+xml");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + sbmlFileName + "\"");

        OutputStream out = response.getOutputStream();
        IOUtils.copy(sbml, out);
        out.flush();
        out.close();
    }

}
