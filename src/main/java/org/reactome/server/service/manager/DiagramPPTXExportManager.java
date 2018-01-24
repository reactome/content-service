package org.reactome.server.service.manager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.EventsService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.controller.exporter.DiagramExporterController;
import org.reactome.server.service.exception.MissingSBMLException;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.common.profiles.service.DiagramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@Component
public class DiagramPPTXExportManager {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    private static final Logger errorLogger = LoggerFactory.getLogger("errorLogger");

    private DiagramService diagramService;
    private GeneralService generalService;
    private EventsService eventsService;
    private DatabaseObjectService databaseObjectService;

    @Value("${diagram.json.folder}")
    private String diagramJsonFolder;

    @Value("${diagram.exporter.temp.folder}")
    private String diagramExporterTempFolder;

    public File getPPTX(String stId, String colorProfile, Decorator decorator, HttpServletResponse response) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramProfileException, IOException {
        String pptxFileName = getPPTXFileName(stId);
        String ancestorStId = getAncestorStId(stId);

        if (!diagramExporterTempFolder.endsWith("/")) diagramExporterTempFolder += "/";

        File outputFolder = new File(diagramExporterTempFolder + generalService.getDBVersion() + "/pptx/" + colorProfile.toLowerCase());
        if (!outputFolder.exists()) {
            infoLogger.debug("Creating the directory tree for storing pptx files");
            if (!outputFolder.mkdirs())
                infoLogger.error("Could not create the folder for the given DBVersion and profile");
        }

        File pptxFile = new File(outputFolder.getAbsolutePath() + "/" + stId + DiagramExporterController.PPT_FILE_EXTENSION);
        if (ancestorStId != null) {
            pptxFile = new File(outputFolder.getAbsolutePath() + "/" + ancestorStId + DiagramExporterController.PPT_FILE_EXTENSION);
        }

        // We don't want to cache neither read from cache if the diagram has selection and flags.
        //boolean isDecorated = ((flags != null && !flags.isEmpty()) || (selected != null && !selected.isEmpty()));

        // The pptx is save in the temp folder using only the StId, then when we write in the response header
        // we rename it using the [stId] displayName
        if (pptxFile.exists() && !decorator.isDecorated()) { // just return the file previously generated.
            infoLogger.debug("Diagram {} has been generated previously.", pptxFile.getName());
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + pptxFileName + "\"");
            return pptxFile;
        } else {
            infoLogger.debug("Export Diagram {} based on StableId {}", pptxFile.getName(), stId);
            File newFile = diagramService.exportToPPTX(stId, diagramJsonFolder, colorProfile, outputFolder.getPath(), decorator);
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + pptxFileName + "\"");
            return newFile;
        }
    }

    /**
     * Sometimes a pathway does not have diagram, then we check the first ancestor that has diagram.
     * We generate the powerpoint based on this ancestorStId, but final pptx filename has the stId received in the URL
     */
    private String getAncestorStId(String stId) {
        String ancestorStId = null;

        Collection<Collection<Pathway>> ancestors = eventsService.getEventAncestors(stId);
        if (ancestors != null && !ancestors.isEmpty()) {
            // This is not a Collection of Collection<Pathway> it is a Collection<Collection<LinkedHashMap<String, Object>>
            // SDN neither map to Pathway nor throw an error
            for (Collection<Pathway> ancestorsList : ancestors) {
                for (Object pathwayMap : ancestorsList) {
                    //noinspection unchecked
                    LinkedHashMap<String, Object> pathway = (LinkedHashMap<String, Object>) pathwayMap;
                    if (pathway != null && !pathway.isEmpty() && pathway.containsKey("hasDiagram")) {
                        Boolean hasDiagram = (Boolean) pathway.get("hasDiagram");
                        if (hasDiagram) {
                            ancestorStId = (String) pathway.get("stId");
                            break;
                        }
                    }
                }
            }
        }

        return ancestorStId;
    }

    /**
     * Query the graph database in order to the get the display name that will be used to in the file name.
     * In an unlikely case of empty display name this method will return the stable identifier.
     *
     * @return displayName (otherwise keep the stId)
     */
    private String getPPTXFileName(String stId) {
        DatabaseObject dbOb = databaseObjectService.findByIdNoRelations(stId);
        if (dbOb == null) return null;
        String displayName = stId;
        if (StringUtils.isNotEmpty(dbOb.getDisplayName())) {
            displayName = "[" + stId + "] " + dbOb.getDisplayName();
        }
        return displayName + ".pptx";
    }


    /* =========================================================== */
    /* ---------------------- SBML EXPORTER ---------------------- */
    /* =========================================================== */

    public File getSBML(Pathway pathway, String sbmlFileName) throws MissingSBMLException {
        if (!diagramExporterTempFolder.endsWith("/")) diagramExporterTempFolder += "/";

        // This folder will be created during release phase, double checking just in case
        File outputFolder = new File(diagramExporterTempFolder + generalService.getDBVersion() + "/sbml");
        if (outputFolder.exists()) {
            File sbml = new File(outputFolder.getAbsolutePath() + "/" + sbmlFileName);
            if (sbml.exists()) return sbml;
        }

        throw new MissingSBMLException("SBML hasn't been previously generated for " + pathway.getStId());
    }

    public void saveSBML(String sbml, String sbmlFileName) {
        File outputFolder = new File(diagramExporterTempFolder + generalService.getDBVersion() + "/sbml");
        if (!outputFolder.exists()) {
            infoLogger.debug("Creating the directory tree for storing SBML files");
            if (!outputFolder.mkdirs())
                infoLogger.error("Could not create the folder for the given DBVersion");
        }

        File file = new File(outputFolder.getAbsolutePath() + "/" + sbmlFileName);
        if (!file.exists()) {
            try {
                FileUtils.writeStringToFile(file, sbml, Charset.defaultCharset());
            } catch (IOException e) {
                errorLogger.error(e.getMessage());
            }
        } else {
            errorLogger.error("Trying to write a file that already exists");
        }
    }

    @Autowired
    public void setDiagramService(DiagramService diagramService) {
        this.diagramService = diagramService;
    }

    @Autowired
    public void setGeneralService(GeneralService generalService) {
        this.generalService = generalService;
    }

    @Autowired
    public void setEventsService(EventsService eventsService) {
        this.eventsService = eventsService;
    }

    @Autowired
    public void setDatabaseObjectService(DatabaseObjectService databaseObjectService) {
        this.databaseObjectService = databaseObjectService;
    }
}
