package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.result.ComponentOf;
import org.reactome.server.graph.service.ComponentService;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 19.05.16.
 */
@RestController
@Api(tags = "basic", description = "Reactome Data: Basic queries.")
@RequestMapping("/data")
public class ComponentController {

    @Autowired
    private ComponentService componentService;

    @ApiOperation(value = "Retrieves a list of simplified entries(type, names, ids) which include given id as component.",
            notes = "",
            response = ComponentOf.class,
            responseContainer = "List",
            produces = "application/json")
    @RequestMapping(value = "/getComponentOf/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ComponentOf> getComponentsOf(@ApiParam(defaultValue = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<ComponentOf> componentOfs = componentService.getComponentsOf(id);
        if (componentOfs == null || componentOfs.isEmpty()) throw new NotFoundException("Id: " + id + " has not been found in the System");
        return componentOfs;
    }

}
