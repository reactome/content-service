package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.Complex;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceMolecule;
import org.reactome.server.graph.domain.model.ReferenceSequence;
import org.reactome.server.graph.domain.result.ComponentOf;
import org.reactome.server.graph.service.AdvancedLinkageService;
import org.reactome.server.graph.service.PhysicalEntityService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Florian Korninger <florian.korninger@ebi.ac.uk>
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "entities", description = "Reactome Data: PhysicalEntity queries")
@RequestMapping("/data")
public class PhysicalEntityController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private PhysicalEntityService physicalEntityService;

    @Autowired
    private AdvancedLinkageService advancedLinkageService;

    @Autowired
    private SchemaService schemaService;

    @Operation(
            summary = "All other forms of a PhysicalEntity",
            description = "Retrieves a list containing all other forms of the given PhysicalEntity. These other forms are PhysicalEntities that share the same ReferenceEntity identifier, e.g. PTEN H93R[R-HSA-2318524] and PTEN C124R[R-HSA-2317439] are two forms of PTEN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/entity/{id}/otherForms", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getOtherFormsOf(@Parameter(description = "DbId or StId of a PhysicalEntity", example = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<PhysicalEntity> physicalEntities = physicalEntityService.getOtherFormsOf(id);
        if (physicalEntities == null || physicalEntities.isEmpty())
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request all other forms of PhysicalEntity with id: {}", id);
        return physicalEntities;
    }

    @Operation(summary = "A list of larger structures containing the entity", description = "Retrieves the list of structures (Complexes and Sets) that include the given entity as their component. It should be mentioned that the list includes only simplified entries (type, names, ids) and not full information about each item.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/entity/{id}/componentOf", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ComponentOf> getComponentsOf(@Parameter(example = "R-HSA-199420", required = true) @PathVariable String id) {
        Collection<ComponentOf> componentOfs = advancedLinkageService.getComponentsOf(id);
        if (componentOfs == null || componentOfs.isEmpty())
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for all components of Entry with id: {}", id);
        return componentOfs;
    }

    @Operation(
            summary = "A list with the entities contained in a given complex",
            description = "Retrieves the list of subunits that constitute any given complex. In case the complex comprises other complexes, this method recursively traverses the content returning each contained PhysicalEntity. Contained complexes and entity sets can be excluded setting the 'excludeStructures' optional parameter to 'true'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/complex/{id}/subunits", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<PhysicalEntity> getComplexSubunits(@Parameter(description = "The complex for which subunits are requested", example = "R-HSA-5674003", required = true)
                                                         @PathVariable String id,
                                                         @Parameter(description = "Specifies whether contained complexes and entity sets are excluded in the response", example = "false")
                                                         @RequestParam(defaultValue = "false") boolean excludeStructures) {
        Collection<PhysicalEntity> componentOfs;
        if (excludeStructures) {
            componentOfs = physicalEntityService.getPhysicalEntitySubunitsNoStructures(id);
        } else {
            componentOfs = physicalEntityService.getPhysicalEntitySubunits(id);
        }
        if (componentOfs == null || componentOfs.isEmpty())
            throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for subunits of Complex with id: {}", id);
        return componentOfs;
    }


    @Operation(
            summary = "A list of complexes containing the pair (identifier, resource)",
            description = "Retrieves the list of complexes that contain a given (identifier, resource). The method deconstructs the complexes into all its participants to do so."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Pair (identifier, resource) does not match with any in current data"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/complexes/{resource}/{identifier}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Complex> getComplexesFor(@Parameter(description = "The resource of the identifier for complexes are requested", example = "UniProt", required = true)
                                               @PathVariable String resource,
                                               @Parameter(description = "The identifier for which complexes are requested", example = "P00533", required = true)
                                               @PathVariable String identifier) {
        Collection<Complex> complexes = physicalEntityService.getComplexesFor(identifier, resource);
        if (complexes.isEmpty())
            throw new NotFoundException("No complexes found for (" + identifier + ", " + resource + ")");
        infoLogger.info("Request for complexes of identifier '{}' in resource '{}'", identifier, resource);
        return complexes;
    }
    //##################### API Ignored but still available for internal purposes #####################//

    @Hidden
    @Operation(summary = "The list of ReferenceMolecule objects", description = "It retrieves the list of reference molecules for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceMolecules", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceMolecule> getReferenceMolecules() {
        infoLogger.info("Request total list of ReferenceMolecules");
        return schemaService.getByClass(ReferenceMolecule.class);
    }

    @Hidden
    @Operation(summary = "The list of ReferenceMolecule identifiers", description = "It retrieves the list of reference molecules identifiers for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceMolecules/identifiers", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getReferenceMoleculesSummary() {
        List<String> rtn = schemaService.getByClass(ReferenceMolecule.class).stream().map(r -> r.getStId() + "\t" + r.getDatabaseName() + ":" + r.getIdentifier()).collect(Collectors.toList());
        infoLogger.info("Request total list of ReferenceMolecules");
        return String.join("\n", rtn);
    }

    @Hidden
    @Operation(summary = "The list of ReferenceSequence objects", description = "It retrieves the list of reference sequences for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceSequences", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReferenceSequence> getReferenceSequences(@Parameter(description = "Page number", example = "1")
                                                               @RequestParam Integer page,
                                                               @Parameter(description = "Number of elements per page", example = "20")
                                                               @RequestParam Integer offset) {
        infoLogger.info("Request total list of ReferenceSequences");
        return schemaService.getByClass(ReferenceSequence.class, page, offset);
    }

    @Hidden
    @Operation(summary = "The list of ReferenceSequence identifiers", description = "It retrieves the list of reference sequences identifiers for which there are annotations in Reactome")
    @RequestMapping(value = "/referenceSequences/identifiers", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getReferenceSequencesSummary(@Parameter(description = "Page number", example = "1")
                                               @RequestParam Integer page,
                                               @Parameter(description = "Number of elements per page", example = "20")
                                               @RequestParam Integer offset) {
        List<String> rtn = schemaService.getByClass(ReferenceSequence.class, page, offset).stream().map(r -> r.getStId() + "\t" + r.getDatabaseName() + ":" + r.getIdentifier()).collect(Collectors.toList());
        infoLogger.info("Request total list of ReferenceSequences");
        return String.join("\n", rtn);
    }
}
