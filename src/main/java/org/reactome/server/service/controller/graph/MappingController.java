package org.reactome.server.service.controller.graph;

import io.swagger.annotations.*;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.service.MappingService;
import org.reactome.server.service.exception.ErrorInfo;
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
@Api(tags = {"mapping"})
@RequestMapping("/data")
public class MappingController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private MappingService mappingService;

    @ApiOperation(
            value = "The reactions where an identifier can be mapped to",
            notes = "Entities play different roles in reactions. This method retrieves the reactions for which an identifier plays a role .",
            response = DatabaseObject.class, responseContainer = "List"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No reactions found for the submitted identifier/resource", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/mapping/{resource}/{identifier}/reactions", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<ReactionLikeEvent> getReactionsLikeEvent(@ApiParam(value = "The <a href='/content/schema/objects/ReferenceDatabase'>resource</a> <b>name</b> for which the identifier is submitted", defaultValue = "UniProt", required = true)
                                                              @PathVariable String resource,
                                                               @ApiParam(value = "The identifier to be mapped", defaultValue = "PTEN", required = true)
                                                              @PathVariable String identifier,
                                                               @ApiParam(value = "Species for which the result is filtered. Accepts <b>taxonomy id</b>, <b>species name</b> and <b>dbId</b>. Important: when identifier points to chemical, this becomes mandatory and if not provided, the default is 'Homo sapiens'", defaultValue = "9606")
                                                              @RequestParam(required = false) String species) {
        species = getSpecies(resource, species);
        Collection<ReactionLikeEvent> rtn;
        if (species != null && !species.isEmpty()) {
            rtn = mappingService.getReactionsLikeEvent(resource, identifier, species);
        } else {
            rtn = mappingService.getReactionsLikeEvent(resource, identifier);
        }

        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No reactions found for " + resource + ":" + identifier);
        infoLogger.info("Request for reactions for {}:{}", resource, identifier);
        return rtn;
    }

    @ApiOperation(
            value = "The lower level pathways where an identifier can be mapped to",
            notes = "Entities play different roles in reactions, and reactions are events that conform a pathway. This method retrieves the pathways for which an identifier plays a role within one or more of their events.",
            response = DatabaseObject.class, responseContainer = "List"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No pathways found for the submitted identifier/resource", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/mapping/{resource}/{identifier}/pathways", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getPathways(@ApiParam(value = "The <a href='/content/schema/objects/ReferenceDatabase'>resource</a> <b>name</b> for which the identifier is submitted", defaultValue = "UniProt", required = true)
                                          @PathVariable String resource,
                                           @ApiParam(value = "The identifier to be mapped", defaultValue = "PTEN", required = true)
                                          @PathVariable String identifier,
                                           @ApiParam(value = "Species for which the result is filtered. Accepts <b>taxonomy id</b>, <b>species name</b> and <b>dbId</b>. Important: when identifier points to chemical, this becomes mandatory and if not provided, the default is 'Homo sapiens'", defaultValue = "9606")
                                          @RequestParam(required = false) String species) {
        species = getSpecies(resource, species);
        Collection<Pathway> rtn;

        //Retrieves pathways for which a gene ontology identifier within it. It uses different cypher query than returning pathways from different resource.
        if (resource.toLowerCase().equals("go")){
            String goIdentifier = identifier.toLowerCase().startsWith("go") ? identifier.replaceAll("[^0-9]", "") : identifier;
            if (species != null && !species.isEmpty()) {
                rtn = mappingService.getGoPathways(goIdentifier, species);
            } else {
                rtn = mappingService.getGoPathways(goIdentifier);
            }
        } else{
            if (species != null && !species.isEmpty()) {
                rtn = mappingService.getPathways(resource, identifier, species);
            } else {
                rtn = mappingService.getPathways(resource, identifier);
            }
        }

        if (rtn == null || rtn.isEmpty()) throw new NotFoundException("No pathways found for " + resource + ":" + identifier);
        infoLogger.info("Request for pathways for {}:{}", resource, identifier);
        return rtn;
    }

    @Autowired
    public void setMappingService(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    // When the resource is related to chemicals, a species becomes mandatory. By default "Homo sapiens"
    private String getSpecies(final String resource, final String species){
        if (species == null && resource != null) {
            String aux = resource.toLowerCase();
            if (aux.contains("chebi") || aux.contains("compound") || aux.contains("pubchem")) return "9606";
        }
        return species;
    }
}
