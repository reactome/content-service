package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.helper.RelationshipDirection;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 19.05.16.
 */
//@RestController
@Hidden
@Tag(name = "advanced")
@RequestMapping("/data")
@Deprecated
public class AdvancedGeneralController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    // ---------------------- Methods with RelationshipDirection and Relationships -------------------------------------

    @Operation(summary = "Retrieves a DatabaseObject", description = "DatabaseObject will be filled with all properties and direct relationships of specified direction. Direction can be INCOMING, OUTGOING or UNDIRECTED")
    @RequestMapping(value = "/detail/{id}/direction/{direction}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@Parameter(description = "DbId or StId of a DatabaseObject", example = "R-HSA-199420", required = true) @PathVariable String id,
                                   @Parameter(description = "Direction of mapped relationships", example = "OUTGOING", required = true) @PathVariable RelationshipDirection direction) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Advanced Request for DatabaseObject for id: {}", id);
        return databaseObject;
    }

    @Operation(summary = "Retrieves a DatabaseObject", description ="DatabaseObject will be filled with all properties and direct relationships of specified direction. Direction can be INCOMING, OUTGOING or UNDIRECTED. Retrieves a single property of the DatabaseObject")
    @RequestMapping(value = "/detail/{id}/direction/{direction}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findById(@Parameter(description = "DbId or StId of a DatabaseObject", example = "R-HSA-199420", required = true) @PathVariable String id,
                           @Parameter(description = "Direction of mapped relationships", example = "OUTGOING", required = true) @PathVariable RelationshipDirection direction,
                           @Parameter(description = "Attribute to be filtered", example = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Advanced Request for DatabaseObject for id: {}", id);
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    @Operation(summary = "Retrieves a DatabaseObject", description ="DatabaseObject will be filled with all properties and direct relationships of specified direction and relationship names. Direction can be INCOMING, OUTGOING or UNDIRECTED")
    @RequestMapping(value = "/detail/{id}/direction/{direction}/relationships/{relationships}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@Parameter(description = "DbId or StId of a DatabaseObject", example = "R-HSA-109581", required = true) @PathVariable String id,
                                   @Parameter(description = "Direction of mapped relationships", example = "OUTGOING", required = true) @PathVariable RelationshipDirection direction,
                                   @Parameter(description = "Relationship names that should be mapped", example = "hasEvent, regulatedBy", required = true) @PathVariable String[] relationships) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction, relationships);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Advanced Request for DatabaseObject for id: {}", id);
        return databaseObject;
    }

    @Operation(summary = "Retrieves a DatabaseObject", description ="DatabaseObject will be filled with all properties and direct relationships of specified direction and relationship names. Direction can be INCOMING, OUTGOING or UNDIRECTED")
    @RequestMapping(value = "/detail/{id}/direction/{direction}/relationships/{relationships}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findById(@Parameter(description = "DbId or StId of a DatabaseObject", example = "R-HSA-109581", required = true) @PathVariable String id,
                           @Parameter(description = "Direction of mapped relationships", example = "OUTGOING", required = true) @PathVariable RelationshipDirection direction,
                           @Parameter(description = "Relationship names that should be mapped", example = "hasEvent, regulatedBy", required = true) @PathVariable String[] relationships,
                           @Parameter(description = "Attribute to be filtered", example = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction, relationships);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Advanced Request for DatabaseObject for id: {}", id);
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

}
