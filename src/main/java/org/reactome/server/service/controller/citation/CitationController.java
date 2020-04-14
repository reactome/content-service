package org.reactome.server.service.controller.citation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.InstanceEdit;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.model.citation.Citation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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


    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;
    @Autowired
    private GeneralService generalService;

    // TO-DO: ADD THE DOI URL TO THIS
    // end point for getting data for citing a pathway
    @GetMapping(value = "/pathway/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> pathwayCitation(@ApiParam(value = "DbId or StId of the requested database object", required = true)
                                             @PathVariable String id) {

        DatabaseObject databaseObject = advancedDatabaseObjectService.findEnhancedObjectById(id);
        Map<String, Object> map = new HashMap<>();
        if (databaseObject instanceof Pathway) {
            Pathway p = (Pathway) databaseObject;
            map.put("stid", id);
            map.put("publicationYear", p.getReleaseDate().substring(0, 4));
            map.put("publicationMonth", p.getReleaseDate().substring(6, 7));
            String doi = p.getDoi();
            if(doi != null && !doi.isEmpty()) {
                map.put("doi", p.getDoi());
                map.put("doiURL", DOI_BASE_URL + p.getDoi());
            }
            map.put("pathwayTitle", p.getDisplayName());
            map.put("hasImage", p.getHasDiagram());

            List<HashMap<String, String>> authors = null;
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
            map.put("authors", authors);
            map.put("releaseVersion", generalService.getDBInfo().getVersion());
        }
        return ResponseEntity.ok(map);
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
                                                @PathVariable String id) throws Exception {
        try {
            HttpGet request = new HttpGet(new URIBuilder()
                    .setScheme("https")
                    .setHost("www.ebi.ac.uk")
                    .setPath("/europepmc/webservices/rest/search")
                    .setParameter("query", id)
                    .setParameter("format", "dc")
                    .setParameter("resultType", "core")
                    .build());

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(request)) {

                // if the EuropePMC API is down or response is not okay for whatever reason...
                if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                    return new ResponseEntity(STATIC_CITATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                else {
                    String xmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlResponse)));
                    return ResponseEntity.ok(doc.getElementsByTagName("dcterms:bibliographicCitation").item(0).getTextContent());
                }

            }
            catch (Exception e) {
                throw new Exception(e);
            }
        }
        catch (Exception e) {
            infoLogger.error("Exception thrown in staticCitation method", e);
            return new ResponseEntity(STATIC_CITATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/export")
    public void export(@RequestParam Boolean isPathway,
                                                      @RequestParam String id,
                                                      @RequestParam String ext,
                                                      HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> map;
        if(isPathway) {
            map = pathwayCitation(id).getBody();
        }
       else {
            map = getStaticCitationForExport(id);
        }

        String citationString = "";
        String filename = "reactome_citation";
        String contentType = "";

        if(map != null && !map.isEmpty()) {
            map.put("isPathway", isPathway);
            map.put("baseURL", getURLBase(request));
            Citation citation = getCitationFromMap(map);

            if(ext.equalsIgnoreCase("bib")) {
                citationString = citation.toBibTeX();
                filename += ".bib";
                contentType = "application/x-bibtex";
            }
            else if(ext.equalsIgnoreCase("ris")) {
                citationString = citation.toRIS();
                filename += ".ris";
                contentType = "application/x-research-info-systems";
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



    private Citation getCitationFromMap(Map<String, Object> map) {
        Citation citation = new Citation();
        citation.setId(map.containsKey("id") ? map.get("id").toString() : map.get("stid").toString());
        citation.setTitle(map.containsKey("title") ? (String)map.get("title") : (String)map.get("pathwayTitle"));

        Map<String, Object> journalInfo = map.containsKey("journalInfo") ? (Map<String, Object>)map.get("journalInfo") : null;
        Map<String, Object> journal = journalInfo != null && journalInfo.containsKey("journal") ? (Map<String, Object>)journalInfo.get("journal") : null;
        citation.setJournal(journal != null && journal.containsKey("title") ? journal.get("title").toString() : null);
        citation.setYear(journalInfo != null && journalInfo.containsKey("yearOfPublication") ? journalInfo.get("yearOfPublication").toString() : map.get("publicationYear").toString());
        citation.setMonth(journalInfo != null && journalInfo.containsKey("monthOfPublication") ? journalInfo.get("monthOfPublication").toString() : map.get("publicationMonth").toString());
        citation.setNumber(journalInfo != null && journalInfo.containsKey("issue") ? journalInfo.get("issue").toString() : null);
        citation.setVolume(journalInfo != null && journalInfo.containsKey("volume") ? journalInfo.get("volume").toString() : null);
        citation.setIssn(journal != null && journal.containsKey("issn") ? journal.get("issn").toString() : null);
        citation.setPages(map.containsKey("pages") ? map.get("pageInfo").toString() : null);

        List<String> urls = new ArrayList<>();
        if(map.containsKey("doi")){
            String doi = (String)map.get("doi");
            citation.setDoi(doi);
            urls.add(DOI_BASE_URL + doi);
        }
        else if(map.containsKey("stid")) {
            urls.add(map.get("baseURL") + "/content/detail/" + map.get("stid"));
        }
        citation.setUrls(urls);

        if(map.containsKey("authorList")) {
            Map<String, Object> authorList = (Map<String, Object>) map.get("authorList");
            citation.setAuthors(authorList.containsKey("author") ? (List<Map<String, String>>)authorList.get("author") : null);
        }
        else if(map.containsKey("authors")) {
            citation.setAuthors((List<Map<String, String>>)map.get("authors"));
        }

        citation.setPathway((Boolean)map.get("isPathway"));
        return citation;
    }

    // private method for getting the citation details for a static citation
    // for export purposes
    private Map<String, Object> getStaticCitationForExport(String id) {
        try {
            HttpGet request = new HttpGet(new URIBuilder()
                    .setScheme("https")
                    .setHost("www.ebi.ac.uk")
                    .setPath("/europepmc/webservices/rest/search")
                    .setParameter("query", id)
                    .setParameter("format", "json")
                    .setParameter("resultType", "core")
                    .build());

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(request)) {

                // if the EuropePMC API is down or response is not okay for whatever reason...
                if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                    return null;
                }

                else {
                    String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.convertValue(objectMapper.readTree(jsonResponse).get("resultList").get("result").get(0), new TypeReference<Map<String, Object>>(){});
                }
            }
            catch (Exception e) {
                throw new Exception(e);
            }
        }
        catch (Exception e) {
            infoLogger.error("Exception thrown in getStaticCitationForExport method", e);
            return null;
        }
    }

    // helper function
    public String getURLBase(HttpServletRequest request) throws MalformedURLException {
        URL requestURL = new URL(request.getRequestURL().toString());
        String port = requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort();
        return requestURL.getProtocol() + "://" + requestURL.getHost() + port;
    }
}
