package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceMolecule;
import org.reactome.server.graph.domain.model.ReferenceSequence;
import org.reactome.server.graph.domain.result.ComponentOf;
import org.reactome.server.graph.service.ComponentService;
import org.reactome.server.graph.service.PhysicalEntityService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@RestController
@Api(tags = "entities", description = "Reactome Data: PhysicalEntity queries")
@RequestMapping("/data")
public class PhysicalEntityController {

    @Autowired
    private PhysicalEntityService physicalEntityService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private SchemaService schemaService;

    @ApiOperation(value = "All other forms of a PhysicalEntity",
            notes = "Other forms are PhysicalEntities that share the same ReferenceEntity identifier",
            produces = "application/json")
    @RequestMapping(value = "/entity/{id}/otherForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getOtherFormsOf(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<PhysicalEntity> physicalEntities = physicalEntityService.getOtherFormsOf(id);
        if (physicalEntities == null || physicalEntities.isEmpty())
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        return physicalEntities;
    }

    @ApiOperation(value = "A list of larger structures containing the entity", notes = "A list of simplified entries(type, names, ids) which include given id as component")
    @RequestMapping(value = "/entity/{id}/componentOf", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ComponentOf> getComponentsOf(@ApiParam(defaultValue = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<ComponentOf> componentOfs = componentService.getComponentsOf(id);
        if (componentOfs == null || componentOfs.isEmpty())
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        return componentOfs;
    }

    @ApiOperation(value = "A list with the entities contained in a given complex", notes = "Retrieves a list of complex subunits that are not Complex class")
    @RequestMapping(value = "/complex/{id}/subunits", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getComplexSubunits(@ApiParam(defaultValue = "R-HSA-5674003", required = true) @PathVariable String id) {
        Collection<PhysicalEntity> componentOfs = physicalEntityService.getComplexSubunits(id);
        if (componentOfs == null || componentOfs.isEmpty())
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        return componentOfs;
    }

    @ApiIgnore
    @ApiOperation(value = "The list of ReferenceMolecule objects", notes = "It retrieves the list of reference molecules for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceMolecules", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceMolecule> getReferenceMolecules() {
        return schemaService.getByClass(ReferenceMolecule.class);
    }

    @ApiIgnore
    @ApiOperation(value = "The list of ReferenceMolecule identifiers", notes = "It retrieves the list of reference molecules identifiers for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceMolecules/identifiers", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getReferenceMoleculesSummary() {
        List<String> rtn = schemaService.getByClass(ReferenceMolecule.class).stream().map(r -> r.getId() + "\t" + r.getDatabaseName() + ":" + r.getIdentifier()).collect(Collectors.toList());
        return String.join("\n", rtn);
    }

    @ApiIgnore
    @ApiOperation(value = "The list of ReferenceSequence objects", notes = "It retrieves the list of reference sequences for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceSequences", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceSequence> getReferenceSequences(@ApiParam(value = "Page number", defaultValue = "1")
                                                               @RequestParam Integer page,
                                                               @ApiParam(value = "Number of elements per page", defaultValue = "20")
                                                               @RequestParam Integer offset) {
        return schemaService.getByClass(ReferenceSequence.class, page, offset);
    }

    @ApiIgnore
    @ApiOperation(value = "The list of ReferenceSequence identifiers", notes = "It retrieves the list of reference sequences identifiers for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceSequences/identifiers", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getReferenceSequencesSummary(@ApiParam(value = "Page number", defaultValue = "1")
                                               @RequestParam Integer page,
                                               @ApiParam(value = "Number of elements per page", defaultValue = "20")
                                               @RequestParam Integer offset) {
        List<String> rtn = schemaService.getByClass(ReferenceSequence.class, page, offset).stream().map(r -> r.getId() + "\t" + r.getDatabaseName() + ":" + r.getIdentifier()).collect(Collectors.toList());
        return String.join("\n", rtn);
    }
}
