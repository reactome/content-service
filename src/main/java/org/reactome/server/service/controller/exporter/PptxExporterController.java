package org.reactome.server.service.controller.exporter;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.IOUtils;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.domain.result.DiagramResult;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DiagramService;
import org.reactome.server.graph.service.util.DatabaseObjectUtils;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.service.exception.DiagramExporterException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.manager.ExportManager;
import org.reactome.server.service.manager.SearchManager;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@Tag(name = "exporter", description = "Reactome Data: Format Exporter")
@RequestMapping("/exporter")
public class PptxExporterController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    public static final String PPT_FILE_EXTENSION = ".pptx";

    private DatabaseObjectService databaseObjectService;
    private AdvancedDatabaseObjectService ados;
    private DiagramService diagramService;
    private ExportManager exportManager;
    private SearchManager searchManager;

    @Hidden
    @Operation(summary = "Exports a given pathway diagram to PowerPoint")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Stable Identifier does not match with any of the available diagrams."),
            @ApiResponse(responseCode = "500", description = "Could not deserialize diagram file."),
            @ApiResponse(responseCode = "503", description = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/diagram/{identifier}" + PPT_FILE_EXTENSION, method = RequestMethod.GET)
    public synchronized void diagramPPTX(@Parameter(description = "Stable Identifier", required = true, example = "R-HSA-177929")
                                         @PathVariable String identifier,
                                         @Parameter(description = "Diagram Color Profile", example = "Modern", schema = @Schema(allowableValues = {"Standard", "Modern"}))
                                         @RequestParam(value = "diagramProfile", defaultValue = "Modern", required = false) String diagramProfile,
                                         @Parameter(description = "Gene name, protein or chemical identifier or Reactome identifier used to flag elements in the diagram")
                                         @RequestParam(value = "flg", required = false) String flg,
                                         @Parameter(description = "Defines whether to take into account interactors for the flagging")
                                         @RequestParam(value = "flgInteractors", required = false, defaultValue = "true") Boolean flgInteractors,
                                         @Parameter(description = "Highlight element(s) selection in the diagram. CSV line.")
                                         @RequestParam(value = "sel", required = false) List<String> sel,
                                         HttpServletResponse response) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramProfileException, IOException {

        DiagramResult result = diagramService.getDiagramResult(identifier);
        // IMPORTANT: Downloading the file on Swagger does not work - https://github.com/swagger-api/swagger-ui/issues/2132
        // for this reason we are keeping this method as APIIgnore
        infoLogger.info("Exporting the Diagram {} to PPTX for color profile {}", result.getDiagramStId(), diagramProfile);

        Decorator decorator = new Decorator();

        List<Long> toSelect = new ArrayList<>();
        if (sel != null) toSelect.addAll(getDatabaseIdentifiers(sel));
        toSelect.addAll(getDatabaseIdentifiers(result.getEvents()));
        decorator.setSelected(toSelect);

        if (flg != null && !flg.isEmpty()) {
            try {
                Collection<String> flag = searchManager.getDiagramFlagging(result.getDiagramStId(), flg, flgInteractors);
                decorator.setFlags(getDatabaseIdentifiers(flag));
            } catch (SolrSearcherException e) {
                //Nothing to be flagged
            }
        }

        File pptx = exportManager.getDiagramPPTX(result.getDiagramStId(), diagramProfile, decorator, response);

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

    @Hidden
    @Operation(summary = "Exports a given reaction to PowerPoint")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier not found"),
            @ApiResponse(responseCode = "422", description = "Identifier does not correspond to a pathway or reaction"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/reaction/{identifier}.pptx", method = RequestMethod.GET)
    public synchronized void reactionPPTX(@Parameter(description = "DbId or StId of the requested pathway or reaction", required = true, example = "R-HSA-5205682")
                                          @PathVariable String identifier,
                                          @Parameter(description = "Diagram Color Profile", example = "Modern", schema = @Schema(allowableValues = {"Standard", "Modern"}))
                                          @RequestParam(value = "diagramProfile", defaultValue = "Modern", required = false) String diagramProfile,
                                          @Parameter(description = "Gene name, protein or chemical identifier or Reactome identifier used to flag elements in the diagram")
                                          @RequestParam(value = "flg", required = false) String flg,
                                          @Parameter(description = "Defines whether to take into account interactors for the flagging")
                                          @RequestParam(value = "flgInteractors", required = false, defaultValue = "true") Boolean flgInteractors,
                                          @Parameter(description = "Highlight element(s) selection in the diagram. CSV line.")
                                          @RequestParam(value = "sel", required = false) List<String> sel,
                                          HttpServletResponse response) throws Exception {
        // IMPORTANT: Downloading the file on Swagger does not work - https://github.com/swagger-api/swagger-ui/issues/2132
        // for this reason we are keeping this method as APIIgnore
        infoLogger.info("Exporting the Diagram {} to PPTX for color profile {}", identifier, diagramProfile);

        Decorator decorator = new Decorator();

        DiagramResult result = diagramService.getDiagramResult(identifier);
        List<Long> toSelect = new ArrayList<>();
        if (sel != null) toSelect.addAll(getDatabaseIdentifiers(sel));
        decorator.setSelected(toSelect);

        if (flg != null && !flg.isEmpty()) {
            try {
                Collection<String> flag = searchManager.getDiagramFlagging(result.getDiagramStId(), flg, flgInteractors);
                decorator.setFlags(getDatabaseIdentifiers(flag));
            } catch (SolrSearcherException e) {
                //Nothing to be flagged
            }
        }

        ReactionLikeEvent rle = getReactionLikeEvent(identifier);
        File pptx = exportManager.getReactionPPTX(rle, diagramProfile, decorator, response);

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

    private ReactionLikeEvent getReactionLikeEvent(String id) {
        ReactionLikeEvent rle;
        try {
            rle = databaseObjectService.findById(id);
        } catch (ClassCastException e) {
            throw new DiagramExporterException(String.format("The identifier '%s' does not correspond to a 'ReactionLikeEvent'", id));
        }
        if (rle == null) throw new NotFoundException(String.format("Identifier '%s' not found", id));
        return rle;
    }

    /**
     * Transforms stable identifiers (and old stable identifiers) to database identifiers. It keeps database identifiers
     *
     * @param identifiers a list of identifiers that can mix stable identifiers, database identifiers and old stable identifiers
     * @return The provided 'identifiers' list with the stable identifiers transformed to database identifiers
     */
    private List<Long> getDatabaseIdentifiers(Collection<String> identifiers) {
        List<String> aux = new ArrayList<>();

        List<Long> rtn = new ArrayList<>();
        for (String identifier : identifiers) {
            String id = DatabaseObjectUtils.getIdentifier(identifier);
            if (DatabaseObjectUtils.isStId(id)) aux.add(id);
            else if (DatabaseObjectUtils.isDbId(id)) rtn.add(Long.valueOf(id));
        }

        //language=cypher
        String query = "" +
                "MATCH (d:DatabaseObject) " +
                "WHERE d.stId IN $identifiers " +
                "RETURN d.dbId ";
        Map<String, Object> params = new HashMap<>();
        params.put("identifiers", aux);
        try {
            rtn.addAll(ados.getCustomQueryResults(Long.class, query, params));
        } catch (CustomQueryException e) {
            //Nothing here
        }
        return rtn;
    }

    @Autowired
    public void setDatabaseObjectService(DatabaseObjectService databaseObjectService) {
        this.databaseObjectService = databaseObjectService;
    }

    @Autowired
    public void setAdos(AdvancedDatabaseObjectService ados) {
        this.ados = ados;
    }

    @Autowired
    public void setDiagramService(DiagramService diagramService) {
        this.diagramService = diagramService;
    }

    @Autowired
    public void setExportManager(ExportManager exportManager) {
        this.exportManager = exportManager;
    }

    @Autowired
    public void setSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }
}
