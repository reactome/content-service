package org.reactome.server.service.controller.graph;

import io.swagger.annotations.*;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceMolecule;
import org.reactome.server.graph.domain.model.ReferenceSequence;
import org.reactome.server.graph.domain.result.ComponentOf;
import org.reactome.server.graph.service.AdvancedLinkageService;
import org.reactome.server.graph.service.PhysicalEntityService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Florian Korninger <florian.korninger@ebi.ac.uk>
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@RestController
@Api(tags = "entities", description = "Reactome Data: PhysicalEntity queries")
@RequestMapping("/data")
public class PhysicalEntityController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private PhysicalEntityService physicalEntityService;

    @Autowired
    private AdvancedLinkageService advancedLinkageService;

    @Autowired
    private SchemaService schemaService;

    @ApiOperation(value = "All other forms of a PhysicalEntity",
            notes = "Retrieves a list containing all other forms of the given PhysicalEntity. These other forms are PhysicalEntities that share the same ReferenceEntity identifier, e.g. PTEN H93R[R-HSA-2318524] and PTEN C124R[R-HSA-2317439] are two forms of PTEN.",
            produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier does not match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/entity/{id}/otherForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getOtherFormsOf(@ApiParam(value = "DbId or StId of a PhysicalEntity", defaultValue = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<PhysicalEntity> physicalEntities = physicalEntityService.getOtherFormsOf(id);
        if (physicalEntities == null || physicalEntities.isEmpty())
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request all other forms of PhysicalEntity with id: {}", id);
        return physicalEntities;
    }

    @ApiOperation(value = "A list of larger structures containing the entity", notes = "Retrieves the list of structures (Complexes and Sets) that include the given entity as their component. It should be mentioned that the list includes only simplified entries (type, names, ids) and not full information about each item.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier does not match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/entity/{id}/componentOf", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ComponentOf> getComponentsOf(@ApiParam(defaultValue = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<ComponentOf> componentOfs = advancedLinkageService.getComponentsOf(id);
        if (componentOfs == null || componentOfs.isEmpty())
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for all components of Entry with id: {}", id);
        return componentOfs;
    }

    @ApiOperation(value = "A list with the entities contained in a given complex", notes = "Retrieves the list of subunits that constitute any given complex. In case the complex comprises other complexes, this method recursively traverses the content returning each contained PhysicalEntity. Contained complexes and entity sets can be excluded setting the 'excludeStructures' optional parameter to 'true'")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Identifier does not match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/complex/{id}/subunits", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getComplexSubunits( @ApiParam(value = "The complex for which subunits are requested", defaultValue = "R-HSA-5674003", required = true)
                                                         @PathVariable String id,
                                                          @ApiParam(value = "Specifies whether contained complexes and entity sets are excluded in the response", defaultValue = "false")
                                                         @RequestParam(defaultValue = "false") boolean excludeStructures) {
        Collection<PhysicalEntity> componentOfs;
        if (excludeStructures) {
            componentOfs = physicalEntityService.getComplexSubunitsNoStructures(id);
        } else {
            componentOfs = physicalEntityService.getComplexSubunits(id);
        }
        if (componentOfs == null || componentOfs.isEmpty())
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for subunits of Complex with id: {}", id);
        return componentOfs;
    }

    //##################### API Ignored but still available for internal purposes #####################//

    @ApiIgnore
    @ApiOperation(value = "The list of ReferenceMolecule objects", notes = "It retrieves the list of reference molecules for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceMolecules", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceMolecule> getReferenceMolecules() {
        infoLogger.info("Request total list of ReferenceMolecules");
        return schemaService.getByClass(ReferenceMolecule.class);
    }

    @ApiIgnore
    @ApiOperation(value = "The list of ReferenceMolecule identifiers", notes = "It retrieves the list of reference molecules identifiers for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceMolecules/identifiers", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getReferenceMoleculesSummary() {
        List<String> rtn = schemaService.getByClass(ReferenceMolecule.class).stream().map(r -> r.getId() + "\t" + r.getDatabaseName() + ":" + r.getIdentifier()).collect(Collectors.toList());
        infoLogger.info("Request total list of ReferenceMolecules");
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
        infoLogger.info("Request total list of ReferenceSequences");
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
        infoLogger.info("Request total list of ReferenceSequences");
        return String.join("\n", rtn);
    }
}
