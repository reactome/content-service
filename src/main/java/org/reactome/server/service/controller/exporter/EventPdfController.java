package org.reactome.server.service.controller.exporter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.service.exception.BadRequestException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.tools.event.exporter.DocumentArgs;
import org.reactome.server.tools.event.exporter.EventExporter;
import org.reactome.server.tools.reaction.exporter.compartment.ReactomeCompartmentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 * @author Lorente-Arencibia, Pascual (plorente@ebi.ac.uk)
 */
@RestController
@Tag(name = "exporter")
@RequestMapping("/exporter")
public class EventPdfController {

    private static final Object REPORT_SEMAPHORE = new Object();
    private static long REPORT_COUNT = 0L;
    private static final int ALLOWED_CONCURRENT_REPORTS = 4;
    private static final int MAX_LEVEL = 1;

    private static final Logger logger = LoggerFactory.getLogger("infoLogger");

    @Value("${report.user:default}")
    private String reportUser;
    @Value("${report.password:default}")
    private String reportPassword;

    private DatabaseObjectService dos;
    private TokenUtils tokenUtils;
    private EventExporter eventExporter;

    @Operation(
            summary = "Exports the content of a given event (pathway or reaction) to a PDF document",
            description = "This method accepts identifiers for <a href=\"/content/schema/Event\" target=\"_blank\">Event class</a> instances." +
                    "<br/>The generated document contains the details for the given event and, optionally, its children (see level parameter). These details include:" +
                    "<br/> - A diagram image" +
                    "<br/> - Summation" +
                    "<br/> - Literature references" +
                    "<br/> - Edit history" +
                    "<br/> - Other details: type, location, compartments, diseases" +
                    "<br/><br/>Documents can also be overlaid with <a href='/dev/analysis' target=\"_blank\">pathway analysis results</a>"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Stable Identifier does not match with any of the available diagrams."),
            @ApiResponse(responseCode = "500", description = "Could not deserialize diagram file."),
            @ApiResponse(responseCode = "503", description = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/document/event/{identifier}.pdf", method = RequestMethod.GET, produces = "application/pdf")
    public void eventPdf(@Parameter(description = "Event identifier (it can be a pathway with diagram, a subpathway or a reaction)", required = true, example = "R-HSA-177929")
                         @PathVariable String identifier,

                         @Parameter(description = "Number of levels to explore down in the pathways hierarchy [0 - 1]", example = "1")
                         @RequestParam(value = "level [0 - 1]", required = false, defaultValue = "1") Integer level,
                         @Parameter(description = "Diagram Color Profile", example = "Modern", schema = @Schema(allowableValues = {"Modern", "Standard"}))
                         @RequestParam(value = "diagramProfile", defaultValue = "Modern", required = false) String diagramProfile,
                         @Parameter(description = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> token with the results to be overlaid on top of the given diagram", example = "MjAyMjAzMDkwODU0NTlfMTU2ND")
                         @RequestParam(value = "token", required = false) String token,
                         @Parameter(description = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> resource for which the results will be overlaid on top of the given pathways overview")
                         @RequestParam(value = "resource", required = false, defaultValue = "TOTAL") String resource,
                         @Parameter(description = "Expression column. When the token is associated to an expression analysis, this parameter allows specifying the expression column for the overlay")
                         @RequestParam(value = "expColumn", required = false) Integer expColumn,
                         @Parameter(description = "Analysis  Color Profile", example = "Standard",schema = @Schema(allowableValues = {"Standard", "Strosobar", "Copper%20Plus"}))
                         @RequestParam(value = "analysisProfile", defaultValue = "Standard", required = false) String analysisProfile,

                         HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {

        synchronized (REPORT_SEMAPHORE) {
            if (++REPORT_COUNT > ALLOWED_CONCURRENT_REPORTS) {
                REPORT_SEMAPHORE.wait();
            }
        }

        Event event;
        try {
            event = dos.findById(identifier);
            if (event == null) throw new NotFoundException(String.format("'%s' couldn't be found.", identifier));

            DocumentArgs args = new DocumentArgs(event.getStId())
                    .setServerName(getServerName(request))
                    .setDiagramProfile(diagramProfile)
                    .setSpecies(event.getSpecies().get(0).getDbId())
                    .setMaxLevel(getLevel(level))
                    .setResource(resource)
                    .setAnalysisProfile(analysisProfile)
                    .setExpressionColumn(expColumn);

            AnalysisStoredResult analysisResult = null;
            if (token != null) {
                analysisResult = tokenUtils.getFromToken(token);
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + event.getStId() + "\".pdf");
            eventExporter.export(args, analysisResult, response.getOutputStream());

        } catch (NotFoundException ex) {
            throw new NotFoundException(String.format("'%s' does not exist", identifier));
        } catch (ClassCastException ex) {
            throw new BadRequestException(String.format("'%s' is not an event", identifier));
        } catch (RuntimeException ex) {
            logger.error("Could not generate PDF document for " + identifier, ex);
        } finally {
            synchronized (REPORT_SEMAPHORE) {
                REPORT_COUNT--;
                REPORT_SEMAPHORE.notify();
            }
        }
    }

    private int getLevel(int level) {
        if (level < 0) return 0;
        if (level > MAX_LEVEL) return MAX_LEVEL;
        return level;
    }

    /**
     * #Custom header added to propagate the request protocol when ProxyPass
     * RequestHeader set supports-ssl "true"
     *
     * @param request the request object as provided
     * @return the name of the server with its corresponding protocol
     */
    private String getServerName(HttpServletRequest request) {
        String rtn;
        try {
            Boolean supportsSSL = Boolean.valueOf(request.getHeader("supports-ssl"));
            URL url = new URL(request.getRequestURL().toString());
            String protocol = url.getProtocol();
            if (supportsSSL && !protocol.endsWith("s")) protocol += "s";
            rtn = protocol + "://" + url.getHost();
        } catch (MalformedURLException e) {
            rtn = null;
        }
        return rtn;
    }

    @Autowired
    public void setDos(DatabaseObjectService dos) {
        this.dos = dos;
    }

    @Autowired
    public void setTokenUtils(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    @Autowired
    public void setEventExporter(EventExporter eventExporter) {
        this.eventExporter = eventExporter;
    }

    @Autowired
    public void setSchemaService(SchemaService schemaService) {
        ReactomeCompartmentFactory.setSchemaService(schemaService);
    }
}
