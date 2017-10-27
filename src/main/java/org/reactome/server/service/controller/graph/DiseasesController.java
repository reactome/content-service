package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.Disease;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.service.exception.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@RestController
@Api(tags = "diseases", description = "Reactome Data: Disease related queries")
@RequestMapping("/data")
public class DiseasesController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private SchemaService schemaService;

    @ApiOperation(
            value = "The list of disease objects",  notes = "It retrieves the list of diseases annotated in Reactome",
            response = DatabaseObject.class, responseContainer = "List"
    )
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class),
    })
    @RequestMapping(value = "/diseases", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Disease> getDiseases() {
        infoLogger.info("Request for all diseases");
        return schemaService.getByClass(Disease.class);
    }

    @ApiOperation(value = "The list of diseases DOID",  notes = "It retrieves the list of disease DOIDs annotated in Reactome")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class),
    })
    @RequestMapping(value = "/diseases/doid", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getDiseasesSummary() {
        infoLogger.info("Request for all diseases");
        List<String> diseases = schemaService.getByClass(Disease.class).stream().map(d -> d.getId() + "\t" + d.getDatabaseName() + ":" + d.getIdentifier()).collect(Collectors.toList());
        return String.join("\n", diseases);
    }
}
