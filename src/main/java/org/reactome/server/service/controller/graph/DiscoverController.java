package org.reactome.server.service.controller.graph;

import io.swagger.annotations.*;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.schema.SchemaDataSet;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.exception.UnprocessableEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RestController
@Api(tags = "discover", description = "Reactome Data: Search engines discovery schema")
@RequestMapping("/data")
public class DiscoverController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private DatabaseObjectService databaseObjectService;

    @Autowired
    private GeneralService generalService;


    @ApiOperation(value = "The schema.org for an Event in Reactome knowledgebase", notes = "For each event (reaction or pathway) this method generates a json file representing the dataset object as defined by schema.org (http). This is mainly used by search engines in order to index the data")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier does not match with any event", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/discover/{identifier}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public SchemaDataSet eventDiscovery(@ApiParam(value = "An event identifier", defaultValue = "R-HSA-446203",required = true) @PathVariable String identifier) throws ClassNotFoundException {
        SchemaDataSet dataSet;
        try {
            Event event = databaseObjectService.findById(identifier);
            if(event == null) throw new NotFoundException(identifier + " not found");
            dataSet = new SchemaDataSet(event, generalService.getDBVersion());
        } catch (ClassCastException ex) {
            throw new UnprocessableEntityException();
        }
        infoLogger.info("Request data discovery: {}", identifier );
        return dataSet;
    }
}
