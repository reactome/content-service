package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.reactome.server.tools.interactors.mapper.InteractionMapper;
import org.reactome.server.tools.interactors.model.PsicquicResource;
import org.reactome.server.tools.manager.InteractionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@RestController
@Api(value = "/interactors", description = "Molecule interactors")
@RequestMapping("/interactors/psicquic")
public class PsicquicInteractionsController {

    @Autowired
    private InteractionManager interactions;

    @ApiOperation(value = "Retrieve a list of all Psicquic Registries services", response = PsicquicResource.class)
    @RequestMapping(value = "/resources", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<PsicquicResource> getResources()  {
        return interactions.getPsicquicResources();
    }

    @ApiOperation(value = "Retrieve a detailed clustered interaction, sorted by score, of a given accession by resource.", response = InteractionMapper.class)
    @RequestMapping(value = "/{resource}/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InteractionMapper getProteinDetailsByResource(@PathVariable String resource,
                                                         @PathVariable String acc)  {

        return interactions.getPsicquicProteinsDetails(Collections.singletonList(acc), resource);
    }

    @ApiOperation(value = "Retrieve a detailed clustered interaction, sorted by score, of a given accession(s) by resource.", response = InteractionMapper.class)
    @RequestMapping(value = "/proteins/{resource}/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public InteractionMapper getProteinsDetailsByResource(@PathVariable String resource,
                                                          @RequestBody String proteins) {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));

        return interactions.getPsicquicProteinsDetails(accs, resource);
    }

    @ApiOperation(value = "Retrieve a summary of a given accession by resource", response = InteractionMapper.class)
    @RequestMapping(value = "/{resource}/{acc}/summary", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InteractionMapper getProteinSummaryByResource(@PathVariable String resource,
                                                         @PathVariable String acc)  {

        return interactions.getPsicquicProteinsSummary(Collections.singletonList(acc), resource);
    }

    @ApiOperation(value = "Retrieve a summary of a given accession list by resource.", response = InteractionMapper.class)
    @RequestMapping(value = "/proteins/{resource}/summary", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public InteractionMapper getProteinsSummaryByResource(@PathVariable String resource,
                                                          @RequestBody String proteins) {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));

        return interactions.getPsicquicProteinsSummary(accs, resource);

    }

}
