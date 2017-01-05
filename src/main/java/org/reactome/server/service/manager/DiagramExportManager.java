package org.reactome.server.service.manager;

import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.service.EventsService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.controller.exporter.DiagramExporterController;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.common.profiles.service.DiagramService;
import org.reactome.server.tools.diagram.exporter.pptx.exception.LicenseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@Component
public class DiagramExportManager {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private DiagramService diagramService;

    @Autowired
    private GeneralService generalService;

    @Autowired
    private EventsService eventsService;

    @Value("${diagram.json.folder}")
    private String diagramJsonFolder;

    @Value("${diagram.exporter.temp.folder}")
    private String diagramExporterTempFolder;

    public File toPPTX(String stId, String colorProfile, HttpServletResponse response) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramProfileException, LicenseException {
        String ancestorStId = getAncestorStId(stId);

        if (!diagramExporterTempFolder.endsWith("/")) diagramExporterTempFolder += "/";
        if (!diagramJsonFolder.endsWith("/")) diagramJsonFolder += "/";

        File outputFolder = new File(diagramExporterTempFolder + generalService.getDBVersion() + "/pptx/" + colorProfile);
        if (!outputFolder.exists()) {
            infoLogger.debug("Creating the directory tree for storing pptx files");
            if (!outputFolder.mkdirs())
                infoLogger.error("Could not create the folder for the given DBVersion and profile");
        }

        File pptxFile = new File(outputFolder.getAbsolutePath() + "/" + stId + DiagramExporterController.FILE_EXTENSION);
        if (ancestorStId != null) {
            pptxFile = new File(outputFolder.getAbsolutePath() + "/" + ancestorStId + DiagramExporterController.FILE_EXTENSION);
        }

        if (pptxFile.exists()) { // just return the file previously generated.
            infoLogger.debug("Diagram {} has been generated previously.", pptxFile.getName());
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            response.setHeader("Content-Disposition", "attachment; filename=" + pptxFile.getName());

            return pptxFile;
        } else {
            infoLogger.debug("Export Diagram {} based on StableId {}", pptxFile.getName(), stId);
            // The stId might not have diagram, then collect the json based on the ancestorStId.
            String diagramJsonFile = diagramJsonFolder + pptxFile.getName().replace(".pptx", ".json");
            File newFile = diagramService.exportToPPTX(diagramJsonFile, colorProfile, pptxFile.getAbsolutePath());
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            response.setHeader("Content-Disposition", "attachment; filename=" + newFile.getName());
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
}
