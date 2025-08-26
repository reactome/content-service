package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.helper.RelationshipDirection;
import org.reactome.server.graph.service.util.DatabaseObjectUtils;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.exception.NotFoundTextPlainException;
import org.reactome.server.service.model.graph.SummaryEntity;
import org.reactome.server.service.utils.GetterSetterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.reactome.server.service.utils.GetterSetterMap.accessor;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "query", description = "Reactome Data: Common data retrieval")
@RequestMapping("/data")
public class QueryObjectController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private DatabaseObjectService databaseObjectService;

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    @Operation(summary = "An entry in Reactome knowledgebase", description = "This method queries for an entry in Reactome knowledgebase based on the given identifier, i.e. stable id or database id. It is worth mentioning that the retrieved database object has all its properties and direct relationships (relationships of depth 1) filled.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data (depth 1)"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/query/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@Parameter(description = "DbId or StId of the requested database object", example = "R-HSA-1640170", required = true)
                                   @PathVariable String id) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, RelationshipDirection.OUTGOING);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for DatabaseObject for id: {}", id);
        return databaseObject;
    }

    @Operation(summary = "A single property of an entry in Reactome knowledgebase", description = "This method queries for a specific property of an entry in Reactome knowledgebase based on the given identifier, i.e. stable id or database id.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data or invalid attribute name"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/query/{id}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findById(@Parameter(description = "DbId or StId of the requested database object", example = "R-HSA-1640170", required = true)
                           @PathVariable String id,
                           @Parameter(description = "Attribute to be filtered", example = "displayName", required = true)
                           @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, RelationshipDirection.OUTGOING);
        if (databaseObject == null)
            throw new NotFoundTextPlainException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for DatabaseObject for id: {}", id);
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    @Operation(summary = "A list of entries in Reactome knowledgebase", description = "This method queries for a set of entries in Reactome knowledgebase based on the given list of identifiers. The provided list of identifiers can include stable ids, database ids or a mixture of both. It should be underlined that any duplicated ids are eliminated while only requests containing up to 20 ids are processed.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data or invalid attribute name"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/query/ids", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public Collection<DatabaseObject> findByIds(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "A comma separated list of identifiers",
                    required = true,
                    content = @Content(examples = @ExampleObject("R-HSA-1640170, R-HSA-109581, 199420"))
            )
            @RequestBody String post) {
        Collection<Object> ids = new ArrayList<>();
        for (String id : post.split(",|;|\\n|\\t")) {
            ids.add(id.trim());
        }
        if (ids.size() > 20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Collection<DatabaseObject> databaseObjects = advancedDatabaseObjectService.findByIds(ids, RelationshipDirection.OUTGOING);
        if (databaseObjects == null || databaseObjects.isEmpty())
            throw new NotFoundException("Ids: " + ids.toString() + " have not been found in the System");
        infoLogger.info("Request for DatabaseObjects for ids: {}", ids);
        return databaseObjects;
    }

    @Operation(summary = "A list of entries with their mapping to the provided identifiers", description = "This method queries for a set of entries in Reactome knowledgebase based on the given list of identifiers. The provided list of identifiers can include stable ids, database ids, old stable ids or a mixture of all. It should be underlined that any duplicated ids are eliminated while only requests containing up to 20 ids are processed.<br>This method is particularly useful for users that still rely on the previous version of stable identifiers to query this API. Please note that those are no longer part of the retrieved objects.")
    @RequestMapping(value = "/query/ids/map", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody //TODO: Swagger is not showing the defaultValue
    public Map<String, DatabaseObject> findByIdsMap(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "A comma separated list of identifiers",
                    required = true,
                    content = @Content(examples = @ExampleObject("R-HSA-1640170, R-HSA-109581, 199420"))
            )
            @RequestBody String post) {
        Collection<String> ids = new ArrayList<>();
        for (String id : post.split(",|;|\\n|\\t")) ids.add(id.trim());
        if (ids.size() > 20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Map<String, DatabaseObject> map = new HashMap<>();
        for (String id : ids) {
            DatabaseObject object = advancedDatabaseObjectService.findById(id, RelationshipDirection.OUTGOING);
            if (object != null) map.put(id, object);
        }
        if (map.isEmpty()) throw new NotFoundException("Ids: " + ids + " have not been found in the System");
        infoLogger.info("Request for DatabaseObjects for ids: {}", ids);
        return map;
    }

    @Operation(summary = "More information on an entry in Reactome knowledgebase", description = "Based on the given identifier, i.e. stable id or database id, this method queries for an entry in Reactome knowledgebase providing more information. In particular, the retrieved database object has all its properties and direct relationships (relationships of depth 1) filled, while it also includes any second level relationships regarding regulations and catalysts.")
    @RequestMapping(value = "/query/enhanced/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findEnhancedObjectById(@Parameter(description = "DbId or StId of the requested database object", example = "R-HSA-60140", required = true)
                                                 @PathVariable String id) {
        DatabaseObject databaseObject = needsIncomingRelationship(id) ?
                advancedDatabaseObjectService.findEnhancedObjectById(id) :
                advancedDatabaseObjectService.findEnhancedObjectByIdOutgoing(id);    //similar to findById
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        if (databaseObject instanceof ReferenceEntity) {

            List<GetterSetterMap.Accessor<?, ? extends PhysicalEntity, SummaryEntity>> accessors = List.of(
                    accessor(PhysicalEntity::getDisease, SummaryEntity::setDisease, PhysicalEntity.class),
//                    accessor(PhysicalEntity::getName, SummaryEntity::setName, PhysicalEntity.class),
                    accessor(PhysicalEntity::getCrossReference, SummaryEntity::setCrossReference, PhysicalEntity.class),
                    accessor(PhysicalEntity::getCompartment, SummaryEntity::setCompartment, PhysicalEntity.class),
                    accessor(PhysicalEntity::getComponentOf, SummaryEntity::setComponentOf, PhysicalEntity.class),
                    accessor(PhysicalEntity::getMemberOf, SummaryEntity::setMemberOf, PhysicalEntity.class),
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

            ReferenceEntity sourceRef = (ReferenceEntity) databaseObject;
            List<PhysicalEntity> physicalEntities = sourceRef.getPhysicalEntity();
            if (physicalEntities.isEmpty()) return databaseObject;
            databaseObject.preventLazyLoading = true;
            Comparator<PhysicalEntity> comparator = Comparator.comparingInt((PhysicalEntity pe) -> pe instanceof EntityWithAccessionedSequence ? ((EntityWithAccessionedSequence) pe).getModifiedResidues().size() : 0)
                    .thenComparingLong(PhysicalEntity::getDbId);
            physicalEntities.sort(comparator);
            physicalEntities.forEach(pe -> pe.preventLazyLoading = true);

            final SummaryEntity summary = new SummaryEntity();
            summary.preventLazyLoading = true;
            summary.setDisplayName(databaseObject.getDisplayName());
            summary.setStId(databaseObject.getStId());
            summary.setDbId(databaseObject.getDbId());
            summary.setName(sourceRef.getName());
            summary.setSummarisedEntities(physicalEntities);
            summary.setReferenceEntity(sourceRef);

            physicalEntities.forEach(entity -> accessors.forEach(accessor -> mergeProperty(summary, entity, accessor)));
            summary.setStId(databaseObject.getStId());
            return summary;
        }

        infoLogger.info("Request for enhanced DatabaseObject for id: {}", id);
        return databaseObject;
    }

    //##################### API Ignored but still available for internal purposes #####################//

    @Hidden //Kept for backwards compatibility. It can be removed when logs show no activity under this mapping
    @Operation(summary = "More information on an entry in Reactome knowledgebase", description = "Based on the given identifier, i.e. stable id or database id, this method queries for an entry in Reactome knowledgebase providing more information. In particular, the retrieved database object has all its properties and direct relationships (relationships of depth 1) filled, while it also includes any second level relationships regarding regulations and catalysts.")
    @RequestMapping(value = "/query/{id}/more", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findMoreObjectById(@Parameter(description = "DbId or StId of the requested database object", example = "R-HSA-60140", required = true)
                                             @PathVariable String id) {
        return findEnhancedObjectById(id);
    }

    @Hidden
    @Operation(summary = "Retrieves a DatabaseObject", description = "DatabaseObject will only be filled with primitive properties but no relationships")
    @RequestMapping(value = "/query/abridged/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findByIdNoRelations(@Parameter(description = "DbId or StId of the requested database object", example = "R-HSA-1640170", required = true)
                                              @PathVariable String id) {
        DatabaseObject databaseObject = databaseObjectService.findByIdNoRelations(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for abridged DatabaseObject for id: {}", id);
        return databaseObject;
    }

    @Hidden
    @Operation(summary = "Retrieves a DatabaseObject property", description = "Retrieves a single property from the DatabaseObject. Using this version it is not possible to retrieve any relationships")
    @RequestMapping(value = "/query/abridged/{id}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findByIdNoRelations(@Parameter(description = "DbId or StId of the requested database object", example = "R-HSA-1640170", required = true)
                                      @PathVariable String id,
                                      @Parameter(description = "Attribute to be filtered", example = "displayName", required = true)
                                      @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = databaseObjectService.findByIdNoRelations(id);
        if (databaseObject == null)
            throw new NotFoundTextPlainException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for abridged DatabaseObject for id: {}", id);
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    private boolean needsIncomingRelationship(String id) {
        boolean rtn = false;
        try {
            if (DatabaseObjectUtils.isStId(id)) {
                rtn = !id.startsWith("R-ALL-") && !id.startsWith("chebi:"); // Chemicals shouldn't fetch incoming relationships
            } else {
                String query = "MATCH (n:DatabaseObject{dbId:$id}) " +
                        "RETURN NOT ((n:Species) OR (n:Summation) OR (n:Person) OR (n:Compartment) OR (n:SimpleEntity))";
                rtn = advancedDatabaseObjectService.getCustomQueryResult(Boolean.class, query, Map.of("id", Long.valueOf(id)));
            }
        } catch (CustomQueryException | NullPointerException | NumberFormatException e) { /* Nothing here */ }
        return rtn;
    }


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
