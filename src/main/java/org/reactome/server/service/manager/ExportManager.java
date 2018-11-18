package org.reactome.server.service.manager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.EventsService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.controller.exporter.ExporterController;
import org.reactome.server.service.exception.MissingSBMLException;
import org.reactome.server.tools.diagram.data.DiagramFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.common.Decorator;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.common.profiles.service.DiagramExporterService;
import org.reactome.server.tools.reaction.exporter.ReactionExporter;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@Component
public class ExportManager {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    private static final Logger errorLogger = LoggerFactory.getLogger("errorLogger");

    private DiagramExporterService diagramExporterService = new DiagramExporterService();
    private GeneralService generalService;
    private EventsService eventsService;
    private DatabaseObjectService databaseObjectService;
    private ExportManager exportManager;

    @Value("${diagram.json.folder}")
    private String diagramJsonFolder;

    @Value("${diagram.exporter.temp.folder}")
    private String diagramExporterTempFolder;

    private ReactionExporter reactionExporter;

    //The reaction will be layed out from the graph database only when object is an instance of 'ReactionLikeEvent'.
    //In any other case, an existing diagram json will be retrieved and converted to PPTX with the original requirements.
    public File getPPTX(Object obj, String colorProfile, Decorator decorator, HttpServletResponse response) throws DiagramJsonNotFoundException, DiagramJsonDeserializationException, DiagramProfileException {
        if (!diagramExporterTempFolder.endsWith("/")) diagramExporterTempFolder += "/";

        String stId;
        String ancestorStId = null;
        if (obj instanceof ReactionLikeEvent) {
            stId = ((ReactionLikeEvent) obj).getStId();
        } else if (obj instanceof String) {
            stId = (String) obj;
            ancestorStId = getAncestorStId(stId);
        } else throw new RuntimeException();

        File outputFolder = new File(diagramExporterTempFolder + generalService.getDBInfo().getVersion() + "/pptx/" + colorProfile.toLowerCase());
        if (!outputFolder.exists()) {
            infoLogger.debug("Creating the directory tree for storing pptx files");
            if (!outputFolder.mkdirs())
                infoLogger.error("Could not create the folder for the given DBVersion and profile");
        }

        String fileName = (ancestorStId != null ? ancestorStId : stId) + ExporterController.PPT_FILE_EXTENSION;
        File pptxFile = new File(outputFolder.getAbsolutePath() + "/" + fileName);

        // We don't want to cache neither read from cache if the diagram has selection and flags.
        //boolean isDecorated = ((flags != null && !flags.isEmpty()) || (selected != null && !selected.isEmpty()));

        // The pptx is save in the temp folder using only the StId, then when we write in the response header
        // we rename it using the [stId] displayName
        String pptxFileName = getPPTXFileName(stId);
        if (pptxFile.exists() && !decorator.isDecorated()) { // just return the file previously generated.
            infoLogger.debug("Diagram {} has been generated previously.", pptxFile.getName());
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + pptxFileName + "\"");
            return pptxFile;
        } else {
            infoLogger.debug("Export Diagram {} based on StableId {}", pptxFile.getName(), stId);
            File newFile;
            if(obj instanceof ReactionLikeEvent) { //We do not do it above to avoid creating the diagram object when no necessary
                Diagram diagram = exportManager.getDiagram((ReactionLikeEvent) obj);
                newFile = diagramExporterService.exportToPPTX(diagram, colorProfile, outputFolder.getPath(), decorator);
            } else {
                newFile = diagramExporterService.exportToPPTX(stId, diagramJsonFolder, colorProfile, outputFolder.getPath(), decorator);
            }
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
    /* ----------------- SBML and SBGN EXPORTER ------------------ */
    /* =========================================================== */



    public File getCachedFile(Event event, String sbmlFileName) throws MissingSBMLException {
        if (!diagramExporterTempFolder.endsWith("/")) diagramExporterTempFolder += "/";

        // This folder will be created during release phase, double checking just in case
        File outputFolder = new File(diagramExporterTempFolder + generalService.getDBInfo().getVersion() + "/sbml");
        if (outputFolder.exists()) {
            File sbml = new File(outputFolder.getAbsolutePath() + "/" + sbmlFileName);
            if (sbml.exists()) return sbml;
        }

        throw new MissingSBMLException(String.format("'%s' file has not been previously generated for '%s'", sbmlFileName, event.getStId()));
    }

    public InputStream saveSBML(String sbml, String sbmlFileName) throws FileNotFoundException {
        File outputFolder = new File(diagramExporterTempFolder + generalService.getDBInfo().getVersion() + "/sbml");
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
            errorLogger.error("Trying to write a SBML file that already exists");
        }
        return new FileInputStream(file);
    }

    public InputStream saveSBGN(Sbgn sbgn, String sgbnFileName) throws FileNotFoundException {
        File outputFolder = new File(diagramExporterTempFolder + generalService.getDBInfo().getVersion() + "/sbgn");
        if (!outputFolder.exists()) {
            infoLogger.debug("Creating the directory tree for storing SBGN files");
            if (!outputFolder.mkdirs())
                infoLogger.error("Could not create the folder for the given DBVersion");
        }

        File file = new File(outputFolder.getAbsolutePath() + "/" + sgbnFileName);
        if (!file.exists()) {
            try {
                SbgnUtil.writeToFile(sbgn, file);
            } catch (JAXBException e) {
                errorLogger.error(e.getMessage());
            }
        } else {
            errorLogger.error("Trying to write a SBGN file that already exists");
        }
        return new FileInputStream(file);
    }

    public Diagram getDiagram(Event event) {
        if (event instanceof Pathway) {
            Pathway pathway = (Pathway) event;
            String diagram = pathway.getHasDiagram() ? pathway.getStId() : getAncestorStId(pathway.getStId());
            try {
                File aux = new File(diagramJsonFolder + "/" + diagram + ".json");
                String json = IOUtils.toString(new FileInputStream(aux), Charset.defaultCharset());
                return DiagramFactory.getDiagram(json);
            } catch (IOException | DeserializationException e) {
                errorLogger.error(e.getMessage(), e);
                return null;
            }
        } else {
            ReactionLikeEvent rle = (ReactionLikeEvent) event;
            return reactionExporter.getReactionDiagram(reactionExporter.getReactionLayout(rle));
        }
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

    @Autowired
    public void setExportManager(ExportManager exportManager) {
        this.exportManager = exportManager;
    }

    @Autowired
    public void setReactionExporter(ReactionExporter reactionExporter) {
        this.reactionExporter = reactionExporter;
    }
}
