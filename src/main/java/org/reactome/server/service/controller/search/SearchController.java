package org.reactome.server.service.controller.search;

import io.swagger.annotations.*;
import org.reactome.server.search.domain.*;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.search.service.SearchService;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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

    @Autowired
    private SearchService searchService;

    @ApiOperation(value = "Spell-check suggestions for a given query", notes = "This method retrieves a list of spell-check suggestions for a given search term.", response = String.class, responseContainer = "List", produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Error in SolR", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/spellcheck", method = RequestMethod.GET)
    @ResponseBody
    public List<String> spellcheckerSuggestions(@ApiParam(value = "Search term", defaultValue = "appoptosis", required = true) @RequestParam String query) throws SolrSearcherException {
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
    public List<String> suggesterSuggestions(@ApiParam(value = "Search term", defaultValue = "apoptos", required = true) @RequestParam String query) throws SolrSearcherException {
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
    public FacetMapping facet_type(@ApiParam(value = "Search term", defaultValue = "apoptosis", required = true) @RequestParam String query,
                                   @RequestParam(value = "Species names", required = false) List<String> species,
                                   @RequestParam(value = "Types to filter", required = false) List<String> types,
                                   @RequestParam(value = "Compartments to filter", required = false) List<String> compartments,
                                   @RequestParam(value = "Reaction types to filter", required = false) List<String> keywords) throws SolrSearcherException {
        infoLogger.info("Request for faceting information for query: {}", query);
        Query queryObject = new Query(query, species, types, compartments, keywords);
        return searchService.getFacetingInformation(queryObject);
    }

    @ApiOperation(value = "Queries Solr against the Reactome knowledgebase", notes = "This method performs a Solr query on the Reactome knowledgebase. Results can be provided in a paginated format.", response = GroupedResult.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Error in SolR", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public GroupedResult getResult(@ApiParam(value = "Search term", defaultValue = "apoptosis", required = true) @RequestParam String query,
                                   @RequestParam(value = "Species to filter", required = false) List<String> species,
                                   @RequestParam(value = "Types to filter", required = false) List<String> types,
                                   @RequestParam(value = "Compartments to filter", required = false) List<String> compartments,
                                   @RequestParam(value = "Reaction types to filter", required = false) List<String> keywords,
                                   @ApiParam(value = "Cluster results", defaultValue = "true") @RequestParam(required = false, defaultValue = "true") Boolean cluster,
                                   @RequestParam(value = "Start row", required = false) Integer start,
                                   @RequestParam(value = "Number of rows to include", required = false) Integer rows) throws SolrSearcherException {
        infoLogger.info("Search request for query: {}", query);
        Query queryObject = new Query(query, species, types, compartments, keywords, start, rows);
        GroupedResult result = searchService.getEntries(queryObject, cluster);
        if (result == null || result.getResults() == null || result.getResults().isEmpty())
            throw new NotFoundException("No entries found for query: " + query);
        return result;
    }

    @ApiOperation(value = "Performs a Solr query (fireworks widget scoped) for a given QueryObject", produces = "application/json")
    @RequestMapping(value = "/fireworks", method = RequestMethod.GET)
    @ResponseBody
    public FireworksResult getFireworksResult(@ApiParam(defaultValue = "PTEN", required = true) @RequestParam String query,
                                              @ApiParam(defaultValue = "Homo sapiens") @RequestParam(required = false) List<String> species,
                                              @ApiParam(defaultValue = "Protein") @RequestParam(required = false) List<String> types,
                                              @RequestParam(required = false) Integer start,
                                              @RequestParam(required = false) Integer rows) throws SolrSearcherException {
        infoLogger.info("Fireworks request for query: {}", query);
        Query queryObject = new Query(query, species, types, null, null, start, rows);
        return searchService.getFireworks(queryObject);
    }

    @ApiOperation(value = "Performs a Solr query (diagram widget scoped) for a given QueryObject", produces = "application/json")
    @RequestMapping(value = "/diagram/{diagram}", method = RequestMethod.GET)
    @ResponseBody
    public DiagramResult getDiagramResult(@ApiParam(defaultValue = "R-HSA-9006927", required = true) @PathVariable String diagram,
                                          @ApiParam(defaultValue = "MAD1L1", required = true) @RequestParam String query,
                                          @RequestParam(required = false) List<String> species,
                                          @RequestParam(required = false) List<String> types,
                                          @RequestParam(required = false) Integer start,
                                          @RequestParam(required = false) Integer rows) throws SolrSearcherException {
        Query queryObject = new Query(query, diagram, species, types, null, null, start, rows);
        return searchService.getDiagrams(queryObject);
    }

    @ApiOperation(value = "Performs a Solr query (diagram widget scoped) for a given QueryObject", produces = "application/json")
    @RequestMapping(value = "/diagram/{diagram}/occurrences/{instance}", method = RequestMethod.GET)
    @ResponseBody
    public DiagramOccurrencesResult getDiagramOccurrences(@ApiParam(defaultValue = "R-HSA-68886", required = true) @PathVariable String diagram,
                                                          @ApiParam(defaultValue = "R-HSA-141433", required = true) @PathVariable String instance,
                                                          @RequestParam(required = false) List<String> species,
                                                          @RequestParam(required = false) List<String> types,
                                                          @RequestParam(required = false) Integer start,
                                                          @RequestParam(required = false) Integer rows) throws SolrSearcherException {
        Query queryObject = new Query(instance, diagram, species, types, null, null, start, rows);
        return searchService.getDiagramOccurrencesResult(queryObject);
    }

    @ApiOperation(value = "A list of diagram entities plus pathways from the provided list containing the specified identifier", notes = "This method traverses the content and checks not only for the main identifier but also for all the cross-references to find the flag targets")
    @RequestMapping(value = "/diagram/{pathwayId}/flag", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<String> getEntitiesInDiagramForIdentifier(@ApiParam(value = "The pathway to find items to flag", defaultValue = "R-HSA-446203")
                                                                              @PathVariable String pathwayId,
                                                                              @ApiParam(value = "The identifier for the elements to be flagged", defaultValue = "CTSA")
                                                                              @RequestParam String query) throws SolrSearcherException {
        Collection<String> rtn = new HashSet<>();
        Query queryObject = new Query(query, pathwayId, null, null, null, null);
        DiagramResult searchInDiagram = searchService.getDiagrams(queryObject);
        List<Entry> entries = searchInDiagram.getEntries();
        for (Entry entry : entries) {
            String stId = entry.getStId();
            queryObject = new Query(stId, pathwayId, null, null, null, null);
            DiagramOccurrencesResult diagramOccurrencesResult = searchService.getDiagramOccurrencesResult(queryObject);
            if (diagramOccurrencesResult.getOccurrences() != null) rtn.addAll(diagramOccurrencesResult.getOccurrences());
            if (diagramOccurrencesResult.getInDiagram()) rtn.add(stId);
        }

        if (rtn.isEmpty()) throw new NotFoundException("No entities with identifier '" + query + "' found for " + pathwayId);
        infoLogger.info("Request for all entities in diagram with identifier: {}", query);
        return rtn;
    }
}
