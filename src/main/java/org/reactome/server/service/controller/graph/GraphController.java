package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.result.Participant;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.service.ParticipantService;
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
    private DatabaseObjectService databaseObjectService;
    @Autowired
    private GeneralService generalService;
    @Autowired
    private ParticipantService participantService;
//    @Autowired
//    private EventService eventService;
//    @Autowired
//    private PhysicalEntityService physicalEntityService;

//    private FileWriter writer;

    public GraphController() throws IOException {
//        writer = new FileWriter(new File("test.txt"));
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/topLevelPathway", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getTopLevelPathways()  {
        return generalService.getTopLevelPathways();
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways for given species name",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/topLevelPathway/{speciesName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getTopLevelPathways(@ApiParam(defaultValue = "Homo sapiens",required = true) @PathVariable String speciesName)  {
        return generalService.getTopLevelPathways(speciesName);
    }

    @ApiOperation(value = "Retrieves all species",response = Species.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/species", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Species> getSpecies()  {
        return generalService.getAllSpecies();
    }

    @ApiOperation(value = "Retrieves details of a databaseObject",response = DatabaseObject.class, produces = "application/json")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject queryById(@ApiParam(defaultValue = "R-HSA-1640170",required = true) @PathVariable String id) throws IOException {
//        Long start, time;
//        start = System.currentTimeMillis();

        DatabaseObject databaseObject = databaseObjectService.findById(id);
//        DatabaseObject databaseObject  = new Pathway();
//        databaseObject.setDbId(123l);
//        time = System.currentTimeMillis() - start;

//        writer.write(time.toString() + "\n");
//        writer.flush();
        return databaseObject;
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participatingPhysicalEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getParticipatingPhysicalEntities(@ApiParam(defaultValue = "R-HSA-5205685",required = true) @PathVariable String id)  {
        return participantService.getParticipatingPhysicalEntities(id);
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",response = Participant.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participants", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Participant> getParticipatingPhysicalEntities2(@ApiParam(defaultValue = "5205685",required = true) @PathVariable Long id)  {
        return participantService.getParticipants(id);
    }

    @ApiOperation(value = "Retrieves a list of participants for a given pathway",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/detail/{id}/participatingReferenceEntities", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceEntity> getParticipationgReferenceEntities(@ApiParam(defaultValue = "5205685",required = true) @PathVariable Long id)  {
        return participantService.getParticipatingReferenceEntities(id);
    }

    @ApiOperation(value = "Retrieves a list of databaseObjects for given class name",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/details/{className:.*}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<DatabaseObject> getDatabaseObjectsForClassName(@ApiParam(defaultValue = "Species",required = true) @PathVariable String className,
                                                               @ApiParam(defaultValue = "1", required = true) @RequestParam(required = true) Integer page,
                                                               @ApiParam(defaultValue = "25", required = true) @RequestParam(required = true) Integer offset) throws ClassNotFoundException {
        return generalService.findObjectsByClassName(className,page,offset);
    }

    @ApiOperation(value = "Retrieves the list of the lower level pathways where the passed PhysicalEntity or Event are present", response = SimpleDatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/pathwaysFor/{stableIdentifier}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<SimpleDatabaseObject> getPathwaysFor(@ApiParam(defaultValue = "R-HSA-199420") @PathVariable String stableIdentifier,
                                                           @ApiParam(defaultValue = "48887", required = false) @RequestParam(required = false, defaultValue = "48887") Long speciesId){
        Collection<SimpleDatabaseObject> rtn = generalService.getPathwaysFor(stableIdentifier, speciesId);
        return rtn;
    }

    @ApiIgnore
    @RequestMapping(value = "/details/legacy/{className:.*}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<DatabaseObject> getDatabaseObjectsForClassNameLegacy(@PathVariable String className) throws ClassNotFoundException {
        return generalService.findObjectsByClassName(className,1, 2000000); //Integer.MAX_VALUE is too big for neo4j
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