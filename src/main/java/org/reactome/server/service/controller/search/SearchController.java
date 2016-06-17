package org.reactome.server.service.controller.search;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.search.domain.FacetMapping;
import org.reactome.server.search.domain.FireworksResult;
import org.reactome.server.search.domain.GroupedResult;
import org.reactome.server.search.domain.Query;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@RestController
@Api(tags = "search", description = "Reactome Search")
@RequestMapping("/search")
class SearchController {

    @Autowired
    private SearchService searchService;

    @ApiOperation(value = "Retrieves spellcheck suggestions for a given query", response = String.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/spellcheck", method = RequestMethod.GET)
    @ResponseBody
    public List<String> spellcheckSuggestions(@ApiParam(defaultValue = "appoptosis", required = true) @RequestParam String query) throws SolrSearcherException {
        return searchService.getSpellcheckSuggestions(query);
    }

    @ApiOperation(value = "Retrieves auto-suggestions for a given query", response = String.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    @ResponseBody
    public List<String> suggesterSuggestions(@ApiParam(defaultValue = "apoptos", required = true) @RequestParam String query) throws SolrSearcherException {
        return searchService.getAutocompleteSuggestions(query);
    }

    @ApiOperation(value = "Retrieves faceting information on the whole Reactome search data", response = FacetMapping.class, produces = "application/json")
    @RequestMapping(value = "/facet", method = RequestMethod.GET)
    @ResponseBody
    public FacetMapping facet() throws SolrSearcherException {
        return searchService.getTotalFacetingInformation();
    }

    @ApiOperation(value = "Retrieves faceting information for a given query", response = FacetMapping.class, produces = "application/json")
    @RequestMapping(value = "/facet_query", method = RequestMethod.GET)
    @ResponseBody
    public FacetMapping facet_type(@ApiParam(defaultValue = "apoptosis", required = true) @RequestParam String query,
                                   @ApiParam(defaultValue = "Homo sapiens") @RequestParam(required = false) List<String> species,
                                   @ApiParam(defaultValue = "Reaction, Pathway") @RequestParam(required = false) List<String> types,
                                   @RequestParam(required = false) List<String> compartments,
                                   @RequestParam(required = false) List<String> keywords) throws SolrSearcherException {
        Query queryObject = new Query(query, species, types, compartments, keywords);
        return searchService.getFacetingInformation(queryObject);
    }

    @ApiOperation(value = "Performs a Solr query for given QueryObject", response = GroupedResult.class, produces = "application/json")
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public GroupedResult getResult(@ApiParam(defaultValue = "apoptosis", required = true) @RequestParam String query,
                                   @ApiParam(defaultValue = "Homo sapiens") @RequestParam(required = false) List<String> species,
                                   @ApiParam(defaultValue = "Reaction, Pathway") @RequestParam(required = false) List<String> types,
                                   @RequestParam(required = false) List<String> compartments,
                                   @RequestParam(required = false) List<String> keywords,
                                   @ApiParam(defaultValue = "true") @RequestParam(required = false) Boolean cluster,
                                   @RequestParam(required = false) Integer start,
                                   @RequestParam(required = false) Integer rows) throws SolrSearcherException {
        Query queryObject = new Query(query, species, types, compartments, keywords, start, rows);
        return searchService.getEntries(queryObject, cluster);
    }

    @ApiIgnore
    @ApiOperation(value = "Performs a Solr query (fireworks widget scoped) for a given QueryObject", produces = "application/json")
    @RequestMapping(value = "/fireworks", method = RequestMethod.GET)
    @ResponseBody
    public FireworksResult getFireworksResult(@ApiParam(defaultValue = "PTEN", required = true) @RequestParam String query,
                                              @ApiParam(defaultValue = "Homo sapiens") @RequestParam(required = false) List<String> species,
                                              @ApiParam(defaultValue = "Protein") @RequestParam(required = false) List<String> types,
                                              @RequestParam(required = false) Integer start,
                                              @RequestParam(required = false) Integer rows) throws SolrSearcherException {
        Query queryObject = new Query(query, species, types, null, null, start, rows);
        return searchService.getFireworks(queryObject);
    }

//    /**
//     * Overwrites the Global Exception Handler
//     */

//    /**
//     * Overwrites the Global Exception Handler
//     */
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(SearchServiceException.class)
//    @ResponseBody
//    ErrorInfo handleServiceException(HttpServletRequest req, SearchServiceException e) {
//        logger.error(e);
//        return new ErrorInfo("SearchService Exception occurred", req.getRequestURL(), e);
//    }

}