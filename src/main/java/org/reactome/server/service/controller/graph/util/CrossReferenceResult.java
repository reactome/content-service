package org.reactome.server.service.controller.graph.util;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.reactome.server.graph.domain.result.CustomQuery;

import java.util.List;

public class CrossReferenceResult implements CustomQuery {

    private String reference;
    private List<String> physicalEntities;
    private List<ShortCrossReference> crossReferences;


    public static CrossReferenceResult EMPTY = new CrossReferenceResult();
    static {
        EMPTY.setReference("NO MATCH");
        EMPTY.setPhysicalEntities(List.of());
        EMPTY.setCrossReferences(List.of());
    }

    public CrossReferenceResult() {
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<String> getPhysicalEntities() {
        return physicalEntities;
    }

    public void setPhysicalEntities(List<String> physicalEntities) {
        this.physicalEntities = physicalEntities;
    }

    public List<ShortCrossReference> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<ShortCrossReference> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Override
    public CustomQuery build(Record r) {
        CrossReferenceResult instance = new CrossReferenceResult();
        instance.setReference(r.get("reference").asString());
        instance.setPhysicalEntities(r.get("physicalEntities").asList(Value::asString));
        instance.setCrossReferences(r.get("crossReferences").asList(ShortCrossReference::build));
        return instance;
    }
}
