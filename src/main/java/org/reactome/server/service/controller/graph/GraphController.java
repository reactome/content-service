package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.service.helper.RelationshipDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.03.16.
 */
@RestController
@Api(tags = "data", description = "Reactome Data ")
@RequestMapping("/data")
public class GraphController {

    @Autowired
    private GeneralService generalService;

    @ApiOperation(value = "Retrieves version number of current database",response = Integer.class, produces = "application/json")
    @RequestMapping(value = "/version", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Integer getDBVersion()  {
        return generalService.getDBVersion();
    }

    @ApiOperation(value = "Retrieves name of current database",response = String.class, produces = "application/json")
    @RequestMapping(value = "/getDBName", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getDBName()  {
        return generalService.getDBName();
    }

    //todo speciesList ??? right name here ?, rename
    @ApiOperation(value = "Retrieves all species",response = Species.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/speciesList", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Species> getAllSpecies()  {
        return generalService.getAllSpecies();
    }

    @ApiOperation(value = "Retrieves all diseases",response = String.class, produces = "text/plain")
    @RequestMapping(value = "/getDiseases", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getAllDiseases() {
        return StringUtils.join(generalService.findSimpleReferencesByClassName(Disease.class.getSimpleName()), "\n");
    }

    @ApiOperation(value = "Retrieves all reference Molecules",response = String.class, produces = "text/plain")
    @RequestMapping(value = "/getReferenceMolecules", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getAllReferenceMolecules() {
        return StringUtils.join(generalService.findSimpleReferencesByClassName(ReferenceMolecule.class.getSimpleName()), "\n");
    }

    @ApiOperation(value = "Retrieves all Uniprot Reference Sequences",response = String.class, produces = "text/plain")
    @RequestMapping(value = "/getUniProtRefSeqs", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getAllUniProtRefSeqs() {
        return StringUtils.join(generalService.findSimpleReferencesByClassName(ReferenceGeneProduct.class.getSimpleName()), "\n");
    }


    @ApiOperation(value = "Retrieves all Persons with name",response = Person.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/queryPersonByEmail/{email:.+}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Person> findPersonByEmail(@PathVariable String email) {
        return generalService.findAllByProperty(Person.class,"eMailAddress", email, 0);
    }

    @ApiOperation(value = "Retrieves a list of databaseObjects for given class name",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/details/{className:.*}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<DatabaseObject> getDatabaseObjectsForClassName(@ApiParam(defaultValue = "Species",required = true) @PathVariable String className,
                                                                     @ApiParam(defaultValue = "1", required = true) @RequestParam(required = true) Integer page,
                                                                     @ApiParam(defaultValue = "25", required = true) @RequestParam(required = true) Integer offset) throws ClassNotFoundException {
        return generalService.findObjectsByClassName(className,page,offset);
    }

    @ApiIgnore
    @RequestMapping(value = "/details/legacy/{className:.*}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<DatabaseObject> getDatabaseObjectsForClassNameLegacy(@PathVariable String className) throws ClassNotFoundException {
        return generalService.findObjectsByClassName(className);
    }


    @ApiOperation(value = "Retrieves details of a databaseObject",response = DatabaseObject.class, produces = "application/json")
    @RequestMapping(value = "/detail2/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findByIdWithOutgoingRelations(@ApiParam(defaultValue = "R-HSA-1640170",required = true) @PathVariable String id) throws IOException {
        DatabaseObject databaseObject = generalService.find(id, RelationshipDirection.OUTGOING);
        return databaseObject;
    }

}