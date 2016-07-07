package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DetailsService;
import org.reactome.server.graph.service.helper.ContentDetails;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RestController
@Api(tags = "query", description = "Reactome Data: Common data retrieval")
@RequestMapping("/data")
public class QueryObjectController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private DatabaseObjectService databaseObjectService;

    @Autowired
    private DetailsService detailsService;

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    @ApiOperation(value = "An entry in Reactome knowledgebase", notes = "This method queries for an entry in Reactome knowledgebase based on the given identifier, i.e. stable id or database id. It is worth mentioning that the retrieved database object has all its properties and direct relationships (relationships of depth 1) filled.")
    @RequestMapping(value = "/query/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@ApiParam(value = "DbId or StId of the requested database object", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id) {
        DatabaseObject databaseObject = databaseObjectService.findById(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for DatabaseObject for id: {}", id);
        return databaseObject;
    }

    @ApiOperation(value = "A single property of an entry in Reactome knowledgebase",  notes = "This method queries for a specific property of an entry in Reactome knowledgebase based on the given identifier, i.e. stable id or database id.")
    @RequestMapping(value = "/query/{id}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findById(@ApiParam(value = "DbId or StId of the requested database object", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id,
                           @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = databaseObjectService.findById(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for DatabaseObject for id: {}", id);
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    @ApiOperation(value = "A list of entries in Reactome knowledgebase", notes = "This method queries for a set of entries in Reactome knowledgebase based on the given list of identifiers. The provided list of identifiers can include stable ids, database ids or a mixture of both. It should be underlined that any duplicated ids are eliminated while only requests containing up to 20 ids are processed.")
    @RequestMapping(value = "/query/ids", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody //TODO: Swagger is not showing the defaultValue
    public Collection<DatabaseObject> findByIds(@ApiParam(value = "A comma separated list of identifiers", defaultValue = "R-HSA-1640170, R-HSA-109581, 199420", required = true)  @RequestBody String post) {
        Collection<String> ids = new ArrayList<>();
        for (String id : post.split(",|;|\\n|\\t")) {
            ids.add(id.trim());
        }
        if (ids.size() > 20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Collection<DatabaseObject> databaseObjects = databaseObjectService.findByIdsNoRelations(ids);
        if (databaseObjects == null || databaseObjects.isEmpty())
            throw new NotFoundException("Ids: " + ids.toString() + " have not been found in the System");
        infoLogger.info("Request for DatabaseObjects for ids: {}", ids);
        return databaseObjects;
    }

    @ApiOperation(value = "A list of entries with their mapping to the provided identifiers", notes = "This method queries for a set of entries in Reactome knowledgebase based on the given list of identifiers. The provided list of identifiers can include stable ids, database ids, old stable ids or a mixture of all. It should be underlined that any duplicated ids are eliminated while only requests containing up to 20 ids are processed.<br>This method is particularly useful for users that still rely on the previous version of stable identifiers to query this API. Please note that those are no longer part of the retrieved objects.")
    @RequestMapping(value = "/query/ids/map", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody //TODO: Swagger is not showing the defaultValue
    public Map<String, DatabaseObject> findByIdsMap(@ApiParam(value = "A comma separated list of identifiers ", defaultValue = "R-HSA-1640170, R-HSA-109581, 199420", required = true)
                                                    @RequestBody String post) {
        Collection<String> ids = new ArrayList<>();
        for (String id : post.split(",|;|\\n|\\t")) ids.add(id.trim());
        if (ids.size() > 20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Map<String, DatabaseObject> map = new HashMap<>();
        for (String id : ids) {
            DatabaseObject object = databaseObjectService.findById(id);
            if (object != null) map.put(id, object);
        }
        if (map.isEmpty()) throw new NotFoundException("Ids: " + ids.toString() + " have not been found in the System");
        infoLogger.info("Request for DatabaseObjects for ids: {}", ids);
        return map;
    }

    @ApiOperation(value = "More information on an entry in Reactome knowledgebase", notes = "Based on the given identifier, i.e. stable id or database id, this method queries for an entry in Reactome knowledgebase providing more information. In particular, the retrieved database object has all its properties and direct relationships (relationships of depth 1) filled, while it also includes any second level relationships regarding regulations and catalysts.")
    @RequestMapping(value = "/query/{id}/more", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findEnhancedObjectById(@ApiParam(value = "DbId or StId of the requested database object", defaultValue = "R-HSA-60140", required = true) @PathVariable String id) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findEnhancedObjectById(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for enhanced DatabaseObject for id: {}", id);
        return databaseObject;
    }

    @ApiOperation(value = "Extended information about an entry in Reactome knowledgebase", notes = "ContentDetails contains: DatabaseObject, componentsOf, other forms of the entry, locationsTree")
    @RequestMapping(value = "/query/{id}/extended", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ContentDetails getContentDetail(@ApiParam(value = "DbId or StId of the requested database object", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id,
                                           @ApiParam(value = "Include direct participants (proteins or molecules directly involved in Reactions)", defaultValue = "false") @RequestParam(required = false) Boolean directParticipants) {
        ContentDetails contentDetails = detailsService.getContentDetails(id, directParticipants);
        if (contentDetails == null || contentDetails.getDatabaseObject() == null)
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for extended DatabaseObject for id: {}", id);
        return contentDetails;
    }

    //##################### API Ignored but still available for internal purposes #####################//

    @ApiIgnore
    @ApiOperation(value = "Retrieves a DatabaseObject", notes = "DatabaseObject will only be filled with primitive properties but no relationships")
    @RequestMapping(value = "/query/{id}/less", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findByIdNoRelations(@ApiParam(value = "DbId or StId of the requested database object", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id) {

        DatabaseObject databaseObject = databaseObjectService.findByIdNoRelations(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for abridged DatabaseObject for id: {}", id);
        return databaseObject;
    }

    @ApiIgnore
    @ApiOperation(value = "Retrieves a DatabaseObject property", notes = "Retrieves a single property from the DatabaseObject. Using this version it is not possible to retrieve any relationships")
    @RequestMapping(value = "/query/{id}/less/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findByIdNoRelations(@ApiParam(value = "DbId or StId of the requested database object", defaultValue = "R-HSA-1640170", required = true) @PathVariable String id,
                                      @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = databaseObjectService.findById(id);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for abridged DatabaseObject for id: {}", id);
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }
}
