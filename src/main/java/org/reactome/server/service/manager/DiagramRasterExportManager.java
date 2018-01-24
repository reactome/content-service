package org.reactome.server.service.manager;

import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.util.DatabaseObjectUtils;
import org.reactome.server.service.exception.RasterException;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisResult;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lorente-Arencibia, Pascual (plorente@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@Component
public class DiagramRasterExportManager {


    @Autowired
    private AdvancedDatabaseObjectService advancedDBObjectService;

    private String diagramDir;
    private String ehldDir;

    @Autowired
    public DiagramRasterExportManager(@Value("${diagram.json.folder}") String diagramDir,
                                      @Value("${ehld.folder}") String ehldDir,
                                      @Value("${svg.summary.file}") String svgSummaryFile,
                                      @Value("${analysis.server}") String analysisServer) {
        this.diagramDir = diagramDir;
        this.ehldDir = ehldDir;
        RasterExporter.initialise(svgSummaryFile);
        AnalysisClient.setServer(analysisServer);
    }

    public ResponseEntity exportRaster(RasterArgs args, HttpServletResponse response) {

        final String ext = args.getFormat();
        final String token = args.getToken();
        final Integer column = args.getColumn();
        // TODO: return codes through response
        // - token not valid
        // - unknown stable identifier
        // - invalid format/ext

        //response.addHeader("Content-Disposition", "attachment; filename=\"" + args.getStId() + "." + args.getFormat().toLowerCase() + "\"");
        response.addHeader("Content-Type", "image/" + args.getFormat());
        if (column == null && ext.equalsIgnoreCase("gif") && isExpression(token)) {
            gif(args, response);
        } else {
            normal(args, response);
        }
        return new ResponseEntity(HttpStatus.OK);
    }


    private boolean isExpression(String token) {
        if (token == null || token.isEmpty()) return false;
        try {
            final AnalysisResult result = AnalysisClient.getAnalysisResult(token);
            final AnalysisType analysisType = AnalysisType.getType(result.getSummary().getType());
            return analysisType == AnalysisType.EXPRESSION;
        } catch (AnalysisServerError e) {
            // token does not exist
            return false;
        }
    }

    private void gif(RasterArgs args, HttpServletResponse response) {
        try {
            final OutputStream os = response.getOutputStream();
            RasterExporter.exportToGif(args, diagramDir, ehldDir, os);
            os.close();
        } catch (Exception e) {
            throw new RasterException(e.getMessage());
        }
    }

    private void normal(RasterArgs args, HttpServletResponse response) {
        try {
            final BufferedImage image = RasterExporter.export(args, diagramDir, ehldDir);
            final OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, args.getFormat(), outputStream);
        } catch (Exception e) {
            throw new RasterException(e.getMessage());
        }
    }

    public DiagramResult argumentsValidation(String identifier) throws Exception {
        String query;
        Map<String, Object> parametersMap = new HashMap<>();

        String id = DatabaseObjectUtils.getIdentifier(identifier);
        if (DatabaseObjectUtils.isStId(id)) {
            query = "" +
                    "MATCH (e:Pathway{stId:{stId}, hasDiagram:True}) " +
                    "RETURN e.stId As diagramStId, [] AS events, e.diagramWidth * e.diagramHeight AS size " +
                    "UNION " +
                    "MATCH path=(d:Pathway{hasDiagram:True})-[:hasEvent*]->(:Pathway{stId:{stId}, hasDiagram:False})-[:hasEvent*]->(r:ReactionLikeEvent) " +
                    "WHERE SINGLE(x IN NODES(path) WHERE (x:Pathway) AND x.hasDiagram) " +
                    "RETURN d.stId as diagramStId, COLLECT(DISTINCT r.stId) AS events, d.diagramWidth * d.diagramHeight AS size " +
                    "UNION " +
                    "MATCH path=(d:Pathway{hasDiagram:True})-[:hasEvent*]->(r:ReactionLikeEvent{stId:{stId}}) " +
                    "WHERE SINGLE(x IN NODES(path) WHERE (x:Pathway) AND x.hasDiagram) " +
                    "RETURN d.stId as diagramStId, [{stId}] AS events, d.diagramWidth * d.diagramHeight AS size";
            parametersMap.put("stId", id);
        } else if (DatabaseObjectUtils.isDbId(id)){
            query = "" +
                    "MATCH (e:Pathway{dbId:{dbId}, hasDiagram:True}) " +
                    "RETURN e.stId As diagramStId, [] AS events, e.diagramWidth * e.diagramHeight AS size  " +
                    "UNION " +
                    "MATCH path=(d:Pathway{hasDiagram:True})-[:hasEvent*]->(:Pathway{dbId:{dbId}, hasDiagram:False})-[:hasEvent*]->(r:ReactionLikeEvent) " +
                    "WHERE SINGLE(x IN NODES(path) WHERE (x:Pathway) AND x.hasDiagram) " +
                    "RETURN d.stId as diagramStId, COLLECT(DISTINCT r.stId) AS events, d.diagramWidth * d.diagramHeight AS size " +
                    "UNION " +
                    "MATCH path=(d:Pathway{hasDiagram:True})-[:hasEvent*]->(r:ReactionLikeEvent{dbId:{dbId}}) " +
                    "WHERE SINGLE(x IN NODES(path) WHERE (x:Pathway) AND x.hasDiagram) " +
                    "RETURN d.stId as diagramStId, [{dbId}] AS events, d.diagramWidth * d.diagramHeight AS size";
            parametersMap.put("dbId", Long.valueOf(id));
        } else {
            throw new Exception(String.format("'%s' is not a valid identifier", identifier));
        }

        try {
            return advancedDBObjectService.customQueryForObject(DiagramResult.class, query, parametersMap);
        } catch (CustomQueryException e) {
            throw new Exception(e.getMessage());
        }
    }
}
