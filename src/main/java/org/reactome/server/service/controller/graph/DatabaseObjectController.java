package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 19.05.16.
 */
@RestController
@Api(tags = "data", description = "Reactome Data ")
@RequestMapping("/data")
public class DatabaseObjectController {

    @Autowired
    private DatabaseObjectService databaseObjectService;

    @ApiOperation(value = "Retrieves details of a databaseObject",response = DatabaseObject.class, produces = "application/json")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject queryById(@ApiParam(defaultValue = "R-HSA-1640170",required = true) @PathVariable String id) throws IOException {
        DatabaseObject databaseObject = databaseObjectService.findById(id);
        return databaseObject;
    }
}
