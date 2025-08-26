package org.reactome.server.service.model.graph;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.reactome.server.graph.domain.annotations.ReactomeProjectedRelationship;
import org.reactome.server.graph.domain.annotations.ReactomeSchemaIgnore;
import org.reactome.server.graph.domain.annotations.StoichiometryView;
import org.reactome.server.graph.domain.model.AbstractModifiedResidue;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.domain.model.Taxon;
import org.reactome.server.graph.domain.relationship.Has;
import org.reactome.server.graph.domain.relationship.HasModifiedResidue;

import java.util.List;
import java.util.SortedSet;

@EqualsAndHashCode(callSuper = true)
@Data
public class SummaryEntity extends PhysicalEntity {
    private List<PhysicalEntity> summarisedEntities;

    // Summarised properties
    private String referenceType;
    private ReferenceEntity referenceEntity;
    private Taxon species;

    // EWAS specific
    private Integer endCoordinate;
    private Integer startCoordinate;
    private SortedSet<HasModifiedResidue> hasModifiedResidue;

    @ReactomeSchemaIgnore
    @JsonView(StoichiometryView.Nested.class)
    @ReactomeProjectedRelationship("getHasModifiedResidue")
    public SortedSet<HasModifiedResidue> getModifiedResidues() {
        return hasModifiedResidue;
    }

    @JsonView(StoichiometryView.Nested.class)
    public void setModifiedResidues(SortedSet<HasModifiedResidue> hasModifiedResidue) {
        this.hasModifiedResidue = hasModifiedResidue;
    }

    @JsonView(StoichiometryView.Flatten.class)
    public List<AbstractModifiedResidue> getHasModifiedResidue() {
        return Has.Util.expandStoichiometry(hasModifiedResidue);
    }

    @JsonView(StoichiometryView.Flatten.class)
    public void setHasModifiedResidue(List<AbstractModifiedResidue> hasModifiedResidue) {
        this.hasModifiedResidue = Has.Util.aggregateStoichiometry(hasModifiedResidue, HasModifiedResidue::new);
    }

    @Override
    public String getClassName() {
        if (referenceType == null) return super.getClassName();
        switch (referenceType) {
            case ("ReferenceGeneProduct"):
                return "Protein";
            case ("ReferenceDNASequence"):
                return "DNA Sequence";
            case ("ReferenceRNASequence"):
                return "RNA Sequence";
            default:
                return super.getClassName();
        }
    }
}
