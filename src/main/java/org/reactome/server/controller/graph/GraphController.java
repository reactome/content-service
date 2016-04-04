package org.reactome.server.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import org.reactome.server.tools.domain.model.DatabaseObject;
import org.reactome.server.tools.domain.model.Pathway;
import org.reactome.server.tools.domain.model.PhysicalEntity;
import org.reactome.server.tools.domain.model.Species;
import org.reactome.server.tools.domain.result.Participant;
import org.reactome.server.tools.service.DatabaseObjectService;
import org.reactome.server.tools.service.GenericService;

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
    private DatabaseObjectService databaseObjectService;
    @Autowired
    private GenericService genericService;
//    @Autowired
//    private EventService eventService;
//    @Autowired
//    private PhysicalEntityService physicalEntityService;

    @ApiOperation(value = "Retrieves all Reactome top level pathways",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/topLevelPathway", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getTopLevelPathways()  {
        return genericService.getTopLevelPathways();
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways for given species name",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/topLevelPathway/{speciesName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getTopLevelPathways(@ApiParam(defaultValue = "Homo sapiens",required = true) @PathVariable String speciesName)  {
        return genericService.getTopLevelPathways(speciesName);
    }

    @ApiOperation(value = "Retrieves all species",response = Species.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/species", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Species> getSpecies()  {
        return genericService.getSpecies();
    }

    @ApiOperation(value = "Retrieves details of a databaseObject",response = DatabaseObject.class, produces = "application/json")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject queryById(@ApiParam(defaultValue = "R-HSA-1640170",required = true) @PathVariable String id)  {
        return databaseObjectService.findById(id);
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participants", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getParticipatingPhysicalEntities(@ApiParam(defaultValue = "R-HSA-5205685",required = true) @PathVariable String id)  {
        return databaseObjectService.getParticipatingMolecules4(id);
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",response = Participant.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participants2", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Participant> getParticipatingPhysicalEntities2(@ApiParam(defaultValue = "5205685",required = true) @PathVariable Long id)  {
        return databaseObjectService.getParticipatingMolecules3(id);
    }

    @ApiOperation(value = "Retrieves a list of databaseObjects for given class name",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/details/{className:.*}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<DatabaseObject> getDatabaseObjectsForClassName(@ApiParam(defaultValue = "Species",required = true) @PathVariable String className,
                                                               @ApiParam(defaultValue = "1", required = true) @RequestParam(required = true) Integer page,
                                                               @ApiParam(defaultValue = "25", required = true) @RequestParam(required = true) Integer offset) throws ClassNotFoundException {
        return genericService.getObjectsByClassName(className,page,offset);

    }

    @ApiIgnore
    @RequestMapping(value = "/details/legacy/{className:.*}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<DatabaseObject> getDatabaseObjectsForClassNameLegacy(@PathVariable String className) throws ClassNotFoundException {
        return genericService.getObjectsByClassName(className,1, 2000000); //Integer.MAX_VALUE is too big for neo4j
    }

//    @ApiOperation(value = "Retrieves details of a databaseObject",response = DatabaseObject.class, produces = "application/json")
//    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json")
//    @ResponseBody
//    public Event queryById(@ApiParam(defaultValue = "R-HSA-1640170",required = true) @PathVariable String id)  {
//        return eventService.findByIdWithLegacyFields(id);
//    }
//
//    @ApiOperation(value = "Retrieves details of a databaseObject",response = DatabaseObject.class, produces = "application/json")
//    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json")
//    @ResponseBody
//    public PhysicalEntity queryById(@ApiParam(defaultValue = "R-HSA-1640170",required = true) @PathVariable String id)  {
//        return physicalEntityService.findByIdWithLegacyFields(id);
//    }

}