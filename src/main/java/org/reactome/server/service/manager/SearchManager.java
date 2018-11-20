package org.reactome.server.service.manager;

import org.reactome.server.search.domain.DiagramOccurrencesResult;
import org.reactome.server.search.domain.Query;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@Component
public class SearchManager {

    private SearchService searchService;

    public DiagramOccurrencesResult getDiagramOccurrencesResult(String pathway, String query) throws SolrSearcherException {
        DiagramOccurrencesResult rtn = new DiagramOccurrencesResult();
        Query queryObject = new Query(query, pathway, null, null, null, null);
        List<DiagramOccurrencesResult> diagramOccurrencesList = searchService.getDiagramFlagging(queryObject);
        for (DiagramOccurrencesResult diagramOccurrencesResult : diagramOccurrencesList) {
            if(diagramOccurrencesResult.getInDiagram()){
                rtn.addOccurrences(Collections.singletonList(diagramOccurrencesResult.getDiagramEntity()));
            }
            rtn.addOccurrences(diagramOccurrencesResult.getOccurrences());
            rtn.addInteractsWith(diagramOccurrencesResult.getInteractsWith());
        }
        return rtn;
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}
