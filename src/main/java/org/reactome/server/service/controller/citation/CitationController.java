package org.reactome.server.service.controller.citation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.model.citation.Citation;
import org.reactome.server.service.model.citation.EventCitation;
import org.reactome.server.service.model.citation.StaticCitation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/**
 * @author Yusra Haider (yhaider@ebi.ac.uk)
 * @since 11.02.2
 */

@Hidden
@RestController
@RequestMapping("/citation")
public class CitationController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    private static final String STATIC_CITATION_ERROR = "Unable to fetch static citation";
    private static final String DOI_BASE_URL = "https://doi.org/";
    private static final String EUROPE_PMC_URL = "https://www.ebi.ac.uk/europepmc/webservices/rest/search";
    private static final String WHITESPACE = " ";


    private final AdvancedDatabaseObjectService advancedDatabaseObjectService;
    private final GeneralService generalService;

    public CitationController(AdvancedDatabaseObjectService advancedDatabaseObjectService, GeneralService generalService) {
        this.advancedDatabaseObjectService = advancedDatabaseObjectService;
        this.generalService = generalService;
    }


    // end point for getting data for citing a pathway
    @GetMapping(value = "/pathway/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> pathwayCitation(@Parameter(description = "DbId or StId of the requested database object", required = true)
                                                               @PathVariable String id,
                                                               @RequestParam String dateAccessed) {
        EventCitation eventCitation = getEventCitationObject(id);
        Map<String, String> map = new HashMap<>();
        if (eventCitation == null) {
            return ResponseEntity.ok(map);
        }
        map.put("pathwayCitation", eventCitation.eventCitation(dateAccessed));
        map.put("imageCitation", eventCitation.imageCitation(dateAccessed));

        return ResponseEntity.ok(map);
    }


    // endpoint for getting the string citation for the download page
    @GetMapping(value = "/download", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> downloadCitation() {
        String downloadLink = "https://reactome.org/download-data/";
        return ResponseEntity.ok("\"Name of file\", Reactome, " + generalService.getDBInfo().getVersion() + ", " + downloadLink);
    }


    // end point for getting data for citing any static citation, given the PMID
    @GetMapping(value = "/static/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> staticCitation(@Parameter(description = "PMID of the requested citation", required = true)
                                                 @PathVariable String id) {
        StaticCitation staticCitation = getStaticCitationObject(id);
        if (staticCitation == null) {
            return new ResponseEntity<>(STATIC_CITATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(staticCitation.toText(null));
    }

    // end point for exporting citation, use this endpoint on the citation pop-up window
    @GetMapping(value = "/export")
    public void export(@RequestParam Boolean isPathway,
                       @RequestParam String id,
                       @RequestParam String ext,
                       @RequestParam String dateAccessed,
                       HttpServletResponse response) throws IOException {

        Citation citation;
        if (isPathway) citation = getEventCitationObject(id);
        else citation = getStaticCitationObject(id);
        exportCitation(id, ext, response, dateAccessed, citation);
    }

    /* end point for exporting citation with identifier and ext only, use this to export bibtex on the Person detail page
       Example: https://reactome.org/content/detail/person/0000-0002-7864-5971
     */
    @GetMapping(value = "/export/{id}")
    public void exportIdentifier(@Parameter(description = "DbId or StId of the requested database object")
                                 @PathVariable String id,
                                 @Parameter(description = "Format in which you want to download the citation")
                                 @RequestParam(name = "ext", required = false) String ext,
                                 HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException {

        LocalDate now = LocalDate.now();
        String dayAndYear = now.format(DateTimeFormatter.ofPattern("dd yyyy"));
        String month = now.getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH);
        String dayOfWeek = now.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH);
        //format example: Tue Apr 05 2022
        String dateAccessed = dayOfWeek.concat(WHITESPACE).concat(month).concat(WHITESPACE).concat(dayAndYear);

        Citation citation = getEventCitationObject(id);
        exportCitation(id, ext, response, dateAccessed, citation);
    }

    public void exportCitation(@PathVariable String id,
                               @RequestParam(name = "ext", required = false)
                               @Parameter(description = "Format in which you want to download the citation") String ext,
                               HttpServletResponse response,
                               String dateAccessed,
                               Citation citation) throws IOException {
        if (citation != null) {
            String filename = "reactome_citation_" + id;
            String contentType = "text/plain";
            String citationString;

            switch (ext) {
                case "bib":
                    citationString = citation.toBibtex(dateAccessed);
                    filename += ".bib";
                    contentType = "application/x-bibtex";
                    break;
                case "ris":
                    citationString = citation.toRIS(dateAccessed);
                    filename += ".ris";
                    contentType = "application/x-research-info-systems";
                    break;
                default:
                    citationString = citation.toText(dateAccessed);
                    filename += ".txt";
            }

            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            InputStream in = new ByteArrayInputStream(citationString.getBytes(StandardCharsets.UTF_8));
            OutputStream out = response.getOutputStream();
            IOUtils.copy(in, out);
            out.flush();
            out.close();
            in.close();
        }
    }

    private EventCitation getEventCitationObject(String id) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findEnhancedObjectById(id);
        EventCitation eventCitation = null;
        if (databaseObject instanceof Event) {
            Event event = (Event) databaseObject;
            eventCitation = new EventCitation(id, event.getDisplayName());
            eventCitation.setYear(event.getReleaseDate().substring(0, 4));
            eventCitation.setMonth(event.getReleaseDate().substring(6, 7));

            List<String> urls = new ArrayList<>();
            boolean hasDOI = false;
            if (event instanceof Pathway) {
                String doi = ((Pathway) event).getDoi();
                if (doi != null && !doi.isEmpty()) {
                    eventCitation.setDoi(doi);
                    urls.add(DOI_BASE_URL + doi);
                    hasDOI = true;
                }
            }

            if (!hasDOI) urls.add("https://reactome.org" + "/content/detail/" + id);

            eventCitation.setUrls(urls);
            eventCitation.setReactomeReleaseVersion(generalService.getDBInfo().getVersion().toString());

            List<Map<String, String>> authors = null;
            List<InstanceEdit> instanceEdits = null;

            // the authors field gets populated in the order of priority as defined by
            // the if else-if conditions below
            // this is because we have pathways with missing authors, creators and reviewers
            // in case none of these are available, the `authors` field will have a null value and
            // won't show up in the response at all
            if (event.getAuthored() != null && !event.getAuthored().isEmpty()) {
                instanceEdits = event.getAuthored();
            } else if (event.getCreated() != null) {
                instanceEdits = new ArrayList<>();
                instanceEdits.add(event.getCreated());
            } else if (event.getReviewed() != null && !event.getReviewed().isEmpty()) {
                instanceEdits = event.getReviewed();
            }

            if (instanceEdits != null) {
                authors = new ArrayList<>();
                for (InstanceEdit instanceEdit : instanceEdits) {
                    for (Person person : instanceEdit.getAuthor()) {
                        HashMap<String, String> author = new HashMap<>();
                        author.put("lastName", person.getSurname());
                        author.put("initials", String.join(".", person.getInitial().split("")) + ".");
                        author.put("firstName", person.getFirstname());
                        authors.add(author);
                    }
                }
            }
            eventCitation.setAuthors(authors);
        }
        return eventCitation;
    }

    private StaticCitation getStaticCitationObject(String id) {
        try {
            HttpGet request = new HttpGet(new URIBuilder(EUROPE_PMC_URL)
                    .setParameter("query", "ext_id:" + id)
                    .setParameter("format", "json")
                    .setParameter("resultType", "core")
                    .build());
            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(getAllSSLContext())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
                 CloseableHttpResponse response = httpClient.execute(request)) {

                // if the EuropePMC API is down or response is not okay for whatever reason...
                if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                    return null;
                } else {
                    String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                    ObjectMapper objectMapper = new ObjectMapper();

                    Map<String, Object> map = objectMapper.convertValue(objectMapper.readTree(jsonResponse).get("resultList").get("result").get(0), new TypeReference<>() {
                    });
                    Map<String, Object> journalInfo = map.containsKey("journalInfo") ? (Map<String, Object>) map.get("journalInfo") : null;
                    Map<String, Object> journal = journalInfo != null && journalInfo.containsKey("journal") ? (Map<String, Object>) journalInfo.get("journal") : null;

                    StaticCitation staticCitation = new StaticCitation(map.get("id").toString(), map.get("title").toString());
                    if (map.containsKey("pageInfo")) staticCitation.setPages(map.get("pageInfo").toString());
                    if (map.containsKey("pmid")) staticCitation.setPmid(map.get("pmid").toString());
                    if (map.containsKey("pmcid")) staticCitation.setPmcid(map.get("pmcid").toString());

                    if (journal != null && journal.containsKey("title"))
                        staticCitation.setJournal(journal.get("title").toString());
                    if (journalInfo != null && journalInfo.containsKey("yearOfPublication"))
                        staticCitation.setYear(journalInfo.get("yearOfPublication").toString());
                    if (journalInfo != null && journalInfo.containsKey("monthOfPublication"))
                        staticCitation.setMonth(journalInfo.get("monthOfPublication").toString());
                    if (journalInfo != null && journalInfo.containsKey("issue"))
                        staticCitation.setNumber(journalInfo.get("issue").toString());
                    if (journalInfo != null && journalInfo.containsKey("volume"))
                        staticCitation.setVolume(journalInfo.get("volume").toString());
                    if (journal != null && journal.containsKey("issn"))
                        staticCitation.setIssn(journal.get("issn").toString());

                    if (map.containsKey("doi") && map.get("doi") != null) {
                        String doi = map.get("doi").toString();
                        staticCitation.setDoi(doi);
                        staticCitation.setUrls(new ArrayList<>(Arrays.asList(DOI_BASE_URL + doi)));
                    }

                    if (map.containsKey("authorList")) {
                        Map<String, Object> authorList = (Map<String, Object>) map.get("authorList");
                        if (authorList.containsKey("author"))
                            staticCitation.setAuthors((List<Map<String, String>>) authorList.get("author"));
                    }

                    return staticCitation;
                }
            } catch (Exception e) {
                throw new Exception(e);
            }
        } catch (Exception e) {
            infoLogger.error("Exception thrown in getStaticCitationObject method", e);
            return null;
        }
    }

    // code taken from: https://www.javacodemonk.com/disable-ssl-certificate-check-resttemplate-e2c53583
    private SSLContext getAllSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }
}


