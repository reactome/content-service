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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 19.05.16.
 */
//@RestController
@ApiIgnore
@Api(tags = "advanced", description = "Reactome Data: Advanced queries")
@RequestMapping("/data")
@Deprecated
public class AdvancedGeneralController {


    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    // ---------------------- Methods with RelationshipDirection and Relationships -------------------------------------

    @ApiOperation(value = "Retrieves a DatabaseObject", notes = "DatabaseObject will be filled with all properties and direct relationships of specified direction. Direction can be INCOMING, OUTGOING or UNDIRECTED")
    @RequestMapping(value = "/detail/{id}/direction/{direction}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-199420", required = true) @PathVariable String id,
                                   @ApiParam(value = "Direction of mapped relationships", defaultValue = "OUTGOING", required = true) @PathVariable RelationshipDirection direction) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return databaseObject;
    }

    @ApiOperation(value = "Retrieves a DatabaseObject", notes = "DatabaseObject will be filled with all properties and direct relationships of specified direction. Direction can be INCOMING, OUTGOING or UNDIRECTED. Retrieves a single property of the DatabaseObject")
    @RequestMapping(value = "/detail/{id}/direction/{direction}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-199420", required = true) @PathVariable String id,
                           @ApiParam(value = "Direction of mapped relationships", defaultValue = "OUTGOING", required = true) @PathVariable RelationshipDirection direction,
                           @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    @ApiOperation(value = "Retrieves a DatabaseObject", notes = "DatabaseObject will be filled with all properties and direct relationships of specified direction and relationship names. Direction can be INCOMING, OUTGOING or UNDIRECTED")
    @RequestMapping(value = "/detail/{id}/direction/{direction}/relationships/{relationships}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-109581", required = true) @PathVariable String id,
                                   @ApiParam(value = "Direction of mapped relationships", defaultValue = "OUTGOING", required = true) @PathVariable RelationshipDirection direction,
                                   @ApiParam(value = "Relationship names that should be mapped", defaultValue = "hasEvent, regulatedBy", required = true) @PathVariable String[] relationships) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction, relationships);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return databaseObject;
    }

    @ApiOperation(value = "Retrieves a DatabaseObject", notes = "DatabaseObject will be filled with all properties and direct relationships of specified direction and relationship names. Direction can be INCOMING, OUTGOING or UNDIRECTED")
    @RequestMapping(value = "/detail/{id}/direction/{direction}/relationships/{relationships}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-109581", required = true) @PathVariable String id,
                           @ApiParam(value = "Direction of mapped relationships", defaultValue = "OUTGOING", required = true) @PathVariable RelationshipDirection direction,
                           @ApiParam(value = "Relationship names that should be mapped", defaultValue = "hasEvent, regulatedBy", required = true) @PathVariable String[] relationships,
                           @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, direction, relationships);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

}
