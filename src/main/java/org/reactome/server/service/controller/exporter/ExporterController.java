package org.reactome.server.service.controller.exporter;

import io.swagger.annotations.*;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.IOUtils;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.domain.result.DiagramResult;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DiagramService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.exception.DiagramExporterException;
import org.reactome.server.service.exception.MissingSBMLException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.manager.ExportManager;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.diagram.exporter.sbgn.SbgnConverter;
import org.reactome.server.tools.reaction.exporter.ReactionExporter;
import org.reactome.server.tools.reaction.exporter.layout.model.Layout;
import org.reactome.server.tools.sbml.converter.SbmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Lorente-Arencibia, Pascual (plorente@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@Api(tags = "exporter", description = "Reactome Data: Format Exporter")
@RequestMapping("/exporter")
public class ExporterController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    public static final String PPT_FILE_EXTENSION = ".pptx";
    private static final String SBML_FILE_EXTENSION = ".sbml";
    private static final String SBGN_FILE_EXTENSION = ".sbgn";

    private static final Object RASTER_SEMAPHORE = new Object();
    private static final int MAX_RASTER_SIZE = 120000000; //60000000;
    private static int CURRENT_RASTER_SIZE = 0;

    @Autowired
    private GeneralService generalService;

    @Autowired
    private DatabaseObjectService databaseObjectService;

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    @Autowired
    private DiagramService diagramService;

    @Autowired
    private ExportManager exportManager;

    @Autowired
    private RasterExporter rasterExporter;

    @Autowired
    private ReactionExporter reactionExporter;

    @ApiOperation(
            value = "Exports a given pathway diagram to the specified image format (png, jpg, jpeg, svg, gif)",
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
    public void diagramImage(@ApiParam(value = "Event identifier (it can be a pathway with diagram, a subpathway or a reaction)", required = true, defaultValue = "R-HSA-177929")
                            @PathVariable String identifier,
                             @ApiParam(value = "File extension (defines the image format)", required = true, defaultValue = "png", allowableValues = "png,jpg,jpeg,svg,gif")
                            @PathVariable String ext,

                             @ApiParam(value = "Result image quality between [1 - 10]. It defines the quality of the final image (Default 5)", defaultValue = "5")
                            @RequestParam(value = "quality", required = false, defaultValue = "5") Integer quality,
                             @ApiParam(value = "Flag element(s) in the diagram. CSV line.")
                            @RequestParam(value = "flg", required = false) List<String> flg,
                             @ApiParam(value = "Highlight element(s) selection in the diagram. CSV line.")
                            @RequestParam(value = "sel", required = false) List<String> sel,
                             @ApiParam(value = "Sets whether the name of the pathway is shown below", defaultValue = "true")
                            @RequestParam(value = "title", required = false, defaultValue = "true") Boolean title,
                             @ApiParam(value = "Defines the image margin between [0 - 20] (Default 15)", defaultValue = "15")
                            @RequestParam(value = "margin", defaultValue = "15", required = false) Integer margin,
                             @ApiParam(value = "Defines whether textbook-like illustration are taken into account", defaultValue = "true")
                            @RequestParam(value = "ehld", defaultValue = "true", required = false) Boolean ehld,
                             @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                            @RequestParam(value = "diagramProfile", defaultValue = "Modern", required = false) String diagramProfile,

                             @ApiParam(value = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> token with the results to be overlaid on top of the given diagram")
                            @RequestParam(value = "token", required = false) String token,
                             @ApiParam(value = "Analysis  Color Profile", defaultValue = "Standard", allowableValues = "Standard, Strosobar, Copper%20Plus")
                            @RequestParam(value = "analysisProfile", defaultValue = "Standard", required = false) String analysisProfile,
                             @ApiParam(value = "Expression column. When the token is associated to an expression analysis, this parameter allows specifying the expression column for the overlay")
                            @RequestParam(value = "expColumn", required = false) Integer expColumn,

                            HttpServletResponse response) throws AnalysisException, EhldException, TranscoderException, DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramExporterException {

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
            args.setMargin(margin);
            args.setEhld(ehld);
            args.setWriteTitle(title);

            String type = ext.equalsIgnoreCase("svg") ? "svg+xml" : ext.toLowerCase();
            response.addHeader("Content-Type", "image/" + type);
            rasterExporter.export(args, response.getOutputStream());
        } catch (IndexOutOfBoundsException e) { //When the output stream is closed, it throws this exception
            //Nothing here
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e.getMessage()); //This won't generate a 400, but a 500 instead (@see GlobalExceptionHandler.handleUnclassified)
        } finally {
            synchronized (RASTER_SEMAPHORE) {
                CURRENT_RASTER_SIZE -= size;
                RASTER_SEMAPHORE.notify();
            }
        }
    }

    //@ApiIgnore
    @ApiOperation(value = "Exports a given pathway diagram to PowerPoint")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Stable Identifier does not match with any of the available diagrams."),
            @ApiResponse(code = 500, message = "Could not deserialize diagram file."),
            @ApiResponse(code = 503, message = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/diagram/{identifier}" + PPT_FILE_EXTENSION, method = RequestMethod.GET)
    public void diagramPPTX(@ApiParam(value = "Stable Identifier", required = true, defaultValue = "R-HSA-177929")
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
        if (decorator.isDecorated()) {
            if (!pptx.delete()) {
                infoLogger.error("Could not delete the temporary file {}", pptx.getPath());
            }
        }
    }

    @ApiIgnore
    @ApiOperation(
            value = "Exports a given reaction to the specified image format (png, jpg, jpeg, svg, gif)",
            notes = "This method accepts identifiers for <a href=\"/content/schema/ReactionLikeEvent\" target=\"_blank\">ReactionLikeEvent class</a> instances.",
            produces = "image/png, image/jpg, image/jpeg, image/svg+xml, image/gif"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "Stable Identifier does not match with any of the reactions."),
            @ApiResponse(code = 500, message = "Could not deserialize diagram file."),
            @ApiResponse(code = 503, message = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/reaction/{identifier}.{ext:.*}", method = RequestMethod.GET)
    public void reactionImage(@ApiParam(value = "Reaction identifier", required = true, defaultValue = "R-HSA-6787403")
                             @PathVariable String identifier,
                              @ApiParam(value = "File extension (defines the image format)", required = true, defaultValue = "png", allowableValues = "png,jpg,jpeg,svg,gif")
                             @PathVariable String ext,

                              @ApiParam(value = "Result image quality between [1 - 10]. It defines the quality of the final image (Default 5)", defaultValue = "5")
                             @RequestParam(value = "quality", required = false, defaultValue = "5") Integer quality,
                              @ApiParam(value = "Flag element(s) in the diagram. CSV line.")
                             @RequestParam(value = "flg", required = false) List<String> flg,
                              @ApiParam(value = "Highlight element(s) selection in the diagram. CSV line.")
                             @RequestParam(value = "sel", required = false) List<String> sel,
                              @ApiParam(value = "Sets whether the name of the reaction is shown below", defaultValue = "true")
                             @RequestParam(value = "title", required = false, defaultValue = "true") Boolean title,
                              @ApiParam(value = "Defines the image margin between [0 - 20] (Default 15)", defaultValue = "15")
                             @RequestParam(value = "margin", defaultValue = "15", required = false) Integer margin,
                              @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                             @RequestParam(value = "diagramProfile", defaultValue = "Modern", required = false) String diagramProfile,

                              @ApiParam(value = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> token with the results to be overlaid on top of the given reaction")
                             @RequestParam(value = "token", required = false) String token,
                              @ApiParam(value = "Analysis  Color Profile", defaultValue = "Standard", allowableValues = "Standard, Strosobar, Copper%20Plus")
                             @RequestParam(value = "analysisProfile", defaultValue = "Standard", required = false) String analysisProfile,
                              @ApiParam(value = "Expression column. When the token is associated to an expression analysis, this parameter allows specifying the expression column for the overlay")
                             @RequestParam(value = "expColumn", required = false) Integer expColumn,

                               HttpServletResponse response) {
        ReactionLikeEvent rle = getReactionLikeEvent(identifier);
        Layout layout = reactionExporter.getReactionLayout(rle);
        Diagram diagram = reactionExporter.getReactionDiagram(layout);
        Graph graph = reactionExporter.getReactionGraph(rle, layout);

        final RasterArgs args;
        if (token != null && diagram.getStableId()!=null) {
            args = new RasterArgs(diagram.getStableId(), ext);
        } else {
            args = new RasterArgs(ext);
        }
        args.setProfiles(new ColorProfiles(diagramProfile, analysisProfile, null));
        args.setFlags(flg);
        args.setSelected(sel);
        args.setToken(token);
        args.setQuality(quality);
        args.setColumn(expColumn);
        args.setMargin(margin);
        args.setWriteTitle(title);

        try {
            String type = ext.equalsIgnoreCase("svg") ? "svg+xml" : ext.toLowerCase();
            response.addHeader("Content-Type", "image/" + type);
            rasterExporter.export(diagram, graph, args, null ,response.getOutputStream());
        } catch (IOException | TranscoderException | AnalysisException e) {
            throw new RuntimeException(e.getMessage()); //This won't generate a 400, but a 500 instead (@see GlobalExceptionHandler.handleUnclassified)
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
        if (decorator.isDecorated()) {
            if (!pptx.delete()) {
                infoLogger.error("Could not delete the temporary file {}", pptx.getPath());
            }
        }
    }

    //@ApiIgnore
    @ApiOperation(value = "Exports a given pathway or reaction to SBGN")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier not found"),
            @ApiResponse(code = 422, message = "Identifier does not correspond to a pathway or reaction"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/event/{identifier}.sbgn", method = RequestMethod.GET)
    public synchronized void eventSBGN(@ApiParam(value = "DbId or StId of the requested pathway or reaction", required = true, defaultValue = "R-HSA-5205682")
                                      @PathVariable String identifier,
                                      HttpServletResponse response) throws Exception {
        Event event = getEvent(identifier);
        String fileName = event.getStId() + SBGN_FILE_EXTENSION;
        response.setContentType("application/sbgn+xml");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        OutputStream out = response.getOutputStream();
        IOUtils.copy(getSBGN(event, fileName), out);
        out.flush();
        out.close();
    }

    private InputStream getSBGN(Event event, String fileName) throws FileNotFoundException {
        InputStream sbgn;
        try {
            File file = exportManager.getCachedFile(event, fileName);
            sbgn = new FileInputStream(file);
            infoLogger.info("Exporting the event {} to SBGN retrieved from previously generated file", event.getStId());
        } catch (MissingSBMLException | IOException e) {
            SbgnConverter converter = new SbgnConverter(exportManager.getDiagram(event));
            sbgn = exportManager.saveSBGN(converter.getSbgn(), fileName);
            infoLogger.info("Exporting the event {} to SBGN", event.getStId());
        }
        return sbgn;
    }


    @ApiIgnore //Only kept here to keep backwards compatibility with the previous URI (ATTENTION: to ".xml")
    @RequestMapping(value = "/sbml/{identifier}.xml", method = RequestMethod.GET)
    public synchronized void eventSBMLOld(@PathVariable String identifier, HttpServletResponse response) throws Exception {
        eventSBML(identifier, response);
    }

    @ApiOperation(value = "Exports a given pathway or reaction to SBML")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier not found"),
            @ApiResponse(code = 422, message = "Identifier does not correspond to a pathway or reaction"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/event/{identifier}.sbml", method = RequestMethod.GET)
    public synchronized void eventSBML(@ApiParam(value = "DbId or StId of the requested pathway or reaction", required = true, defaultValue = "R-HSA-68616")
                                      @PathVariable String identifier,
                                      HttpServletResponse response) throws Exception {
        Event event = getEvent(identifier);
        String fileName = event.getStId() + SBML_FILE_EXTENSION;
        response.setContentType("application/sbml+xml");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        OutputStream out = response.getOutputStream();
        IOUtils.copy(getSBML(event, fileName), out);
        out.flush();
        out.close();
    }

    private InputStream getSBML(Event event, String fileName) throws FileNotFoundException {
        InputStream sbml;
        try {
            File file = exportManager.getCachedFile(event, fileName);
            sbml = new FileInputStream(file);
            infoLogger.info("Exporting the event {} to SBML retrieved from previously generated file", event.getStId());
        } catch (MissingSBMLException | IOException e) {
            SbmlConverter converter = new SbmlConverter(event, generalService.getDBInfo().getVersion(), advancedDatabaseObjectService);
            converter.convert();
            String content = converter.toString();
            sbml = exportManager.saveSBML(content, fileName);
            infoLogger.info("Exporting the event {} to SBML", event.getStId());
            generalService.clearCache();
        }
        return sbml;
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
