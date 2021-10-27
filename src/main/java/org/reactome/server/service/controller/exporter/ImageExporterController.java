package org.reactome.server.service.controller.exporter;

import io.swagger.annotations.*;
import org.apache.batik.transcoder.TranscoderException;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.domain.result.DiagramResult;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DiagramService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.service.exception.DiagramExporterException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.manager.SearchManager;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.reaction.exporter.ReactionExporter;
import org.reactome.server.tools.reaction.exporter.compartment.ReactomeCompartmentFactory;
import org.reactome.server.tools.reaction.exporter.layout.model.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 * @author Lorente-Arencibia, Pascual (plorente@ebi.ac.uk)
 */
@RestController
@Api(tags = {"exporter"})
@RequestMapping("/exporter")
public class ImageExporterController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private static final Object RASTER_SEMAPHORE = new Object();
    private static final int MAX_RASTER_SIZE = 120000000; //60000000;
    private static int CURRENT_RASTER_SIZE = 0;

    private DatabaseObjectService databaseObjectService;
    private DiagramService diagramService;
    private RasterExporter rasterExporter;
    private ReactionExporter reactionExporter;
    private SearchManager searchManager;

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
            @ApiResponse(code = 500, message = "Could not deserialize diagram file.")
    })
    @RequestMapping(value = "/diagram/{identifier}.{ext:.*}", method = RequestMethod.GET)
    public void diagramImage(@ApiParam(value = "Event identifier (it can be a pathway with diagram, a subpathway or a reaction)", required = true, defaultValue = "R-HSA-177929")
                            @PathVariable String identifier,
                             @ApiParam(value = "File extension (defines the image format)", required = true, defaultValue = "png", allowableValues = "png,jpg,jpeg,svg,gif")
                            @PathVariable String ext,
                             @ApiParam(value = "Result image quality between [1 - 10]. It defines the quality of the final image (Default 5)", defaultValue = "5")
                            @RequestParam(value = "quality", required = false, defaultValue = "5") Integer quality,
                             @ApiParam(value = "Gene name, protein or chemical identifier or Reactome identifier used to flag elements in the diagram")
                            @RequestParam(value = "flg", required = false) String flg,
                             @ApiParam(value = "Defines whether to take into account interactors for the flagging")
                            @RequestParam(value = "flgInteractors", required = false, defaultValue = "true") Boolean flgInteractors,
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
                             @ApiParam(value = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> resource for which the results will be overlaid on top of the given pathways overview")
                            @RequestParam(value = "resource", required = false, defaultValue = "TOTAL") String resource,
                             @ApiParam(value = "Analysis  Color Profile", defaultValue = "Standard", allowableValues = "Standard, Strosobar, Copper%20Plus")
                            @RequestParam(value = "analysisProfile", defaultValue = "Standard", required = false) String analysisProfile,
                             @ApiParam(value = "Expression column. When the token is associated to an expression analysis, this parameter allows specifying the expression column for the overlay")
                            @RequestParam(value = "expColumn", required = false) Integer expColumn,

                            HttpServletResponse response) throws AnalysisException, EhldException, TranscoderException, DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramExporterException {

        DiagramResult result = diagramService.getDiagramResult(identifier);
        if (result == null) throw new DiagramExporterException(String.format("'%s' is not an event", identifier));
        infoLogger.info("Exporting the Diagram {} to {} for color profile {}", result.getDiagramStId(), ext, diagramProfile);

        //NO PDF for the time being
        if (ext.equalsIgnoreCase("pdf")) throw new IllegalArgumentException("Unsupported file extension pdf");

        int size = result.getSize() * (int) Math.ceil(quality * 0.3);
        boolean isSVG = ext.equalsIgnoreCase("svg");
        try {
            if (!isSVG) {
                synchronized (RASTER_SEMAPHORE) {
                    while ((CURRENT_RASTER_SIZE + size) >= MAX_RASTER_SIZE) RASTER_SEMAPHORE.wait();
                    CURRENT_RASTER_SIZE += size;
                }
            }

            final RasterArgs args = new RasterArgs(result.getDiagramStId(), ext);
            args.setProfiles(new ColorProfiles(diagramProfile, analysisProfile, null));

            List<String> toSelect = result.getEvents();
            if (sel != null) toSelect.addAll(sel);
            args.setSelected(toSelect);

            if (flg != null && !flg.isEmpty()) {
                try {
                    args.setFlags(searchManager.getDiagramFlagging(result.getDiagramStId(), flg, flgInteractors));
                } catch (SolrSearcherException e) {
                    //Nothing to be flagged
                }
            }

            args.setWriteTitle(title);
            args.setQuality(quality);
            args.setEhld(ehld);
            args.setMargin(margin);
            args.setToken(token);
            args.setResource(resource);
            args.setColumn(expColumn);

            String type = isSVG ? "svg+xml" : ext.toLowerCase();
            response.addHeader("Content-Type", "image/" + type);
            rasterExporter.export(args, response.getOutputStream());
        } catch (IndexOutOfBoundsException e) { //When the output stream is closed, it throws this exception
            //Nothing here
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e.getMessage()); //This won't generate a 400, but a 500 instead (@see GlobalExceptionHandler.handleUnclassified)
        } finally {
            if (!isSVG) {
                synchronized (RASTER_SEMAPHORE) {
                    CURRENT_RASTER_SIZE -= size;
                    RASTER_SEMAPHORE.notify();
                }
            }
        }
    }

    @ApiOperation(
            value = "Exports a given reaction to the specified image format (png, jpg, jpeg, svg, gif)",
            notes = "This method accepts identifiers for <a href=\"/content/schema/ReactionLikeEvent\" target=\"_blank\">ReactionLikeEvent class</a> instances.",
            produces = "image/png, image/jpg, image/jpeg, image/svg+xml, image/gif"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "Stable Identifier does not match with any of the reactions."),
            @ApiResponse(code = 500, message = "Could not deserialize diagram file.")
    })
    @RequestMapping(value = "/reaction/{identifier}.{ext:.*}", method = RequestMethod.GET)
    public void reactionImage(@ApiParam(value = "Reaction identifier", required = true, defaultValue = "R-HSA-6787403")
                             @PathVariable String identifier,
                              @ApiParam(value = "File extension (defines the image format)", required = true, defaultValue = "png", allowableValues = "png,jpg,jpeg,svg,gif")
                             @PathVariable String ext,

                              @ApiParam(value = "Result image quality between [1 - 10]. It defines the quality of the final image (Default 5)", defaultValue = "5")
                             @RequestParam(value = "quality", required = false, defaultValue = "5") Integer quality,
                              @ApiParam(value = "Gene name, protein or chemical identifier or Reactome identifier used to flag elements in the diagram")
                             @RequestParam(value = "flg", required = false) String flg,
                              @ApiParam(value = "Defines whether to take into account interactors for the flagging")
                             @RequestParam(value = "flgInteractors", required = false, defaultValue = "true") Boolean flgInteractors,
                              @ApiParam(value = "Highlight element selection in the diagram.")
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
                              @ApiParam(value = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> resource for which the results will be overlaid on top of the given pathways overview")
                             @RequestParam(value = "resource", required = false, defaultValue = "TOTAL") String resource,
                              @ApiParam(value = "Expression column. When the token is associated to an expression analysis, this parameter allows specifying the expression column for the overlay")
                             @RequestParam(value = "expColumn", required = false) Integer expColumn,

                               HttpServletResponse response) throws SolrSearcherException {

        ReactionLikeEvent rle = getReactionLikeEvent(identifier);
        infoLogger.info("Exporting the Reaction {} to {} for color profile {}", rle.getStId(), ext, diagramProfile);

        //NO PDF for the time being
        if(ext.equalsIgnoreCase("pdf")) throw new IllegalArgumentException("Unsupported file extension pdf");

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

        if (flg != null && !flg.isEmpty()) {
            args.setFlags(searchManager.getDiagramFlagging(diagram.getStableId(), flg, flgInteractors));
        }
        args.setSelected(sel);
        args.setToken(token);
        args.setResource(resource);
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

    @Autowired
    public void setDatabaseObjectService(DatabaseObjectService databaseObjectService) {
        this.databaseObjectService = databaseObjectService;
    }

    @Autowired
    public void setDiagramService(DiagramService diagramService) {
        this.diagramService = diagramService;
    }

    @Autowired
    public void setRasterExporter(RasterExporter rasterExporter) {
        this.rasterExporter = rasterExporter;
    }

    @Autowired
    public void setReactionExporter(ReactionExporter reactionExporter) {
        this.reactionExporter = reactionExporter;
    }

    @Autowired
    public void setSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    @Autowired
    public void setSchemaService(SchemaService schemaService) {
        ReactomeCompartmentFactory.setSchemaService(schemaService);
    }
}
