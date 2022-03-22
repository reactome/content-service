package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.Disease;
import org.reactome.server.graph.service.SchemaService;
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
@Tag(name = "diseases", description = "Reactome Data: Disease related queries")
@RequestMapping("/data")
public class DiseasesController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private SchemaService schemaService;

    @Operation(summary = "The list of disease objects", description = "It retrieves the list of diseases annotated in Reactome")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    })
    @RequestMapping(value = "/diseases", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Disease> getDiseases() {
        infoLogger.info("Request for all diseases");
        return schemaService.getByClass(Disease.class);
    }

    @Operation(summary = "The list of diseases DOID", description = "It retrieves the list of disease DOIDs annotated in Reactome")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    })
    @RequestMapping(value = "/diseases/doid", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getDiseasesSummary() {
        infoLogger.info("Request for all diseases");
        List<String> diseases = schemaService.getByClass(Disease.class).stream().map(d -> d.getDbId() + "\t" + d.getDatabaseName() + ":" + d.getIdentifier()).collect(Collectors.toList());
        return String.join("\n", diseases);
    }
}
