package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.service.MappingService;
import org.reactome.server.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@RestController
@Tag(name = "mapping", description = "Reactome Data: Mapping related queries")
@RequestMapping("/data")
public class MappingController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private MappingService mappingService;

    @Operation(
            summary = "The reactions where an identifier can be mapped to",
            description = "Entities play different roles in reactions. This method retrieves the reactions for which an identifier plays a role ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No reactions found for the submitted identifier/resource"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/mapping/{resource}/{identifier}/reactions", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReactionLikeEvent> getReactionsLikeEvent(@Parameter(description = "The <a href='/content/schema/objects/ReferenceDatabase'>resource</a> <b>name</b> for which the identifier is submitted", example = "UniProt", required = true)
                                                               @PathVariable String resource,
                                                               @Parameter(description = "The identifier to be mapped", example = "PTEN", required = true)
                                                               @PathVariable String identifier,
                                                               @Parameter(description = "Species for which the result is filtered. Accepts <b>taxonomy id</b>, <b>species name</b> and <b>dbId</b>. Important: when identifier points to chemical, this becomes mandatory and if not provided, the default is 'Homo sapiens'", example = "9606")
                                                               @RequestParam(required = false) String species) {
        species = getSpecies(resource, species);
        Collection<ReactionLikeEvent> rtn;
        if (species != null && !species.isEmpty()) {
            rtn = mappingService.getReactionsLikeEvent(resource, identifier, species);
        } else {
            rtn = mappingService.getReactionsLikeEvent(resource, identifier);
        }

        if (rtn == null || rtn.isEmpty())
            throw new NotFoundException("No reactions found for " + resource + ":" + identifier);
        infoLogger.info("Request for reactions for {}:{}", resource, identifier);
        return rtn;
    }

    @Operation(
            summary = "The lower level pathways where an identifier can be mapped to",
            description = "Entities play different roles in reactions, and reactions are events that conform a pathway. This method retrieves the pathways for which an identifier plays a role within one or more of their events."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No pathways found for the submitted identifier/resource"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/mapping/{resource}/{identifier}/pathways", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getPathways(@Parameter(description = "The <a href='/content/schema/objects/ReferenceDatabase'>resource</a> <b>name</b> for which the identifier is submitted", example = "UniProt", required = true)
                                           @PathVariable String resource,
                                           @Parameter(description = "The identifier to be mapped", example = "PTEN", required = true)
                                           @PathVariable String identifier,
                                           @Parameter(description = "Species for which the result is filtered. Accepts <b>taxonomy id</b>, <b>species name</b> and <b>dbId</b>. Important: when identifier points to chemical, this becomes mandatory and if not provided, the default is 'Homo sapiens'", example = "9606")
                                           @RequestParam(required = false) String species) {
        species = getSpecies(resource, species);
        Collection<Pathway> rtn;

        //Retrieves pathways for which a gene ontology identifier within it. It uses different cypher query than returning pathways from different resource.
        if (resource.equalsIgnoreCase("go")) {
            String goIdentifier = identifier.toLowerCase().startsWith("go") ? identifier.replaceAll("[^0-9]", "") : identifier;
            if (species != null && !species.isEmpty()) {
                rtn = mappingService.getGoPathways(goIdentifier, species);
            } else {
                rtn = mappingService.getGoPathways(goIdentifier);
            }
        } else {
            if (species != null && !species.isEmpty()) {
                rtn = mappingService.getPathways(resource, identifier, species);
            } else {
                rtn = mappingService.getPathways(resource, identifier);
            }
        }

        if (rtn == null || rtn.isEmpty())
            throw new NotFoundException("No pathways found for " + resource + ":" + identifier);
        infoLogger.info("Request for pathways for {}:{}", resource, identifier);
        return rtn;
    }

    @Autowired
    public void setMappingService(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    // When the resource is related to chemicals, a species becomes mandatory. By default "Homo sapiens"
    private String getSpecies(final String resource, final String species) {
        if (species == null && resource != null) {
            String aux = resource.toLowerCase();
            if (aux.contains("chebi") || aux.contains("compound") || aux.contains("pubchem")) return "9606";
        }
        return species;
    }
}
