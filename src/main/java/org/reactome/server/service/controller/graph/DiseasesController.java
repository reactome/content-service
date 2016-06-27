package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.reactome.server.graph.domain.model.Disease;
import org.reactome.server.graph.service.SchemaService;
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
@RestController
@Api(tags = "diseases", description = "Reactome Data: Disease related queries")
@RequestMapping("/data")
public class DiseasesController {

    @Autowired
    private SchemaService schemaService;

    @ApiOperation(value = "The list of disease objects",  notes = "It retrieves the list of diseases for which there are annotations in Reactome")
    @RequestMapping(value = "/diseases", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Disease> getDiseases() {
        return schemaService.getByClass(Disease.class);
    }

    @ApiOperation(value = "The list of diseases DOID",  notes = "It retrieves the list of diseases DOIDs for which there are annotations in Reactome")
    @RequestMapping(value = "/diseases/doid", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getDiseasesSummary() {
        List<String> diseases = schemaService.getByClass(Disease.class).stream().map(d -> d.getId() + "\t" + d.getDatabaseName() + ":" + d.getIdentifier()).collect(Collectors.toList());
        return String.join("\n", diseases);
    }
}
