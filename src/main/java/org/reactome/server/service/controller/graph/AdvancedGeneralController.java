package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.helper.RelationshipDirection;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 19.05.16.
 */
@RestController
@Api(tags = "advanced", description = "Reactome Data: Advanced queries.")
@RequestMapping("/data")
public class AdvancedGeneralController {


    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    // --------------------------------------- Enhanced Finder Methods -------------------------------------------------

    @ApiOperation(value = "Retrieves an extended DatabaseObject.",
            notes = "DatabaseObject will be filled with all properties, direct relationships and second level relationships of: regulations, catalysts.",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/more", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findEnhancedObjectById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-60140", required = true) @PathVariable String id) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findEnhancedObjectById(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return databaseObject;
    }

    @ApiOperation(value = "Retrieves an extended DatabaseObject.",
            notes = "DatabaseObject will be filled with all properties, direct relationships and second level relationships of: regulations, catalysts. Retrieves a single property of the DatabaseObject.",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/more/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object findEnhancedObjectById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-60140", required = true) @PathVariable String id,
                                         @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findEnhancedObjectById(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    // ---------------------- Methods with RelationshipDirection and Relationships -------------------------------------

    @ApiOperation(value = "Retrieves a DatabaseObject.",
            notes = "DatabaseObject will be filled with all properties and direct relationships of specified direction. Direction can be INCOMING, OUTGOING or UNDIRECTED",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/direction/{direction}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-199420", required = true) @PathVariable String id,
                                   @ApiParam(value = "Direction of mapped relationships", defaultValue = "OUTGOING", required = true) @PathVariable RelationshipDirection direction) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return databaseObject;
    }

    @ApiOperation(value = "Retrieves a DatabaseObject.",
            notes = "DatabaseObject will be filled with all properties and direct relationships of specified direction. Direction can be INCOMING, OUTGOING or UNDIRECTED. Retrieves a single property of the DatabaseObject.",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/direction/{direction}/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-199420", required = true) @PathVariable String id,
                           @ApiParam(value = "Direction of mapped relationships", defaultValue = "OUTGOING", required = true) @PathVariable RelationshipDirection direction,
                           @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    @ApiOperation(value = "Retrieves a DatabaseObject.",
            notes = "DatabaseObject will be filled with all properties and direct relationships of specified direction and relationship names. Direction can be INCOMING, OUTGOING or UNDIRECTED. ",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/direction/{direction}/relationships/{relationships}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-109581", required = true) @PathVariable String id,
                                   @ApiParam(value = "Direction of mapped relationships", defaultValue = "OUTGOING", required = true) @PathVariable RelationshipDirection direction,
                                   @ApiParam(value = "Relationship names that should be mapped", defaultValue = "hasEvent, regulatedBy", required = true) @PathVariable String[] relationships) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction, relationships);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return databaseObject;
    }

    @ApiOperation(value = "Retrieves a DatabaseObject.",
            notes = "DatabaseObject will be filled with all properties and direct relationships of specified direction and relationship names. Direction can be INCOMING, OUTGOING or UNDIRECTED. ",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/direction/{direction}/relationships/{relationships}/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-109581", required = true) @PathVariable String id,
                           @ApiParam(value = "Direction of mapped relationships", defaultValue = "OUTGOING", required = true) @PathVariable RelationshipDirection direction,
                           @ApiParam(value = "Relationship names that should be mapped", defaultValue = "hasEvent, regulatedBy", required = true) @PathVariable String[] relationships,
                           @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction, relationships);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

}
