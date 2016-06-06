package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 19.05.16.
 */
@RestController
@Api(tags = "basic", description = "Reactome Data: Basic queries.")
@RequestMapping("/data")
public class DatabaseObjectController {

    @Autowired
    private DatabaseObjectService databaseObjectService;

    @ApiOperation(value = "Retrieves a DatabaseObject",
            notes = "DatabaseObject will be filled with all properties and direct relationships (relationships of depth 1).",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id)  {
        DatabaseObject databaseObject = databaseObjectService.findById(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return databaseObject;
    }

    @ApiOperation(value = "Retrieves a DatabaseObject",
            notes = "Retrieves a single property from the DatabaseObject.",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object findById(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id,
                           @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true)   @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = databaseObjectService.findById(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    @ApiOperation(value = "Retrieves a DatabaseObject.",
            notes = "DatabaseObject will only be filled with primitive properties but no relationships",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/less", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findByIdNoRelations(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id)  {

        DatabaseObject databaseObject = databaseObjectService.findByIdNoRelations(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return databaseObject;
    }

    @ApiOperation(value = "Retrieves a DatabaseObject property",
            notes = "Retrieves a single property from the DatabaseObject. Using this version it is not possible to retrieve any relationships.",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/less/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object findByIdNoRelations(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id,
                                      @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true)   @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = databaseObjectService.findById(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    @ApiOperation(value = "Retrieves a list of DatabaseObjects.",
            notes = "Ids can be StIds, DbIds or a mixed list of both. Duplicated ids will be eliminated. A maximum of 20 ids per request will be processed",
            produces = "application/json")
    @RequestMapping(value = "/details/{ids}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<DatabaseObject> findByIds(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-1640170, R-HSA-109581, 199420", required = true) @PathVariable Collection<String> ids)  {
        if (ids.size()>20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Collection<DatabaseObject> databaseObjects = databaseObjectService.findByIdsNoRelations(ids);
        if (databaseObjects == null || databaseObjects.isEmpty()) throw new NotFoundException("Ids: " + ids.toString() + " have not been found in the System");
        return databaseObjects;
    }

    @ApiOperation(value = "Retrieves a list of DatabaseObject properties",
            notes = "Retrieves a single property from a list of DatabaseObjects, using this version it is not possible to retrieve any relationships. Ids can be StIds, DbIds or a mixed list of both. Duplicated ids will be eliminated. A maximum of 20 ids per request will be processed",
            response = Object.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/details/{ids}/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Object> findByIds(@ApiParam(value = "DbId or StId of a DatabaseObject", defaultValue = "R-HSA-1640170, R-HSA-109581, 199420", required = true) @PathVariable Collection<String> ids,
                                        @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        if (ids.size()>20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Collection<DatabaseObject> databaseObjects = databaseObjectService.findByIdsNoRelations(ids);
        if (databaseObjects == null || databaseObjects.isEmpty()) throw new NotFoundException("Ids: " + ids.toString() + " have not been found in the System");
        return ControllerUtils.getProperties(databaseObjects, attributeName);
    }

}
