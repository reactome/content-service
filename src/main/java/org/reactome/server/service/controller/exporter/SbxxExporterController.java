package org.reactome.server.service.controller.exporter;

import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.exception.DiagramExporterException;
import org.reactome.server.service.exception.MissingSBMLException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.manager.ExportManager;
import org.reactome.server.tools.diagram.exporter.sbgn.SbgnConverter;
import org.reactome.server.tools.sbml.converter.SbmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RestController
@Api(tags = "exporter", description = "Reactome Data: Format Exporter")
@RequestMapping("/exporter")
public class SbxxExporterController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private static final String SBML_FILE_EXTENSION = ".sbml";
    private static final String SBGN_FILE_EXTENSION = ".sbgn";

    private GeneralService generalService;
    private DatabaseObjectService databaseObjectService;
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;
    private ExportManager exportManager;

    @ApiOperation(value = "Exports a given pathway to SBGN")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier not found"),
            @ApiResponse(code = 422, message = "Identifier does not correspond to a pathway"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/event/{identifier}.sbgn", method = RequestMethod.GET)
    public synchronized void eventSBGN(@ApiParam(value = "DbId or StId of the requested pathway", required = true, defaultValue = "R-HSA-5205682")
                                      @PathVariable String identifier,
                                      HttpServletResponse response) throws Exception {
        Event event = getPathway(identifier);
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

    @ApiOperation(value = "Exports a given pathway to SBML")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier not found"),
            @ApiResponse(code = 422, message = "Identifier does not correspond to a pathway"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/event/{identifier}.sbml", method = RequestMethod.GET)
    public synchronized void eventSBML(@ApiParam(value = "DbId or StId of the requested pathway", required = true, defaultValue = "R-HSA-68616")
                                      @PathVariable String identifier,
                                      HttpServletResponse response) throws Exception {
        Event event = getPathway(identifier);
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

    private Pathway getPathway(String id){
        Pathway pathway;
        try {
            pathway = databaseObjectService.findById(id);
        } catch (ClassCastException ex) {
            throw new DiagramExporterException(String.format("The identifier '%s' does not correspond to a 'Pathway'", id));
        }
        if (pathway == null) throw new NotFoundException(String.format("Identifier '%s' not found", id));
        return pathway;
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
