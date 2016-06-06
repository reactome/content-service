package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.service.PhysicalEntityService;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@RestController
@Api(tags = "physicalEntities", description = "Reactome Data: PhysicalEntity queries" )
@RequestMapping("/data")
public class PhysicalEntityController {

    @Autowired
    private PhysicalEntityService physicalEntityService;

    @ApiOperation(value = "Retrieves all other forms of a PhyicalEntity.",
            notes = "Other forms are PhysicalEntities that share the same ReferenceEntity identifier",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/otherForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getOtherFormsOf(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<PhysicalEntity> physicalEntities = physicalEntityService.getOtherFormsOf(id);
        if (physicalEntities == null || physicalEntities.isEmpty())  throw new NotFoundException("Id: " + id + " has not been found in the System");
        return physicalEntities;
    }

    @ApiOperation(value = "Retrieves all other forms of a PhyicalEntity.",
            notes = "Other forms are PhysicalEntities that share the same ReferenceEntity identifier. Retrieves a single property from the list of DatabaseObjects. ",
            produces = "application/json")
    @RequestMapping(value = "/detail/{id}/otherForms/{attributeName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Object> getOtherFormsOf(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-199420", required = true) @PathVariable String id,
                                              @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Collection<PhysicalEntity> physicalEntities = physicalEntityService.getOtherFormsOf(id);
        if (physicalEntities == null || physicalEntities.isEmpty())  throw new NotFoundException("Id: " + id + " has not been found in the System");
        return ControllerUtils.getProperties(physicalEntities, attributeName);
    }
}
