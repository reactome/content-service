package org.reactome.server.controller.search;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.reactome.server.search.domain.FacetMapping;
import org.reactome.server.search.domain.GroupedResult;
import org.reactome.server.search.domain.Query;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Converts a Solr QueryResponse into Objects provided by Project Models
 *
 * @author Florian Korninger (fkorn@ebi.ac.uk)
 * @version 1.0
 */
@RestController
@Api(tags = "search", description = "Reactome Search")
@RequestMapping("/search")
class SearchController {

    private static final Logger logger = Logger.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    @ApiOperation(value = "Retrieves spellcheck suggestions for a given query",response = String.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/spellcheck", method = RequestMethod.GET)
    @ResponseBody
    public List<String> spellcheckSuggestions(@ApiParam(defaultValue = "appoptosis",required = true) @RequestParam String query) throws SolrSearcherException {
        return searchService.getSpellcheckSuggestions(query);
    }

    @ApiOperation(value = "Retrieves auto-suggestions for a given query",response = String.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    @ResponseBody
    public List<String> suggesterSuggestions(@ApiParam(defaultValue = "apoptos",required = true)@RequestParam String query) throws SolrSearcherException {
        return searchService.getAutocompleteSuggestions(query);
    }

    @ApiOperation(value = "Retrieves faceting information on the whole Reactome search data",response = FacetMapping.class, produces = "application/json")
    @RequestMapping(value = "/facet", method = RequestMethod.GET)
    @ResponseBody
    public FacetMapping facet() throws SolrSearcherException {
        return searchService.getTotalFacetingInformation();
    }

    @ApiOperation(value = "Retrieves faceting information for a given query",response = FacetMapping.class, produces = "application/json")
    @RequestMapping(value = "/facet_query", method = RequestMethod.GET)
    @ResponseBody
    public FacetMapping facet_type(@ApiParam(defaultValue = "apoptosis",required = true) @RequestParam String query,
                                   @ApiParam(defaultValue = "Homo sapiens") @RequestParam( required = false ) List<String> species,
                                   @ApiParam(defaultValue = "Reaction, Pathway") @RequestParam( required = false ) List<String> types,
                                   @RequestParam( required = false ) List<String> compartments,
                                   @RequestParam( required = false ) List<String> keywords ) throws SolrSearcherException {
        Query queryObject = new Query(query, species, types, compartments, keywords);
        return searchService.getFacetingInformation(queryObject);
    }

    @ApiOperation(value = "Performs a Solr query for given QueryObject",response = GroupedResult.class, produces = "application/json")
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public GroupedResult getResult (@ApiParam(defaultValue = "apoptosis",required = true) @RequestParam String query,
                                    @ApiParam(defaultValue = "Homo sapiens") @RequestParam( required = false ) List<String> species,
                                    @ApiParam(defaultValue = "Reaction, Pathway") @RequestParam( required = false ) List<String> types,
                                    @RequestParam( required = false ) List<String> compartments,
                                    @RequestParam( required = false ) List<String> keywords,
                                    @ApiParam(defaultValue = "true") @RequestParam( required = false ) Boolean cluster,
                                    @RequestParam( required = false ) Integer page,
                                    @RequestParam( required = false ) Integer rows ) throws SolrSearcherException {
        Query queryObject = new Query(query, species,types,compartments,keywords,page, rows);
        return searchService.getEntries(queryObject, cluster);
    }

//    /**
//     * Overwrites the Global Exception Handler
//     */
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(SolrSearcherException.class)
//    @ResponseBody
//    ErrorInfo handleSolrException(HttpServletRequest req, SolrSearcherException e) {
//        logger.error(e);
//        return new ErrorInfo("SolrService Exception occurred", req.getRequestURL(), e);
//    }
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
//    /**
//     * Overwrites the Global Exception Handler
//     */
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(EnricherException.class)
//    @ResponseBody
//    ErrorInfo handleEnricherException(HttpServletRequest req, EnricherException e) {
//        logger.error(e);
//        return new ErrorInfo("Enricher Exception occurred", req.getRequestURL(), e);
//    }
}