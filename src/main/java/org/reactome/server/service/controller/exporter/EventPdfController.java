package org.reactome.server.service.controller.exporter;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.service.exception.BadRequestException;
import org.reactome.server.tools.event.exporter.DocumentArgs;
import org.reactome.server.tools.event.exporter.EventExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 * @author Lorente-Arencibia, Pascual (plorente@ebi.ac.uk)
 */
@RestController
@Api(tags = "exporter", description = "Reactome Data: Format Exporter")
@RequestMapping("/exporter")
public class EventPdfController {

    private static final Object REPORT_SEMAPHORE = new Object();
    private static long REPORT_COUNT = 0L;
    private static final int ALLOWED_CONCURRENT_REPORTS = 2;
    private static final int MAX_LEVEL = 1;

    private static final Logger logger = LoggerFactory.getLogger("infoLogger");

    @Value("${report.user:default}")
    private String reportUser;
    @Value("${report.password:default}")
    private String reportPassword;

    private DatabaseObjectService dos;
    private TokenUtils tokenUtils;
    private EventExporter eventExporter;

    @ApiOperation(
            value = "Exports the content of a given event (pathway or reaction) to a PDF document",
            notes = "This method accepts identifiers for <a href=\"/content/schema/Event\" target=\"_blank\">Event class</a> instances." +
                    "<br/>The generated document contains the details for the given event and, optionally, its children (see level parameter). These details include:" +
                    "<br/> - A diagram image" +
                    "<br/> - Summation" +
                    "<br/> - Literature references" +
                    "<br/> - Edit history" +
                    "<br/> - Other details: type, location, compartments, diseases" +
                    "<br/><br/>Documents can also be overlaid with <a href='/dev/analysis' target=\"_blank\">pathway analysis results</a>",
            produces = "application/pdf"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "Stable Identifier does not match with any of the available diagrams."),
            @ApiResponse(code = 500, message = "Could not deserialize diagram file."),
            @ApiResponse(code = 503, message = "Service was unable to export to Power Point.")
    })
    @RequestMapping(value = "/document/event/{identifier}.pdf", method = RequestMethod.GET)
    public void eventPdf(@ApiParam(value = "Event identifier (it can be a pathway with diagram, a subpathway or a reaction)", required = true, defaultValue = "R-HSA-177929")
                        @PathVariable String identifier,

                         @ApiParam(value = "Number of levels to explore down in the pathways hierarchy [0 - 1]", defaultValue = "1")
                        @RequestParam(value = "level [0 - 1]", required = false, defaultValue = "1") Integer level,
                         @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                        @RequestParam(value = "diagramProfile", defaultValue = "Modern", required = false) String diagramProfile,
                         @ApiParam(value = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> token with the results to be overlaid on top of the given diagram")
                        @RequestParam(value = "token", required = false) String token,
                         @ApiParam(value = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> resource for which the results will be overlaid on top of the given pathways overview")
                        @RequestParam(value = "resource", required = false, defaultValue = "total") String resource,
                         @ApiParam(value = "Expression column. When the token is associated to an expression analysis, this parameter allows specifying the expression column for the overlay")
                        @RequestParam(value = "expColumn", required = false) Integer expColumn,
                         @ApiParam(value = "Analysis  Color Profile", defaultValue = "Standard", allowableValues = "Standard, Strosobar, Copper%20Plus")
                        @RequestParam(value = "analysisProfile", defaultValue = "Standard", required = false) String analysisProfile,

                         HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {

        long waitingTime = 0L;
        synchronized (REPORT_SEMAPHORE) {
            if (++REPORT_COUNT > ALLOWED_CONCURRENT_REPORTS) {
                long waitStart = System.currentTimeMillis();
                REPORT_SEMAPHORE.wait();
                waitingTime = System.currentTimeMillis() - waitStart;
            }
        }

        Event event;
        try {
            event = dos.findById(identifier);
        } catch (ClassCastException ex) {
            throw new BadRequestException(String.format("'%s' is not an event", identifier));
        }

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

        try {
            long reportStart = System.currentTimeMillis();
            response.addHeader("Content-Type", "application/pdf");
            int pages = eventExporter.export(args, analysisResult, response.getOutputStream());
            Long reportTime = System.currentTimeMillis() - reportStart;
            Map<String, String> map = getReportInformation(request);
            doAsyncSearchReport(map.get("ip-address"), waitingTime, reportTime, pages, map.get("user-agent"));
        } catch (RuntimeException ex) {
            logger.error("Could not generate PDF document for " + event.getStId(), ex);
        } finally {
            synchronized (REPORT_SEMAPHORE) {
                REPORT_COUNT--;
                REPORT_SEMAPHORE.notify();
            }
        }
    }

    private int getLevel(int level){
        if (level < 0) return 0;
        if (level > MAX_LEVEL) return MAX_LEVEL;
        return level;
    }

    private void doAsyncSearchReport(String ip, Long waitingTime, Long reportTime, Integer pages, String userAgent) {
        new Thread(() -> report(ip, waitingTime, reportTime, pages, userAgent), "EventPDFWaitingReportThread").start();
    }

    /**
     * #Custom header added to propagate the request protocol when ProxyPass
     * RequestHeader set supports-ssl "true"
     *
     * @param request the request object as provided
     * @return the name of the server with its corresponding protocol
     */
    private String getServerName(HttpServletRequest request){
        String rtn;
        try {
            Boolean supportsSSL = Boolean.valueOf(request.getHeader("supports-ssl"));
            URL url = new URL(request.getRequestURL().toString());
            String protocol = url.getProtocol();
            if(supportsSSL && !protocol.endsWith("s")) protocol += "s";
            rtn = protocol + "://" + url.getHost();
        } catch (MalformedURLException e) {
            rtn = null;
        }
        return rtn;
    }

    private void report(String ip, Long waitingTime, Long reportTime, Integer pages, String userAgent) {
        try {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(reportUser, reportPassword);
            provider.setCredentials(AuthScope.ANY, credentials);
            CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
            URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/report/event/pdf/waiting");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("ip", ip));
            params.add(new BasicNameValuePair("waitingTime", String.valueOf(waitingTime)));
            params.add(new BasicNameValuePair("reportTime", String.valueOf(reportTime)));
            params.add(new BasicNameValuePair("pages", String.valueOf(pages)));
            params.add(new BasicNameValuePair("agent", userAgent));
            uriBuilder.addParameters(params);

            HttpGet httpGet = new HttpGet(uriBuilder.toString());
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.error("[REP001] The url {} returned the code {} and the report hasn't been created.", uriBuilder.toString(), statusCode);
            }
            client.close();
        } catch (ConnectException e) {
            logger.error("[REP002] Report service is unavailable");
        } catch (IOException | URISyntaxException e) {
            logger.error("[REP003] An unexpected error has occurred when saving a report");
        }
    }

    /**
     * Extra information to be sent to report service in order to store potential target
     */
    private Map<String, String> getReportInformation(HttpServletRequest request) {
        if (request == null) return null;

        Map<String, String> result = new HashMap<>();
        result.put("user-agent", request.getHeader("User-Agent"));
        String remoteAddr = request.getHeader("X-FORWARDED-FOR"); // Client IP
        if (!StringUtils.isEmpty(remoteAddr)) {
            // The general format of the field is: X-Forwarded-For: client, proxy1, proxy2 ... we only want the client
            remoteAddr = new StringTokenizer(remoteAddr, ",").nextToken().trim();
        } else {
            remoteAddr = request.getRemoteAddr();
        }

        result.put("ip-address", remoteAddr);
        return result;
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
}
