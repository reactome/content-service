package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.reactome.server.graph.service.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by flo on 06/06/16.
 */
@RestController
@Api(tags = "database", description = "Reactome Data: Database info queries.")
@RequestMapping("/data")
public class GeneralController {

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
}
