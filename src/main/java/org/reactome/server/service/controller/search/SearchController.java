package org.reactome.server.service.controller.search;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.service.SpeciesService;
import org.reactome.server.graph.service.util.DatabaseObjectUtils;
import org.reactome.server.search.domain.*;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.search.service.SearchService;
import org.reactome.server.service.exception.BadRequestException;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.exception.NoResultsFoundException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.manager.SearchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@SuppressWarnings("unused")
@RestController
@Api(tags = "search", description = "Reactome Search")
@RequestMapping("/search")
class SearchController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private final Integer releaseNumber;

    private SearchService searchService;
    private SearchManager searchManager;
    private SpeciesService speciesService;
    private DatabaseObjectService dos;

    @Autowired
    public SearchController(GeneralService generalService) {
        releaseNumber = generalService.getDBInfo().getVersion();
    }

    @ApiOperation(value = "Spell-check suggestions for a given query", notes = "This method retrieves a list of spell-check suggestions for a given search term.", response = String.class, responseContainer = "List", produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Error in SolR", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/spellcheck", method = RequestMethod.GET)
    @ResponseBody
    public List<String> spellcheckerSuggestions(@ApiParam(value = "Search term", defaultValue = "repoduction", required = true) @RequestParam String query) throws SolrSearcherException {
        infoLogger.info("Request for spellcheck suggestions for query {}", query);
        return searchService.getSpellcheckSuggestions(query);
    }

    @ApiOperation(value = "Auto-suggestions for a given query", notes = "This method retrieves a list of suggestions for a given search term.", response = String.class, responseContainer = "List", produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Error in SolR", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    @ResponseBody
    public List<String> suggesterSuggestions(@ApiParam(value = "Search term", defaultValue = "platele", required = true) @RequestParam String query) throws SolrSearcherException {
        infoLogger.info("Request for autocomplete suggestions for query {}", query);
        return searchService.getAutocompleteSuggestions(query);
    }

    @ApiOperation(value = "A list of facets corresponding to the whole Reactome search data", notes = "This method retrieves faceting information on the whole Reactome search data.", response = FacetMapping.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Error in SolR", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/facet", method = RequestMethod.GET)
    @ResponseBody
    public FacetMapping facet() throws SolrSearcherException {
        infoLogger.info("Request for faceting information of all Reactome data");
        return searchService.getTotalFacetingInformation();
    }

    @ApiOperation(value = "A list of facets corresponding to a specific query", notes = "This method retrieves faceting information on a specific query", response = FacetMapping.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Error in SolR", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/facet_query", method = RequestMethod.GET)
    @ResponseBody
    public FacetMapping facet_type(@ApiParam(value = "Search term", defaultValue = "TP53", required = true) @RequestParam String query,
                                   @ApiParam(value = "Species name") @RequestParam(required = false) List<String> species, // default value isn't supported by Swagger.
                                   @ApiParam(value = "Types to filter") @RequestParam(required = false) List<String> types,
                                   @ApiParam(value = "Compartments to filter") @RequestParam(required = false) List<String> compartments,
                                   @ApiParam(value = "Keywords") @RequestParam(required = false) List<String> keywords) throws SolrSearcherException {
        infoLogger.info("Request for faceting information for query: {}", query);
        Query queryObject = new Query(query, species, types, compartments, keywords);
        return searchService.getFacetingInformation(queryObject);
    }

    @ApiOperation(value = "Queries Solr against the Reactome knowledgebase", notes = "This method performs a Solr query on the Reactome knowledgebase. Results can be provided in a paginated format.", response = GroupedResult.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Entry not found. Targets inform if the term is our scope of annotation", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Error in SolR", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public GroupedResult getResult(@ApiParam(value = "Search term", defaultValue = "Biological oxidations", required = true) @RequestParam String query,
                                   @ApiParam(value = "Species name") @RequestParam(required = false) List<String> species, // default value isn't supported by Swagger.
                                   @ApiParam(value = "Types to filter") @RequestParam(required = false) List<String> types,
                                   @ApiParam(value = "Compartments to filter") @RequestParam(required = false) List<String> compartments,
                                   @ApiParam(value = "Keywords") @RequestParam(required = false) List<String> keywords,
                                   @ApiParam(value = "Cluster results", defaultValue = "true") @RequestParam(required = false, defaultValue = "true") Boolean cluster,
                                   @ApiParam(value = "Start row") @RequestParam(value = "Start row", required = false) Integer start,
                                   @ApiParam(value = "Number of rows to include") @RequestParam(required = false) Integer rows,
                                   HttpServletRequest request) throws SolrSearcherException {
        infoLogger.info("Search request for query: {}", query);
        Query queryObject = new Query(query, "", species, types, compartments, keywords, start, rows, getReportInformation(request));
        GroupedResult result = searchService.getEntries(queryObject, cluster);
        if (result == null || result.getResults() == null || result.getResults().isEmpty()) {
            Set<TargetResult> targets = null;
            if (result != null && result.getTargetResults() != null && !result.getTargetResults().isEmpty()) {
                targets = result.getTargetResults();
            }
            throw new NoResultsFoundException("No entries found for query: " + query, targets);
        }
        return result;
    }

    @ApiOperation(value = "Performs a Solr query (fireworks widget scoped) for a given QueryObject", produces = "application/json")
    @RequestMapping(value = "/fireworks", method = RequestMethod.GET)
    @ResponseBody
    public FireworksResult getFireworksResult(@ApiParam(defaultValue = "BRAF", required = true) @RequestParam String query,
                                              @ApiParam(value = "Species name", defaultValue = "Homo sapiens") @RequestParam(required = false, defaultValue = "Homo sapiens") String species, // default value isn't supported by Swagger.
                                              @ApiParam(value = "Types to filter") @RequestParam(required = false) List<String> types,
                                              @ApiParam(value = "Start row") @RequestParam(required = false) Integer start,
                                              @ApiParam(value = "Number of rows to include") @RequestParam(required = false) Integer rows,
                                              HttpServletRequest request) throws SolrSearcherException {
        infoLogger.info("Fireworks request for query: {}", query);
        List<String> speciess = new ArrayList<>(); speciess.add(species);
        Query queryObject = new Query(query, "", speciess, types, null, null, start, rows, getReportInformation(request));
        FireworksResult fireworksResult = searchService.getFireworks(queryObject);
        if (fireworksResult == null || fireworksResult.getFound() == 0) {
            Set<TargetResult> targets = null;
            if (fireworksResult != null && fireworksResult.getTargetResults() != null && !fireworksResult.getTargetResults().isEmpty()) {
                targets = fireworksResult.getTargetResults();
            }
            throw new NoResultsFoundException("No entries found for query: " + query, targets);
        }
        return fireworksResult;
    }

    @ApiOperation(value = "Performs a Solr query (fireworks widget scoped) for a given QueryObject", produces = "application/json")
    @RequestMapping(value = "/fireworks/flag", method = RequestMethod.GET)
    @ResponseBody
    public Collection<String> fireworksFlagging(@ApiParam(defaultValue = "KNTC1", required = true) @RequestParam String query,
                                                @RequestParam(required = false, defaultValue = "Homo sapiens") String species) throws SolrSearcherException {
        infoLogger.info("Fireworks Flagging request for query: {}", query);
        Species sp = speciesService.getSpeciesByName(species);
        if (sp == null) throw new BadRequestException("No species found for '" + species + "'");

        List<String> speciess = new ArrayList<>(); speciess.add(species);
        Query queryObject = new Query(query, speciess, null, null, null);
        //Filter the result by species. This is necessary when the query term is a chemical
        String prefix = "R-" + sp.getAbbreviation();
        Collection<String> rtn = searchService.fireworksFlagging(queryObject).stream()
                .filter(s -> s.startsWith(prefix))
                .collect(Collectors.toList());
        if (rtn.isEmpty()) throw new NotFoundException("No entries found for query: '" + query + "' in species '" + species + "'");
        return rtn;
    }

    @ApiOperation(value = "Performs a Solr query (diagram widget scoped) for a given QueryObject", produces = "application/json")
    @RequestMapping(value = "/diagram/{diagram}", method = RequestMethod.GET)
    @ResponseBody
    public DiagramResult getDiagramResult(@ApiParam(defaultValue = "R-HSA-8848021", required = true) @PathVariable String diagram,
                                          @ApiParam(defaultValue = "MAD", required = true) @RequestParam String query,
                                          @ApiParam(value = "Types to filter") @RequestParam(required = false) List<String> types,
                                          @ApiParam(value = "Start row") @RequestParam(required = false) Integer start,
                                          @ApiParam(value = "Number of rows to include") @RequestParam(required = false) Integer rows) throws SolrSearcherException {
        checkDiagramIdentifier(diagram);
        Query queryObject = new Query(query, diagram, null, types, null, null, start, rows);
        DiagramResult rtn = searchService.getDiagrams(queryObject);
        if (rtn == null || rtn.getFound() == 0) throw new NotFoundException(String.format("No entries found for '%s' in diagram '%s'", query, diagram));
        return rtn;
    }

    @ApiOperation(value = "Performs a Solr query (diagram widget scoped) for a given QueryObject", produces = "application/json")
    @RequestMapping(value = "/diagram/{diagram}/occurrences/{instance}", method = RequestMethod.GET)
    @ResponseBody
    public DiagramOccurrencesResult getDiagramOccurrences(@ApiParam(defaultValue = "R-HSA-68886", required = true) @PathVariable String diagram,
                                                          @ApiParam(defaultValue = "R-HSA-141433", required = true) @PathVariable String instance,
                                                          @ApiParam(value = "Types to filter")@RequestParam(required = false) List<String> types) throws SolrSearcherException {
        checkIdentifiers(diagram, instance);
        Query queryObject = new Query(instance, diagram, null, types, null, null);
        DiagramOccurrencesResult rtn = searchService.getDiagramOccurrencesResult(queryObject);
        if (rtn == null) throw new NotFoundException(String.format("No occurrences of '%s' found in '%s'", instance, diagram));
        return rtn;
    }

    @ApiOperation(value = "A list of diagram entities plus pathways from the provided list containing the specified identifier", notes = "This method traverses the content and checks not only for the main identifier but also for all the cross-references to find the flag targets")
    @RequestMapping(value = "/diagram/{pathwayId}/flag", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DiagramOccurrencesResult getEntitiesInDiagramForIdentifier(@ApiParam(value = "The pathway to find items to flag", defaultValue = "R-HSA-446203")
                                                                    @PathVariable String pathwayId,
                                                                    @ApiParam(value = "The identifier for the elements to be flagged", defaultValue = "CTSA")
                                                                    @RequestParam String query) throws SolrSearcherException {
        checkDiagramIdentifier(pathwayId);
        DiagramOccurrencesResult rtn = searchManager.getDiagramOccurrencesResult(pathwayId, query);
        infoLogger.info("Request for all entities in diagram with identifier: {}", query);
        if (rtn.isEmpty()) throw new NotFoundException("No entities with identifier '" + query + "' found for " + pathwayId);
        return rtn;
    }

    @ApiIgnore
    @ApiOperation(value = "", response = DiagramSearchSummary.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Error in SolR", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/diagram/summary", method = RequestMethod.GET)
    @ResponseBody
    public DiagramSearchSummary diagramSearchSummary(@ApiParam(value = "Search term", defaultValue = "KIF", required = true)
                                                     @RequestParam String query,
                                                     @ApiParam(value = "Species name", defaultValue = "Homo sapiens", required = true)
                                                     @RequestParam(required = false, defaultValue = "Homo sapiens") String species,
                                                     @ApiParam(value = "Diagram", defaultValue = "R-HSA-8848021", required = true)
                                                     @RequestParam String diagram) throws SolrSearcherException {
        infoLogger.info("Requested diagram summary for query {}", query);
        List<String> speciess = new ArrayList<>();
        speciess.add(species);
        Query queryObject = new Query(query, diagram, speciess, null, null, null);
        return searchService.getDiagramSearchSummary(queryObject);
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Autowired
    public void setSpeciesService(SpeciesService speciesService) {
        this.speciesService = speciesService;
    }

    @Autowired
    public void setDos(DatabaseObjectService dos) {
        this.dos = dos;
    }

    @Autowired
    public void setSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    /**
     * Extra information to be sent to report service in order to store potential target
     */
    private Map<String, String> getReportInformation(HttpServletRequest request) {
        if (request == null) return null;

        Map<String, String> result = new HashMap<>();
        result.put("user-agent", request.getHeader("User-Agent"));
        String remoteAddr = request.getHeader("X-FORWARDED-FOR"); // Client IP
        if (remoteAddr == null || "".equals(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }
        result.put("ip-address", remoteAddr);
        result.put("release-version", releaseNumber.toString());

        return result;
    }

    private void checkDiagramIdentifier(String diagram) {
        checkIdentifiers(diagram, null);
    }

    /**
     * A simple mechanism to check whether the identifiers submitted by the users are valid. This is meant to offer
     * better HttpStatus codes depending on the paramenters (more accurate error messages)
     *
     * @param diagram a valid identifier to a diagrammed pathway (does not accept null)
     * @param object a valid identifier to any object in Reactome (accepts null)
     */
    private void checkIdentifiers(String diagram, String object) {
        SortedSet<String> report = new TreeSet<>();

        if (diagram == null || DatabaseObjectUtils.getIdentifier(diagram) == null) {
            report.add(String.format("'%s' is not a valid identifier", diagram));
        } else {
            DatabaseObject d = dos.findByIdNoRelations(diagram);
            if (d == null){
                report.add(String.format("'%s' cannot be found", diagram));
            } else if(!(d instanceof Pathway)) {
                report.add(String.format("'%s' does not belong to a pathway", diagram));
            } else {
                Pathway p = (Pathway) d;
                if (!p.getHasDiagram()) report.add(String.format("'%s' the pathway does not have diagram", diagram));
            }
        }

        if (object != null) {
            if (DatabaseObjectUtils.getIdentifier(object) == null) {
                report.add(String.format("'%s' is not a valid identifier", object));
            } else {
                DatabaseObject o = dos.findByIdNoRelations(object);
                if (o == null) report.add(String.format("'%s' cannot be found", object));
            }
        }

        if (!report.isEmpty()) throw new BadRequestException(StringUtils.join(report, " and "));
    }
}
