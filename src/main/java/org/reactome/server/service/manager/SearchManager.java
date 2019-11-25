package org.reactome.server.service.manager;

import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.search.domain.DiagramOccurrencesResult;
import org.reactome.server.search.domain.FireworksOccurrencesResult;
import org.reactome.server.search.domain.Query;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.search.service.SearchService;
import org.reactome.server.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@Component
public class SearchManager {

    private SearchService searchService;
    private AdvancedDatabaseObjectService ados;

    public DiagramOccurrencesResult getDiagramOccurrencesResult(String pathway, String query) throws SolrSearcherException {
        DiagramOccurrencesResult rtn = new DiagramOccurrencesResult();
        Query queryObject = new Query.Builder(query).addFilterQuery(pathway).build();
        List<DiagramOccurrencesResult> diagramOccurrencesList = searchService.getDiagramFlagging(queryObject);
        for (DiagramOccurrencesResult diagramOccurrencesResult : diagramOccurrencesList) {
            if (diagramOccurrencesResult.getInDiagram()) {
                rtn.addOccurrences(Collections.singletonList(diagramOccurrencesResult.getDiagramEntity()));
            }
            rtn.addOccurrences(diagramOccurrencesResult.getOccurrences());
            rtn.addInteractsWith(diagramOccurrencesResult.getInteractsWith());
        }
        return rtn;
    }

    public Collection<String> getDiagramFlagging(String pathway, String query, Boolean includeInteractors) throws SolrSearcherException {
        DiagramOccurrencesResult occ = getDiagramOccurrencesResult(pathway, query);
        Collection<String> toFlag = occ.getOccurrences() != null ? occ.getOccurrences() : new ArrayList<>();
        if (includeInteractors && occ.getInteractsWith() != null) toFlag.addAll(occ.getInteractsWith());

        try {
            String q = "" +
                    "MATCH path=(p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent) " +
                    "WHERE p.stId IN {toFlag} AND NONE(x IN NODES(path) WHERE (x:Pathway) AND x.hasDiagram) " +
                    "RETURN DISTINCT rle.stId AS identifier ";
            Map<String, Object> params = new HashMap<>();
            params.put("toFlag", toFlag);
            toFlag.addAll(ados.getCustomQueryResults(String.class, q, params));
        } catch (CustomQueryException e) {
            //Nothing here
        }
        return toFlag;
    }

    @SuppressWarnings("Duplicates")
    public FireworksOccurrencesResult getFireworksOccurrencesResult(Species species, String query) throws SolrSearcherException {
        List<String> speciess = new ArrayList<>();
        speciess.add(species.getDisplayName());
        Query queryObject = new Query.Builder(query).forSpecies(speciess).build();

        FireworksOccurrencesResult occ = searchService.fireworksFlagging(queryObject);

        //Filter the result by species. This is necessary when the query term is a chemical
        String prefix = "R-" + species.getAbbreviation();

        FireworksOccurrencesResult rtn = new FireworksOccurrencesResult();

        if (occ.getLlps() != null) {
            rtn.addLlps(occ.getLlps().stream()
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList()));
        }
        if (occ.getInteractsWith() != null) {
            occ.getInteractsWith().stream()
                    .filter(s -> s.startsWith(prefix))
                    .forEach(rtn::addInteractsWith);
        }

        if (rtn.isEmpty())
            throw new NotFoundException("No entries found for query: '" + query + "' in species '" + species + "'");

        return rtn;
    }


    @SuppressWarnings("Duplicates")
    public Collection<String> getFireworksFlagging(Species species, String query, Boolean includeInteractors) throws SolrSearcherException {
        FireworksOccurrencesResult occ = getFireworksOccurrencesResult(species, query);
        Collection<String> rtn = occ.getLlps() != null ? occ.getLlps() : new ArrayList<>();
        if (includeInteractors && occ.getInteractsWith() != null) rtn.addAll(occ.getInteractsWith());
        if (rtn.isEmpty())
            throw new NotFoundException("No entries found for query: '" + query + "' in species '" + species + "'");
        return rtn;
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Autowired
    public void setAdos(AdvancedDatabaseObjectService ados) {
        this.ados = ados;
    }
}
