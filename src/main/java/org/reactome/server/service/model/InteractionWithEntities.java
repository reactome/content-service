package org.reactome.server.service.model;

import org.neo4j.driver.Record;
import org.reactome.server.graph.domain.result.CustomQuery;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Result row for the "interactor page" Cypher query. Mirrors the per-interactor
 * payload that data-content/InteractionsController used to render server-side
 * for the Joomla interactor profile page, so the Angular interactor detail
 * page can fetch a single response instead of fanning out a separate
 * /references/mapping/{id}/xrefs call per interactor (TP53 has ~250).
 */
public class InteractionWithEntities implements CustomQuery {
    private Double score;
    private String accession;
    private String accessionURL;
    private Collection<SimpleDatabaseObject> physicalEntity;
    private Integer evidences;
    private String url;

    public InteractionWithEntities() {
    }

    public Double getScore() {
        return score;
    }

    public String getAccession() {
        return accession;
    }

    public String getAccessionURL() {
        return accessionURL;
    }

    public Collection<SimpleDatabaseObject> getPhysicalEntity() {
        if (physicalEntity == null) return java.util.Collections.emptyList();
        return physicalEntity.stream()
                .sorted(Comparator.comparing(SimpleDatabaseObject::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Integer getEvidences() {
        return evidences;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public CustomQuery build(Record r) {
        InteractionWithEntities ci = new InteractionWithEntities();
        ci.accession = r.get("accession").asString(null);
        ci.accessionURL = r.get("accessionURL").asString(null);
        ci.evidences = r.get("evidences").asInt(0);
        ci.score = r.get("score").asDouble(0);
        ci.url = r.get("url").asString(null);
        ci.physicalEntity = r.get("physicalEntity").asList(SimpleDatabaseObject::build);
        return ci;
    }
}
