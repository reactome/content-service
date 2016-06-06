package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.domain.result.SimpleReferenceObject;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@RestController
@Api(tags = "schema", description = "Reactome Data: Schema class based queries.")
@RequestMapping("/data")
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    @ApiOperation(value = "Retrieves a list of DatabaseObjects for given class name.",
            notes = "If species is specified result will be filtered. If species is specified, Schema class needs to an instance of Event or PhysicalEntity. Paging is required. A maximum of 25 entries can be returned per request",
            produces = "application/json")
    @RequestMapping(value = "/schema/{className}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<DatabaseObject> getDatabaseObjectsForClassName(@ApiParam(value = "Schema class name", defaultValue = "Pathway",required = true) @PathVariable String className,
                                                                     @ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606") @RequestParam(required = false) String species,
                                                                     @ApiParam(value = "Page to be returned", defaultValue = "1", required = true)  @RequestParam Integer page,
                                                                     @ApiParam(value = "Number of rows returned. Maximum = 25", defaultValue = "25", required = true) @RequestParam Integer offset) throws ClassNotFoundException {
        if (offset > 25) offset = 25;
        Collection<DatabaseObject> databaseObjects;
        if (species == null) {
            databaseObjects = schemaService.getByClassName(className, page, offset);
        } else {
            databaseObjects = schemaService.getByClassName(className, species, page, offset);
        }
        if (databaseObjects == null || databaseObjects.isEmpty()) throw new NotFoundException("No entries found for class: " + className);
        return databaseObjects;
    }

    @ApiOperation(value = "Retrieves a list of DatabaseObjects for given class name.",
            notes = "Retrieves a single property from the list of DatabaseObjects. Using this version it is not possible to retrieve any relationships. If species is specified result will be filtered. If species is specified, Schema class needs to an instance of Event or PhysicalEntity. Paging is required. A maximum of 25 entries can be returned per request",
            produces = "application/json")
    @RequestMapping(value = "/schema/{className}/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Object> getDatabaseObjectsForClassName(@ApiParam(value = "Schema class name", defaultValue = "Pathway",required = true) @PathVariable String className,
                                                             @ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606") @RequestParam(required = false) String species,
                                                             @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName,
                                                             @ApiParam(value = "Page to be returned", defaultValue = "1", required = true)  @RequestParam Integer page,
                                                             @ApiParam(value = "Number of rows returned. Maximum = 25", defaultValue = "25", required = true) @RequestParam Integer offset) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        if (offset > 25) offset = 25;
        Collection<DatabaseObject> databaseObjects;
        if (species == null) {
            databaseObjects = schemaService.getByClassName(className, page, offset);
        } else {
            databaseObjects = schemaService.getByClassName(className, species, page, offset);
        }
        if (databaseObjects == null || databaseObjects.isEmpty()) throw new NotFoundException("No entries found for class: " + className);
        return ControllerUtils.getProperties(databaseObjects, attributeName);
    }

    @ApiOperation(value = "Retrieves a list of SimpleDatabaseObjects for given class name.",
            notes = "SimpleDatabaseObject contains dbId, stId, displayName and the type. If species is specified result will be filtered. If species is specified, Schema class needs to an instance of Event or PhysicalEntity. Paging is required. A maximum of 20000 entries can be returned per request",
            response = SimpleDatabaseObject.class,
            responseContainer = "List",
            produces = "application/json")
    @RequestMapping(value = "/schema/{className}/less", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getSimpleDatabaseObjectByClassName(@ApiParam(value = "Schema class name", defaultValue = "Pathway",required = true) @PathVariable String className,
                                                                               @ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606") @RequestParam(required = false) String species,
                                                                               @ApiParam(value = "Page to be returned", defaultValue = "1", required = true)  @RequestParam Integer page,
                                                                               @ApiParam(value = "Number of rows returned. Maximum = 20000", defaultValue = "20000", required = true) @RequestParam Integer offset) throws ClassNotFoundException {
        if (offset > 20000) offset = 20000;
        Collection<SimpleDatabaseObject> simpleDatabaseObjects;
        if (species == null) {
            simpleDatabaseObjects = schemaService.getSimpleDatabaseObjectByClassName(className, page, offset);
        } else {
            simpleDatabaseObjects = schemaService.getSimpleDatabaseObjectByClassName(className, species, page, offset);
        }
        if (simpleDatabaseObjects == null || simpleDatabaseObjects.isEmpty()) throw new NotFoundException("No entries found for class: " + className);
        return simpleDatabaseObjects;
    }

    @ApiOperation(value = "Retrieves a list of SimpleDatabaseObjects for given class name.",
            notes = "SimpleDatabaseObject contains dbId, stId, displayName and the type. Retrieves a single property from the list of SimpleDatabaseObjects. If species is specified result will be filtered. If species is specified, Schema class needs to an instance of Event or PhysicalEntity. Paging is required. A maximum of 20000 entries can be returned per request",
            response = SimpleDatabaseObject.class,
            responseContainer = "List",
            produces = "application/json")
    @RequestMapping(value = "/schema/{className}/less/{attributeName}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<Object> getSimpleDatabaseObjectByClassName(@ApiParam(value = "Schema class name", defaultValue = "Pathway",required = true) @PathVariable String className,
                                                                 @ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606") @RequestParam(required = false) String species,
                                                                 @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName,
                                                                 @ApiParam(value = "Page to be returned", defaultValue = "1", required = true)  @RequestParam Integer page,
                                                                 @ApiParam(value = "Number of rows returned. Maximum = 20000", defaultValue = "20000", required = true) @RequestParam Integer offset) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        if (offset > 20000) offset = 20000;
        Collection<SimpleDatabaseObject> simpleDatabaseObjects;
        if (species == null) {
            simpleDatabaseObjects = schemaService.getSimpleDatabaseObjectByClassName(className, page, offset);
        } else {
            simpleDatabaseObjects = schemaService.getSimpleDatabaseObjectByClassName(className, species, page, offset);
        }
        if (simpleDatabaseObjects == null || simpleDatabaseObjects.isEmpty()) throw new NotFoundException("No entries found for class: " + className);
        return ControllerUtils.getProperties(simpleDatabaseObjects, attributeName);
    }

    @ApiOperation(value = "Retrieves of SimpleReferenceObjects for given class name.",
            notes = "SimpleReferenceObjects contains dbId, external identifier and external database name. Schema class needs to an instance of ReferenceEntity or ExternalOntology. Paging is required. A maximum of 20000 entries can be returned per request",
            response = SimpleReferenceObject.class,
            responseContainer = "List",
            produces = "text/plain")
    @RequestMapping(value = "/schema/{className}/reference", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleReferenceObject> getSimpleReferencesObjectsByClassName(@ApiParam(value = "Schema class name. Class needs to an instance of ReferenceEntity or ExternalOntology", defaultValue = "ReferenceMolecule",required = true) @PathVariable String className,
                                                                                   @ApiParam(value = "Page to be returned", defaultValue = "1", required = true)  @RequestParam Integer page,
                                                                                   @ApiParam(value = "Number of rows returned. Maximum = 20000", defaultValue = "20000", required = true) @RequestParam Integer offset) throws ClassNotFoundException {
        if (offset > 20000) offset = 20000;
        Collection<SimpleReferenceObject> simpleReferenceObjects = schemaService.getSimpleReferencesObjectsByClassName(className, page, offset);
        if (simpleReferenceObjects == null || simpleReferenceObjects.isEmpty()) throw new NotFoundException("No entries found for class: " + className);
        return simpleReferenceObjects;
    }

    @ApiOperation(value = "Retrieves of SimpleReferenceObjects for given class name.",
            notes = "SimpleReferenceObjects contains dbId, external identifier and external database name. Retrieves a single property from the list of SimpleReferenceObjects. Schema class needs to an instance of ReferenceEntity or ExternalOntology. Paging is required. A maximum of 20000 entries can be returned per request",
            response = SimpleReferenceObject.class,
            responseContainer = "List",
            produces = "text/plain")
    @RequestMapping(value = "/schema/{className}/reference/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Object> getSimpleReferencesObjectsByClassName(@ApiParam(value = "Schema class name. Class needs to an instance of ReferenceEntity or ExternalOntology", defaultValue = "ReferenceMolecule",required = true) @PathVariable String className,
                                                                                   @ApiParam(value = "Attribute to be filtered", defaultValue = "identifier", required = true) @PathVariable String attributeName,
                                                                                   @ApiParam(value = "Page to be returned", defaultValue = "1", required = true)  @RequestParam Integer page,
                                                                                   @ApiParam(value = "Number of rows returned. Maximum = 20000", defaultValue = "20000", required = true) @RequestParam Integer offset) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        if (offset > 20000) offset = 20000;
        Collection<SimpleReferenceObject> simpleReferenceObjects = schemaService.getSimpleReferencesObjectsByClassName(className, page, offset);
        if (simpleReferenceObjects == null || simpleReferenceObjects.isEmpty()) throw new NotFoundException("No entries found for class: " + className);
        return ControllerUtils.getProperties(simpleReferenceObjects, attributeName);
    }

    @ApiOperation(value = "Counts entries of Schema class specified.",
            notes = "If species is specified result will be filtered. If species is specified, Schema class needs to an instance of Event or PhysicalEntity.",
            response = Long.class,
            produces = "application/json")
    @RequestMapping(value = "/schema/{className}/count", method = RequestMethod.GET)
    @ResponseBody
    public Long countEntries(@ApiParam(value = "Schema class name", defaultValue = "Pathway",required = true) @PathVariable String className,
                             @ApiParam(value = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", defaultValue = "9606") @RequestParam(required = false) String species) throws ClassNotFoundException {
        if (species == null) {
            return schemaService.countEntries(className);
        } else {
            Long count = schemaService.countEntries(className, species);
            if (count == null || count == 0) throw new NotFoundException("No entries have been found for species: " + species);
            return count;
        }
    }


}
