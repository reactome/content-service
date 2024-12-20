package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.domain.result.SimpleReferenceObject;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.graph.service.helper.SchemaNode;
import org.reactome.server.graph.service.util.DatabaseObjectUtils;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "schema", description = "Reactome Data: Schema class queries")
@RequestMapping("/data")
public class SchemaController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    private SchemaNode cacheSchema;

    @Autowired
    private SchemaService schemaService;
    @Autowired
    private GeneralService generalService;

    @Operation(summary = "A list of entries corresponding to a given schema class", description = "This method retrieves the list of entries in Reactome that belong to the specified schema class. Please take into account that if species is specified to filter the results, schema class needs to be an instance of Event or PhysicalEntity. Additionally, paging is required, while a maximum of 25 entries can be returned per request.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Schema class does not match with any current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/schema/{className}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<DatabaseObject> getDatabaseObjectsForClassName(@Parameter(description = "Schema class name", example = "Pathway", required = true) @PathVariable String className,
                                                                     @Parameter(description = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", example = "9606") @RequestParam(required = false) String species,
                                                                     @Parameter(description = "Page to be returned", example = "1", required = true) @RequestParam Integer page,
                                                                     @Parameter(description = "Number of rows returned. Maximum = 25", example = "25", required = true) @RequestParam Integer offset) throws ClassNotFoundException {
        if (offset > 25) offset = 25;
        Collection<DatabaseObject> databaseObjects;
        if (species == null) {
            databaseObjects = schemaService.getByClassName(className, page, offset);
        } else {
            databaseObjects = schemaService.getByClassName(className, species, page, offset);
        }
        if (databaseObjects == null || databaseObjects.isEmpty())
            throw new NotFoundException("No entries found for class: " + className);
        infoLogger.info("Request for objects of class: {}", className, species);
        return databaseObjects;
    }

    @Operation(summary = "A list of simplified entries corresponding to a given schema class", description = "This method retrieves the list of simplified entries in Reactome that belong to the specified schema class. A simplified entry may be considered as a minimised version of the full database object that includes its database id, stable id, displayName and type. Please take into account that if species is specified to filter the results, schema class needs to be an instance of Event or PhysicalEntity. Also, paging is required, while a maximum of 20000 entries can be returned per request.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Schema class does not match with any current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/schema/{className}/min", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleDatabaseObject> getSimpleDatabaseObjectByClassName(@Parameter(description = "Schema class name", example = "Pathway", required = true) @PathVariable String className,
                                                                               @Parameter(description = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", example = "9606") @RequestParam(required = false) String species,
                                                                               @Parameter(description = "Page to be returned", example = "1", required = true) @RequestParam Integer page,
                                                                               @Parameter(description = "Number of rows returned. Maximum = 20000", example = "20000", required = true) @RequestParam Integer offset) throws ClassNotFoundException {
        if (offset > 20000) offset = 20000;
        Collection<SimpleDatabaseObject> simpleDatabaseObjects;
        if (species == null) {
            simpleDatabaseObjects = schemaService.getSimpleDatabaseObjectByClassName(className, page, offset);
        } else {
            simpleDatabaseObjects = schemaService.getSimpleDatabaseObjectByClassName(className, species, page, offset);
        }
        if (simpleDatabaseObjects == null || simpleDatabaseObjects.isEmpty())
            throw new NotFoundException("No entries found for class: " + className);
        infoLogger.info("Request for simple objects of class: {}", className, species);
        return simpleDatabaseObjects;
    }

    @Operation(summary = "A list of simplified reference objects corresponding to a given schema class", description = "This method retrieves the list of simplified reference objects that belong to the specified schema class. A reference object includes its database id, external identifier, and external database name. Please take into account that schema class needs to be an instance of ReferenceEntity or ExternalOntology. Also, paging is required, while a maximum of 20000 entries can be returned per request.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Schema class does not match with any current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/schema/{className}/reference", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<SimpleReferenceObject> getSimpleReferencesObjectsByClassName(@Parameter(description = "Schema class name. Class needs to an instance of ReferenceEntity or ExternalOntology", example = "ReferenceMolecule", required = true) @PathVariable String className,
                                                                                   @Parameter(description = "Page to be returned", example = "1", required = true) @RequestParam Integer page,
                                                                                   @Parameter(description = "Number of rows returned. Maximum = 20000", example = "20000", required = true) @RequestParam Integer offset) throws ClassNotFoundException {
        if (offset > 20000) offset = 20000;
        Collection<SimpleReferenceObject> simpleReferenceObjects = schemaService.getSimpleReferencesObjectsByClassName(className, page, offset);
        if (simpleReferenceObjects == null || simpleReferenceObjects.isEmpty())
            throw new NotFoundException("No entries found for class: " + className);
        infoLogger.info("Request for reference objects of class: {}", className);
        return simpleReferenceObjects;
    }

    @Operation(summary = "Number of entries belonging to the specified schema class", description = "This method counts the total number of entries in Reactome that belong to the specified schema class. Please take into account that if species is specified to filter the results, schema class needs to be an instance of Event or PhysicalEntity.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Schema class does not match with any current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/schema/{className}/count", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Long countEntries(@Parameter(description = "Schema class name", example = "Pathway", required = true) @PathVariable String className,
                             @Parameter(description = "Allowed species filter: SpeciesName (eg: Homo sapiens) SpeciesTaxId (eg: 9606)", example = "9606") @RequestParam(required = false) String species) throws ClassNotFoundException {
        infoLogger.info("Request for count of objects of class: {}, species: {}", className, species);
        if (species == null) {
            return schemaService.countEntries(className);
        } else {
            Long count = schemaService.countEntries(className, species);
            if (count == null || count == 0)
                throw new NotFoundException("No entries have been found for species: " + species);
            return count;
        }
    }

    @Operation(summary = "A list of Reactome data model", description = "This method retrieves a full specification of all Reactome classes.")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/schema/model", method = RequestMethod.GET, produces = {"application/json"})
    public SchemaNode getSchemaModel() {
        if (cacheSchema == null) {
            try {
                cacheSchema = DatabaseObjectUtils.getGraphModelTree(generalService.getSchemaClassCounts());
            } catch (ClassNotFoundException ignored) {
            }
        }
        return cacheSchema;
    }
}
