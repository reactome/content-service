package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.result.ComponentOf;
import org.reactome.server.graph.service.ComponentService;
import org.reactome.server.graph.service.PhysicalEntityService;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@RestController
@Api(tags = "entities", description = "Reactome Data: PhysicalEntity queries" )
@RequestMapping("/data")
public class PhysicalEntityController {

    @Autowired
    private PhysicalEntityService physicalEntityService;

    @Autowired
    private ComponentService componentService;

    @ApiOperation(value = "All other forms of a PhysicalEntity",
            notes = "Other forms are PhysicalEntities that share the same ReferenceEntity identifier",
            produces = "application/json")
    @RequestMapping(value = "/entity/{id}/otherForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getOtherFormsOf(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<PhysicalEntity> physicalEntities = physicalEntityService.getOtherFormsOf(id);
        if (physicalEntities == null || physicalEntities.isEmpty())  throw new NotFoundException("Id: " + id + " has not been found in the System");
        return physicalEntities;
    }

    @ApiOperation(value = "A list of simplified entries(type, names, ids) which include given id as component",
            notes = "",
            response = ComponentOf.class,
            responseContainer = "List",
            produces = "application/json")
    @RequestMapping(value = "/entity/{id}/componentOf", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ComponentOf> getComponentsOf(@ApiParam(defaultValue = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<ComponentOf> componentOfs = componentService.getComponentsOf(id);
        if (componentOfs == null || componentOfs.isEmpty()) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return componentOfs;
    }

    @ApiOperation(value = "A list with the entities contained in a given complex", notes = "")
    @RequestMapping(value = "/complex/{id}/subunits", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getComplexSubunits(@ApiParam(defaultValue = "R-HSA-5674003", required = true) @PathVariable String id) {
        Collection<PhysicalEntity> componentOfs = physicalEntityService.getComplexSubunits(id);
        if (componentOfs == null || componentOfs.isEmpty()) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return componentOfs;
    }

}
