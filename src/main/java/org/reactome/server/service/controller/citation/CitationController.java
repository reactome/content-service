package org.reactome.server.service.controller.citation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.InstanceEdit;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.model.citation.Citation;
import org.reactome.server.service.model.citation.PathwayCitation;
import org.reactome.server.service.model.citation.StaticCitation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Yusra Haider (yhaider@ebi.ac.uk)
 * @since 11.02.2
 */

@ApiIgnore
@RestController
@RequestMapping("/citation")
public class CitationController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    private static final String STATIC_CITATION_ERROR = "Unable to fetch static citation";
    private static final String DOI_BASE_URL = "https://doi.org/";
    private static final String EUROPE_PMC_URL = "https://www.ebi.ac.uk/europepmc/webservices/rest/search";


    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;
    @Autowired
    private GeneralService generalService;


    // end point for getting data for citing a pathway
    @GetMapping(value = "/pathway/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PathwayCitation> pathwayCitation(@ApiParam(value = "DbId or StId of the requested database object", required = true)
                                                           @PathVariable String id) {
        return ResponseEntity.ok(getPathwayCitationObject(id));
    }


    // endpoint for getting the string citation for the download page
    @GetMapping(value = "/download", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> downloadCitation() {
        String downloadLink = "https://reactome.org/download-data/";
        return ResponseEntity.ok("\"Name of file\", Reactome, " + generalService.getDBInfo().getVersion() + ", " + downloadLink);
    }

    // end point for getting data for citing any static citation, given the PMID
    @GetMapping(value = "/static/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> staticCitation(@ApiParam(value = "PMID of the requested citation", required = true)
                                                 @PathVariable String id) {
        try {
            HttpGet request = new HttpGet(new URIBuilder(EUROPE_PMC_URL)
                    .setParameter("query", id)
                    .setParameter("format", "dc")
                    .setParameter("resultType", "core")
                    .build());

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(request)) {

                // if the EuropePMC API is down or response is not okay for whatever reason...
                if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                    return new ResponseEntity(STATIC_CITATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
                } else {
                    String xmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlResponse)));
                    return ResponseEntity.ok(doc.getElementsByTagName("dcterms:bibliographicCitation").item(0).getTextContent());
                }

            } catch (Exception e) {
                throw new Exception(e);
            }
        } catch (Exception e) {
            infoLogger.error("Exception thrown in staticCitation method", e);
            return new ResponseEntity(STATIC_CITATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(value = "/export")
    public void export(@RequestParam Boolean isPathway,
                       @RequestParam String id,
                       @RequestParam String ext,
                       @RequestParam String dateAccessed,
                       HttpServletResponse response) throws IOException {

        Citation citation;

        if (isPathway) citation = getPathwayCitationObject(id);
        else citation = getStaticCitationObject(id);

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


    private PathwayCitation getPathwayCitationObject(String id) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findEnhancedObjectById(id);
        PathwayCitation pathwayCitation = null;
        if (databaseObject instanceof Pathway) {
            Pathway p = (Pathway) databaseObject;
            pathwayCitation = new PathwayCitation(id, p.getDisplayName());
            pathwayCitation.setYear(p.getReleaseDate().substring(0, 4));
            pathwayCitation.setMonth(p.getReleaseDate().substring(6, 7));
            String doi = p.getDoi();
            List<String> urls = new ArrayList<>();
            if (doi != null && !doi.isEmpty()) {
                pathwayCitation.setDoi(doi);
                urls.add(DOI_BASE_URL + p.getDoi());
            } else urls.add("https://reactome.org" + "/content/detail/" + id);
            pathwayCitation.setUrls(urls);

            pathwayCitation.setReactomeReleaseVersion(generalService.getDBInfo().getVersion().toString());

            List<Map<String, String>> authors = null;
            List<InstanceEdit> instanceEdits = null;

            // the authors field gets populated in the order of priority as defined by
            // the if else-if conditions below
            // this is because we have pathways with missing authors, creators and reviewers
            // in case none of these are available, the `authors` field will have a null value and
            // won't show up in the response at all
            if (p.getAuthored() != null && !p.getAuthored().isEmpty()) {
                instanceEdits = p.getAuthored();
            } else if (p.getCreated() != null) {
                instanceEdits = new ArrayList<>();
                instanceEdits.add(p.getCreated());
            } else if (p.getReviewed() != null && !p.getReviewed().isEmpty()) {
                instanceEdits = p.getReviewed();
            }

            if (instanceEdits != null) {
                authors = new ArrayList<>();
                for (InstanceEdit instanceEdit : instanceEdits) {
                    for (Person person : instanceEdit.getAuthor()) {
                        HashMap<String, String> author = new HashMap<>();
                        author.put("lastName", person.getSurname());
                        author.put("initials", String.join(".", person.getInitial().split("")) + ".");
                        author.put("firstName", person.getFirstname());
                        author.put("fullName", person.getDisplayName());
                        authors.add(author);
                    }
                }
            }
            pathwayCitation.setAuthors(authors);
        }
        return pathwayCitation;
    }

    private StaticCitation getStaticCitationObject(String id) {
        try {
            HttpGet request = new HttpGet(new URIBuilder(EUROPE_PMC_URL)
                    .setParameter("query", id)
                    .setParameter("format", "json")
                    .setParameter("resultType", "core")
                    .build());

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(request)) {

                // if the EuropePMC API is down or response is not okay for whatever reason...
                if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                    return null;
                } else {
                    String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                    ObjectMapper objectMapper = new ObjectMapper();

                    Map<String, Object> map = objectMapper.convertValue(objectMapper.readTree(jsonResponse).get("resultList").get("result").get(0), new TypeReference<Map<String, Object>>() {
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
}

