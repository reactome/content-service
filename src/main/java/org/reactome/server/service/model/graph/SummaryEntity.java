package org.reactome.server.service.model.graph;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.reactome.server.graph.domain.annotations.ReactomeProjectedRelationship;
import org.reactome.server.graph.domain.annotations.ReactomeSchemaIgnore;
import org.reactome.server.graph.domain.annotations.StoichiometryView;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.relationship.Has;
import org.reactome.server.graph.domain.relationship.HasModifiedResidue;
import org.reactome.server.service.utils.GetterSetterMap;

import java.util.*;

import static org.reactome.server.service.utils.GetterSetterMap.accessor;

@EqualsAndHashCode(callSuper = true)
@Data
public class SummaryEntity extends PhysicalEntity {
    private List<PhysicalEntity> summarisedEntities;

    // ReferenceEntity specific
    private String moleculeType;
    private String databaseName;
    private String identifier;
    private List<String> name;
    private List<String> otherIdentifier;
    private String url;
    private ReferenceDatabase referenceDatabase;
    private List<DatabaseIdentifier> crossReference;


    public SummaryEntity(ReferenceEntity referenceEntity) {
        referenceEntity.preventLazyLoading(true);
        this.setDbId(referenceEntity.getDbId());
        this.setStId(referenceEntity.getStId());
        this.setDisplayName(referenceEntity.getDisplayName());
        this.setName(referenceEntity.getName());

        this.referenceEntity = referenceEntity;
        this.moleculeType = referenceEntity.getMoleculeType();
        this.databaseName = referenceEntity.getDatabaseName();
        this.identifier = referenceEntity.getIdentifier();
        this.url = referenceEntity.getUrl();
        this.referenceDatabase = referenceEntity.getReferenceDatabase();
        this.otherIdentifier = referenceEntity.getOtherIdentifier();
        this.crossReference = referenceEntity.getCrossReference();

        List<PhysicalEntity> physicalEntities = referenceEntity.getPhysicalEntity();
        physicalEntities.sort(comparator);
        this.summarisedEntities = physicalEntities;

        physicalEntities.forEach(entity -> accessors.forEach(accessor -> mergeProperty(this, entity, accessor)));
    }

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

    private static final Comparator<PhysicalEntity> comparator =
            Comparator.comparingInt((PhysicalEntity pe) -> pe instanceof EntityWithAccessionedSequence ? ((EntityWithAccessionedSequence) pe).getModifiedResidues().size() : 0)
                    .thenComparingLong(PhysicalEntity::getDbId);

    private static final List<GetterSetterMap.Accessor<?, ? extends PhysicalEntity, SummaryEntity>> accessors = List.of(
            accessor(PhysicalEntity::getDisease, SummaryEntity::setDisease, PhysicalEntity.class),
//                    accessor(PhysicalEntity::getName, SummaryEntity::setName, PhysicalEntity.class),
            accessor(PhysicalEntity::getCrossReference, SummaryEntity::setCrossReference, PhysicalEntity.class),
            accessor(PhysicalEntity::getCompartment, SummaryEntity::setCompartment, PhysicalEntity.class),
            accessor(PhysicalEntity::getComponentOf, SummaryEntity::setComponentOf, PhysicalEntity.class),
            accessor(PhysicalEntity::getMemberOf, SummaryEntity::setMemberOf, PhysicalEntity.class),
            accessor(PhysicalEntity::getCandidateOf, SummaryEntity::setCandidateOf, PhysicalEntity.class),
            accessor(PhysicalEntity::getIsRequired, SummaryEntity::setIsRequired, PhysicalEntity.class),
            accessor(PhysicalEntity::getInferredFrom, SummaryEntity::setInferredFrom, PhysicalEntity.class),
            accessor(PhysicalEntity::getInferredTo, SummaryEntity::setInferredTo, PhysicalEntity.class),
            accessor(PhysicalEntity::getInputFor, SummaryEntity::setInputFor, PhysicalEntity.class),
            accessor(PhysicalEntity::getOutputFor, SummaryEntity::setOutputFor, PhysicalEntity.class),
            accessor(PhysicalEntity::getCatalystActivities, SummaryEntity::setCatalystActivities, PhysicalEntity.class),
            accessor(PhysicalEntity::getLiteratureReference, SummaryEntity::setLiteratureReference, PhysicalEntity.class),
            accessor(PhysicalEntity::getPositivelyRegulates, SummaryEntity::setPositivelyRegulates, PhysicalEntity.class),
            accessor(PhysicalEntity::getNegativelyRegulates, SummaryEntity::setNegativelyRegulates, PhysicalEntity.class),
//                    accessor(PhysicalEntity::getSummation, SummaryEntity::setSummation, PhysicalEntity.class),
            accessor(PhysicalEntity::getMarkingReferences, SummaryEntity::setMarkingReferences, PhysicalEntity.class),

            accessor(GenomeEncodedEntity::getSpecies, SummaryEntity::getSpecies, SummaryEntity::setSpecies, GenomeEncodedEntity.class),
            accessor(SimpleEntity::getSpecies, SummaryEntity::getSpecies, SummaryEntity::setSpecies, SimpleEntity.class),
            accessor(SimpleEntity::getReferenceType, SummaryEntity::getReferenceType, SummaryEntity::setReferenceType, SimpleEntity.class),
//                    accessor(EntityWithAccessionedSequence::getHasModifiedResidue, SummaryEntity::getHasModifiedResidue, SummaryEntity::setHasModifiedResidue, EntityWithAccessionedSequence.class),
            accessor(EntityWithAccessionedSequence::getReferenceType, SummaryEntity::getReferenceType, SummaryEntity::setReferenceType, EntityWithAccessionedSequence.class)
    );


    // Unsafe, but unavoidable: cast to generic with helper method
    @SuppressWarnings("unchecked")
    private static <T, S, R> void mergeProperty(T target, S source, GetterSetterMap.Accessor<?, ? extends S, T> acc) {
        if (acc.sourceClass.isAssignableFrom(source.getClass())) {
            Object sourceValue = getPropertyValue(source, (GetterSetterMap.Accessor<R, S, T>) acc);
            if (sourceValue == null) return;
            if (sourceValue instanceof Collection) {
                if (((Collection<?>) sourceValue).isEmpty()) return;
                mergeSingleList(target, source, (GetterSetterMap.Accessor<Collection<R>, S, T>) acc);
            } else {
                mergeSingleProperty(target, source, (GetterSetterMap.Accessor<R, S, T>) acc);
            }
        }
    }

    private static <R, S, T> R getPropertyValue(S source, GetterSetterMap.Accessor<R, S, T> acc) {
        return acc.sourceGetter.apply(source);
    }

    private static <T, S, V> void mergeSingleProperty(T target, S source, GetterSetterMap.Accessor<V, S, T> acc) {
        acc.setter.accept(target, acc.sourceGetter.apply(source));
    }

    private static <T, S, E> void mergeSingleList(T target, S source, GetterSetterMap.Accessor<Collection<E>, S, T> acc) {
        Collection<E> sourceList = acc.sourceGetter.apply(source);
        if (sourceList == null) return;
        Collection<E> targetList = acc.targetGetter.apply(target);
        if (targetList == null) targetList = new LinkedHashSet<>();
        Set<E> result = targetList instanceof Set ? (Set<E>) targetList : new LinkedHashSet<>(targetList);
        result.addAll(sourceList);
        acc.setter.accept(target, new ArrayList<>(result));
    }
}
