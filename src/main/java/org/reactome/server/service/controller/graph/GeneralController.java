package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.reactome.server.graph.domain.model.DBInfo;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.exception.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RestController
@Api(tags = {"database"})
@RequestMapping("/data")
public class GeneralController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private GeneralService generalService;

    @ApiOperation(value = "The name of current database")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/database/name", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getDBName()  {
        infoLogger.info("Request for DatabaseName");
        return generalService.getDBInfo().getName();
    }

    @ApiOperation(value = "The version number of current database")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/database/version", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getDBVersion()  {
        infoLogger.info("Request for DatabaseVersion");
        return "" + generalService.getDBInfo().getVersion();
    }

    @ApiIgnore
    @RequestMapping(value = "/database/info", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DBInfo getChecksum() {
        infoLogger.info("Request for DatabaseInfo");
        return generalService.getDBInfo();
    }
}
