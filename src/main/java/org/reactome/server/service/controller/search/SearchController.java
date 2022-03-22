package org.reactome.server.service.controller.search;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.reactome.server.service.exception.NoResultsFoundException;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.manager.SearchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "search")
@RequestMapping("/search")
class SearchController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    public static final int PRE_DETERMINED = 0;

    private final Integer releaseNumber;

    private SearchService searchService;
    private SearchManager searchManager;
    private SpeciesService speciesService;
    private DatabaseObjectService dos;

    @Autowired
    public SearchController(GeneralService generalService) {
        releaseNumber = generalService.getDBInfo().getVersion();
    }

    @Operation(summary = "Spell-check suggestions for a given query", description = "This method retrieves a list of spell-check suggestions for a given search term.")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Error in SolR")
    })
    @RequestMapping(value = "/spellcheck", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<String> spellcheckerSuggestions(@Parameter(description = "Search term", example = "repoduction", required = true) @RequestParam String query) throws SolrSearcherException {
        infoLogger.info("Request for spellcheck suggestions for query {}", query);
        return searchService.getSpellcheckSuggestions(query);
    }

    @Operation(summary = "Auto-suggestions for a given query", description = "This method retrieves a list of suggestions for a given search term.")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Error in SolR")
    })
    @RequestMapping(value = "/suggest", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<String> suggesterSuggestions(@Parameter(description = "Search term", example = "platele", required = true) @RequestParam String query) throws SolrSearcherException {
        infoLogger.info("Request for autocomplete suggestions for query {}", query);
        return searchService.getAutocompleteSuggestions(query);
    }

    @Operation(summary = "A list of facets corresponding to the whole Reactome search data", description = "This method retrieves faceting information on the whole Reactome search data.")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Error in SolR")
    })
    @RequestMapping(value = "/facet", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FacetMapping facet() throws SolrSearcherException {
        infoLogger.info("Request for faceting information of all Reactome data");
        return searchService.getTotalFacetingInformation();
    }

    @Operation(summary = "A list of facets corresponding to a specific query", description = "This method retrieves faceting information on a specific query")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Error in SolR")
    })
    @RequestMapping(value = "/facet_query", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FacetMapping facet_type(@Parameter(description = "Search term", example = "TP53", required = true) @RequestParam String query,
                                   @Parameter(description = "Species name") @RequestParam(required = false) List<String> species, // default value isn't supported by Swagger.
                                   @Parameter(description = "Types to filter") @RequestParam(required = false) List<String> types,
                                   @Parameter(description = "Compartments to filter") @RequestParam(required = false) List<String> compartments,
                                   @Parameter(description = "Keywords") @RequestParam(required = false) List<String> keywords) throws SolrSearcherException {
        infoLogger.info("Request for faceting information for query: {}", query);
        Query queryObject = new Query.Builder(query).forSpecies(species).withTypes(types).inCompartments(compartments).withKeywords(keywords).build();
        return searchService.getFacetingInformation(queryObject);
    }

    @Operation(summary = "Queries Solr against the Reactome knowledgebase", description = "This method performs a Solr query on the Reactome knowledgebase. Results can be provided in a paginated format.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Entry not found. Targets inform if the term is our scope of annotation"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Error in SolR")
    })
    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public GroupedResult getSearchResult(@Parameter(description = "Search term", example = "Biological oxidations", required = true) @RequestParam String query,
                                         @Parameter(description = "Species name") @RequestParam(required = false) List<String> species, // default value isn't supported by Swagger.
                                         @Parameter(description = "Types to filter") @RequestParam(required = false) List<String> types,
                                         @Parameter(description = "Compartments to filter") @RequestParam(required = false) List<String> compartments,
                                         @Parameter(description = "Keywords") @RequestParam(required = false) List<String> keywords,
                                         @Parameter(description = "Cluster results", example = "true") @RequestParam(required = false, defaultValue = "true") Boolean cluster,
                                         @Parameter(description = "Query parser to use", example = "STD") @RequestParam(required = false, defaultValue = "STD") ParserType parserType,
                                         @Parameter(description = "Start row", example = "0") @RequestParam(value = "Start row", required = false, defaultValue = "0") Integer start,
                                         @Parameter(description = "Number of rows to include", example = "10") @RequestParam(required = false, defaultValue = "10") Integer rows,
                                         HttpServletRequest request) throws SolrSearcherException {
        infoLogger.info("Search request for query: {}", query);
        Query queryObject = new Query.Builder(query).forSpecies(species).withTypes(types).inCompartments(compartments).withKeywords(keywords).start(start).numberOfRows(rows).withReportInfo(getReportInformation(request)).withParserType(parserType).build();
        SearchResult searchResult = searchService.getSearchResult(queryObject, PRE_DETERMINED, PRE_DETERMINED, cluster);
        GroupedResult result = searchResult != null ? searchResult.getGroupedResult() : null;
        if (result == null || result.getResults() == null || result.getResults().isEmpty()) {
            Set<TargetResult> targets = null;
            if (result != null && result.getTargetResults() != null && !result.getTargetResults().isEmpty()) {
                targets = result.getTargetResults();
            }
            throw new NoResultsFoundException("No entries found for query: " + query, targets);
        }
        return result;

    }

    @Operation(summary = "Queries Solr against the Reactome knowledgebase", description = "This method performs a Solr query on the Reactome knowledgebase. Results are in a paginated format, pages count starting from 1.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "One or more parameter is illegal"),
            @ApiResponse(responseCode = "404", description = "Entry not found. Targets inform if the term is our scope of annotation"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Error in SolR")
    })
    @RequestMapping(value = "/query/paginated", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public GroupedResult getResult(@Parameter(description = "Search term", example = "Biological oxidations", required = true) @RequestParam String query,
                                   @Parameter(description = "Page, should be strictly positive", example = "1", required = true) @RequestParam Integer page,
                                   @Parameter(description = "Rows per page", example = "10", required = true) @RequestParam Integer rowCount,
                                   @Parameter(description = "Species name") @RequestParam(required = false) List<String> species, // default value isn't supported by Swagger.
                                   @Parameter(description = "Types to filter") @RequestParam(required = false) List<String> types,
                                   @Parameter(description = "Compartments to filter") @RequestParam(required = false) List<String> compartments,
                                   @Parameter(description = "Keywords") @RequestParam(required = false) List<String> keywords,
                                   @Parameter(description = "Cluster results", example = "true") @RequestParam(required = false, defaultValue = "true") Boolean cluster,
                                   @Parameter(description = "Query parser to use", example = "STD") @RequestParam(required = false, defaultValue = "STD") ParserType parserType,
                                   HttpServletRequest request) throws SolrSearcherException {
        if (page <= 0) throw new BadRequestException("page should be greater than 0");
        if (rowCount <= 0) throw new BadRequestException("rowCount should be greater than 0");
        infoLogger.info("Search request for query: {}", query);
        Query queryObject = new Query.Builder(query).forSpecies(species).withTypes(types).inCompartments(compartments).withKeywords(keywords).withReportInfo(getReportInformation(request)).withParserType(parserType).build();
        SearchResult searchResult = searchService.getSearchResult(queryObject, rowCount, page, cluster);
        GroupedResult result = searchResult != null ? searchResult.getGroupedResult() : null;
        if (result == null || result.getResults() == null || result.getResults().isEmpty()) {
            Set<TargetResult> targets = null;
            if (result != null && result.getTargetResults() != null && !result.getTargetResults().isEmpty()) {
                targets = result.getTargetResults();
            }
            throw new NoResultsFoundException("No entries found for query: " + query, targets);
        }
        return result;
    }

    @Operation(summary = "Performs a Solr query (fireworks widget scoped) for a given QueryObject")
    @RequestMapping(value = "/fireworks", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FireworksResult getFireworksResult(@Parameter(example = "BRAF", required = true) @RequestParam String query,
                                              @Parameter(description = "Species name", example = "Homo sapiens") @RequestParam(required = false, defaultValue = "Homo sapiens") String species, // default value isn't supported by Swagger.
                                              @Parameter(description = "Types to filter") @RequestParam(required = false) List<String> types,
                                              @Parameter(description = "Start row") @RequestParam(required = false) Integer start,
                                              @Parameter(description = "Number of rows to include") @RequestParam(required = false) Integer rows,
                                              HttpServletRequest request) throws SolrSearcherException {
        infoLogger.info("Fireworks request for query: {}", query);
        List<String> speciess = new ArrayList<>();
        speciess.add(species);
        Query queryObject = new Query.Builder(query).forSpecies(speciess).withTypes(types).start(start).numberOfRows(rows).withReportInfo(getReportInformation(request)).build();
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

    @Operation(summary = "Performs a Solr query (fireworks widget scoped) for a given QueryObject")
    @RequestMapping(value = "/fireworks/flag", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FireworksOccurrencesResult fireworksFlagging(@Parameter(example = "KNTC1", required = true) @RequestParam String query,
                                                        @RequestParam(required = false, defaultValue = "Homo sapiens") String species) throws SolrSearcherException {
        infoLogger.info("Fireworks Flagging request for query: {}", query);
        Species sp = speciesService.getSpeciesByName(species);
        if (sp == null) throw new BadRequestException("No species found for '" + species + "'");

        FireworksOccurrencesResult rtn = searchManager.getFireworksOccurrencesResult(sp, query);
        if (rtn.isEmpty())
            throw new NotFoundException("No entries found for query: '" + query + "' in species '" + species + "'");
        return rtn;
    }

    @Operation(summary = "Performs a Solr query (diagram widget scoped) for a given QueryObject")
    @RequestMapping(value = "/diagram/{diagram}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DiagramResult getDiagramResult(@Parameter(example = "R-HSA-8848021", required = true) @PathVariable String diagram,
                                          @Parameter(example = "MAD", required = true) @RequestParam String query,
                                          @Parameter(description = "Types to filter") @RequestParam(required = false) List<String> types,
                                          @Parameter(description = "Start row") @RequestParam(required = false) Integer start,
                                          @Parameter(description = "Number of rows to include") @RequestParam(required = false) Integer rows) throws SolrSearcherException {
        checkDiagramIdentifier(diagram);
        Query queryObject = new Query.Builder(query).addFilterQuery(diagram).withTypes(types).start(start).numberOfRows(rows).build();
        DiagramResult rtn = searchService.getDiagrams(queryObject);
        if (rtn == null || rtn.getFound() == 0)
            throw new NotFoundException(String.format("No entries found for '%s' in diagram '%s'", query, diagram));
        return rtn;
    }

    @Operation(summary = "Performs a Solr query (diagram widget scoped) for a given QueryObject")
    @RequestMapping(value = "/diagram/{diagram}/occurrences/{instance}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DiagramOccurrencesResult getDiagramOccurrences(@Parameter(example = "R-HSA-68886", required = true) @PathVariable String diagram,
                                                          @Parameter(example = "R-HSA-141433", required = true) @PathVariable String instance,
                                                          @Parameter(description = "Types to filter") @RequestParam(required = false) List<String> types) throws SolrSearcherException {
        checkIdentifiers(diagram, instance);
        Query queryObject = new Query.Builder(instance).addFilterQuery(diagram).withTypes(types).build();
        DiagramOccurrencesResult rtn = searchService.getDiagramOccurrencesResult(queryObject);
        if (rtn == null)
            throw new NotFoundException(String.format("No occurrences of '%s' found in '%s'", instance, diagram));
        return rtn;
    }

    @Operation(summary = "A list of diagram entities plus pathways from the provided list containing the specified identifier", description = "This method traverses the content and checks not only for the main identifier but also for all the cross-references to find the flag targets")
    @RequestMapping(value = "/diagram/{pathwayId}/flag", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DiagramOccurrencesResult getEntitiesInDiagramForIdentifier(@Parameter(description = "The pathway to find items to flag", example = "R-HSA-446203")
                                                                      @PathVariable String pathwayId,
                                                                      @Parameter(description = "The identifier for the elements to be flagged", example = "CTSA")
                                                                      @RequestParam String query) throws SolrSearcherException {
        checkDiagramIdentifier(pathwayId);
        DiagramOccurrencesResult rtn = searchManager.getDiagramOccurrencesResult(pathwayId, query);
        infoLogger.info("Request for all entities in diagram with identifier: {}", query);
        if (rtn.isEmpty())
            throw new NotFoundException("No entities with identifier '" + query + "' found for " + pathwayId);
        return rtn;
    }

    @Hidden
    @Operation
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Error in SolR")
    })
    @RequestMapping(value = "/diagram/summary", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DiagramSearchSummary diagramSearchSummary(@Parameter(description = "Search term", example = "KIF", required = true)
                                                     @RequestParam String query,
                                                     @Parameter(description = "Species name", example = "Homo sapiens", required = true)
                                                     @RequestParam(required = false, defaultValue = "Homo sapiens") String species,
                                                     @Parameter(description = "Diagram", example = "R-HSA-8848021", required = true)
                                                     @RequestParam String diagram) throws SolrSearcherException {
        infoLogger.info("Requested diagram summary for query {}", query);
        List<String> speciess = new ArrayList<>();
        speciess.add(species);
        Query queryObject = new Query.Builder(query).addFilterQuery(diagram).forSpecies(speciess).build();
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
     * better HttpStatus codes depending on the parameters (more accurate error messages)
     *
     * @param diagram a valid identifier to a diagrammed pathway (does not accept null)
     * @param object  a valid identifier to any object in Reactome (accepts null)
     */
    private void checkIdentifiers(String diagram, String object) {
        SortedSet<String> report = new TreeSet<>();

        if (diagram == null || DatabaseObjectUtils.getIdentifier(diagram) == null) {
            report.add(String.format("'%s' is not a valid identifier", diagram));
        } else {
            DatabaseObject d = dos.findByIdNoRelations(diagram);
            if (d == null) {
                report.add(String.format("'%s' cannot be found", diagram));
            } else if (!(d instanceof Pathway)) {
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
