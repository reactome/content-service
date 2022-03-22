package org.reactome.server.service.controller.exporter;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.IOUtils;
import org.gk.persistence.MySQLAdaptor;
import org.reactome.sbml.rel.SbmlConverterForRel;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.exception.DiagramExporterException;
import org.reactome.server.service.exception.MissingSBXXException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.manager.ExportManager;
import org.reactome.server.tools.diagram.exporter.sbgn.SbgnConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RestController
@Tag(name = "exporter")
@RequestMapping("/exporter")
public class SbxxExporterController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private static final String SBML_FILE_EXTENSION = ".sbml";
    private static final String SBGN_FILE_EXTENSION = ".sbgn";

    private GeneralService generalService;
    private DatabaseObjectService databaseObjectService;
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;
    private ExportManager exportManager;

    // For the time being use the relational database to export layout information for SBML
    // This should be updated to use JSON and Neo4j in the future
    private MySQLAdaptor mysqlDba;

    @Operation(summary = "Exports a given pathway or reaction to SBGN")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier not found"),
            @ApiResponse(responseCode = "422", description = "Identifier does not correspond to a pathway or reaction"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/event/{identifier}.sbgn", method = RequestMethod.GET)
    public synchronized void eventSBGN(@Parameter(description = "DbId or StId of the requested pathway or reaction", required = true, example = "R-HSA-5205682")
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
        } catch (MissingSBXXException | IOException e) {
            SbgnConverter converter = new SbgnConverter(exportManager.getDiagram(event));
            sbgn = exportManager.saveSBGN(converter.getSbgn(), fileName);
            infoLogger.info("Exporting the event {} to SBGN", event.getStId());
        }
        return sbgn;
    }


    @Hidden //Only kept here to keep backwards compatibility with the previous URI (ATTENTION: to ".xml")
    @RequestMapping(value = "/sbml/{identifier}.xml", method = RequestMethod.GET)
    public synchronized void eventSBMLOld(@PathVariable String identifier, HttpServletResponse response) throws Exception {
        eventSBML(identifier, response);
    }

    @Operation(summary = "Exports a given pathway or reaction to SBML")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier not found"),
            @ApiResponse(responseCode = "422", description = "Identifier does not correspond to a pathway or reaction"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/event/{identifier}.sbml", method = RequestMethod.GET)
    public synchronized void eventSBML(@Parameter(description = "DbId or StId of the requested pathway or reaction", required = true, example = "R-HSA-68616")
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
        } catch (MissingSBXXException | IOException e) {
            //SbmlConverter converter = new SbmlConverter(event, generalService.getDBInfo().getVersion(), advancedDatabaseObjectService);
            SbmlConverterForRel converter = new SbmlConverterForRel(event.getStId(),
                    generalService.getDBInfo().getVersion(),
                    advancedDatabaseObjectService);
            converter.setDBA(this.mysqlDba);
            converter.convert();
            String content = converter.toString();
            sbml = exportManager.saveSBML(content, fileName);
            infoLogger.info("Exporting the event {} to SBML", event.getStId());
            generalService.clearCache();
        }
        return sbml;
    }

    @Autowired
    public void setMySQLDBA(MySQLAdaptor dba) {
        this.mysqlDba = dba;
        if (dba != null) {
            infoLogger.info("Starting a dumb thread to keep MySQLAdaptor connected to avoid reconnection exception.");
            dba.initDumbThreadForConnection(); // To keep the DBA running to avoid connection error
        }
    }

    private Event getEvent(String id) {
        Event event;
        try {
            event = databaseObjectService.findById(id);
        } catch (ClassCastException ex) {
            throw new DiagramExporterException(String.format("The identifier '%s' does not correspond to a pathway or reaction", id));
        }
        if (event == null) throw new NotFoundException(String.format("Identifier '%s' not found", id));
        return event;
    }

    @Autowired
    public void setGeneralService(GeneralService generalService) {
        this.generalService = generalService;
    }

    @Autowired
    public void setDatabaseObjectService(DatabaseObjectService databaseObjectService) {
        this.databaseObjectService = databaseObjectService;
    }


    @Autowired
    public void setAdvancedDatabaseObjectService(AdvancedDatabaseObjectService advancedDatabaseObjectService) {
        this.advancedDatabaseObjectService = advancedDatabaseObjectService;
    }

    @Autowired
    public void setExportManager(ExportManager exportManager) {
        this.exportManager = exportManager;
    }
}
